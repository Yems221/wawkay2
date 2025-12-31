package com.kufay.app.ui.models

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.kufay.app.data.db.entities.Notification
import com.kufay.app.ui.theme.AppTheme
import com.kufay.app.ui.theme.Lato
import com.kufay.app.ui.viewmodels.HomeViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun NotificationDetailDialog(
    notification: Notification,
    onDismiss: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()  // ✅ INJECTION ViewModel
) {
    // ✅ Déterminer la couleur de l'app
    val appType = try {
        AppType.fromPackageName(notification.packageName, notification.title)
    } catch (e: IllegalArgumentException) {
        null
    }

    val appColor = when (appType) {
        AppType.WAVE_PERSONAL -> AppTheme.colors.wavePersonal
        AppType.WAVE_BUSINESS -> AppTheme.colors.waveBusiness
        AppType.ORANGE_MONEY -> AppTheme.colors.orangeMoney
        AppType.MIXX -> AppTheme.colors.mixx
        null -> Color.Gray
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.Start
            ) {
                // Title from first line or app name
                val title = notification.text.split("\n").firstOrNull() ?: notification.title
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontFamily = Lato,
                        fontWeight = FontWeight.W500
                    ),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Extract and display key information
                val lines = notification.text.split("\n")
                lines.drop(1).forEach { line ->
                    if (line.isNotEmpty()) {
                        Text(
                            text = line,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }

                // Display the amount with special handling for Wave notifications
                val displayAmount = when {
                    notification.packageName == "com.wave.personal" &&
                            notification.title.contains("Paiement réussi", ignoreCase = true) -> {
                        val paymentPattern = """Vous avez payé (\d+(?:\.\d+)?F)""".toRegex()
                        val paymentMatch = paymentPattern.find(notification.text)

                        if (paymentMatch != null) {
                            "${paymentMatch.groupValues[1]} Franc CFA"
                        } else {
                            val altPattern = """payé (\d+(?:\.\d+)?F)""".toRegex()
                            val altMatch = altPattern.find(notification.text)

                            if (altMatch != null) {
                                "${altMatch.groupValues[1]} Franc CFA"
                            } else {
                                NotificationFormatter.formatAmount(
                                    notification.amount,
                                    notification.currency
                                )
                            }
                        }
                    }

                    notification.packageName == "com.wave.personal" &&
                            notification.title.contains("Transfert réussi", ignoreCase = true) -> {
                        val transferPattern = """Vous avez envoyé (\d+(?:\.\d+)?F)""".toRegex()
                        val transferMatch = transferPattern.find(notification.text)

                        if (transferMatch != null) {
                            "${transferMatch.groupValues[1]} Franc CFA"
                        } else {
                            NotificationFormatter.formatAmount(
                                notification.amount,
                                notification.currency
                            )
                        }
                    }

                    notification.packageName == "com.wave.business" &&
                            notification.text.contains("sur votre encaissement de", ignoreCase = true) -> {
                        val encaissementPattern = """sur votre encaissement de (\d+(?:\.\d+)?F?)""".toRegex()
                        val encaissementMatch = encaissementPattern.find(notification.text)

                        if (encaissementMatch != null) {
                            var amountText = encaissementMatch.groupValues[1]
                            if (!amountText.endsWith("F", ignoreCase = true)) {
                                amountText += "F"
                            }
                            "$amountText Franc CFA"
                        } else {
                            NotificationFormatter.formatAmount(
                                notification.amount,
                                notification.currency
                            )
                        }
                    }

                    notification.packageName == "com.wave.personal" ||
                            notification.packageName == "com.wave.business" -> {
                        val amountPattern = """(\d+\.\d+F)""".toRegex()
                        val amountMatch = amountPattern.find(notification.text)

                        if (amountMatch != null) {
                            val exactAmount = amountMatch.groupValues[1]
                            "$exactAmount Franc CFA"
                        } else if (notification.amount != null) {
                            NotificationFormatter.formatAmount(
                                notification.amount,
                                notification.currency
                            )
                        } else {
                            ""
                        }
                    }

                    notification.amount != null -> {
                        NotificationFormatter.formatAmount(
                            notification.amount,
                            notification.currency
                        )
                    }

                    else -> ""
                }

                // Display the amount if we have one
                if (displayAmount.isNotEmpty()) {
                    Text(
                        text = displayAmount,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                // Format and display date
                val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                val formattedDate = dateFormat.format(Date(notification.timestamp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = formattedDate,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }

                    TextButton(onClick = onDismiss) {
                        Text(
                            text = "Fermer",
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // ✅ NOUVEAU : Bouton Play pour lecture VERBATIM
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { viewModel.readNotificationVerbatim(notification) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = appColor
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Écouter",
                        tint = Color.White,
                        modifier = Modifier
                            .size(24.dp)
                            .padding(end = 8.dp)
                    )
                    Text(
                        text = "Écouter",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
