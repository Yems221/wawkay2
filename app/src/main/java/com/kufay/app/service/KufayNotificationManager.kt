package com.kufay.app.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Build
import androidx.core.app.NotificationCompat
import com.kufay.app.MainActivity
import com.kufay.app.R
import com.kufay.app.data.db.entities.Notification as KufayNotification
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class KufayNotificationManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val CHANNEL_ID = "kufay_financial_notifications"
        private const val CHANNEL_NAME = "Notifications Financières"
        private const val PLAY_ACTION = "com.kufay.app.ACTION_PLAY_NOTIFICATION"

        // Codes couleur pour chaque application (depuis Color.kt)
        private const val COLOR_WAVE_PERSONAL = "#00B2FF"      // WavePersonalBlue
        private const val COLOR_WAVE_BUSINESS = "#3F0FB7"      // WaveBusinessPurple
        private const val COLOR_ORANGE_MONEY = "#FF6D00"       // OrangeMoneyOrange
        private const val COLOR_MIXX = "#F1BE49"               // MixxYellow
    }

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications des transactions financières Kufay"
                enableLights(true)
                lightColor = Color.parseColor(COLOR_WAVE_PERSONAL)
                enableVibration(true)
                setShowBadge(true)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Affiche une notification Kufay dans la barre de statut
     */
    fun showKufayNotification(notification: KufayNotification, notificationId: Long) {
        // Déterminer la couleur selon l'app
        val appColor = getAppColor(notification.appTag)

        // Créer l'icône avec la pastille de couleur
        val largeIcon = createColoredIcon(appColor, notification.isIncomingTransaction)

        // Intent pour ouvrir l'app
        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("notification_id", notificationId)
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            notificationId.toInt(),
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Intent pour le bouton "Écouter"
        val playIntent = Intent(PLAY_ACTION).apply {
            putExtra("notification_id", notificationId)
            setPackage(context.packageName)
        }
        val playPendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId.toInt(),
            playIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Construire le titre avec indicateur visuel
        val titlePrefix = if (notification.isIncomingTransaction) "↓ " else ""
        val title = "$titlePrefix${getAppDisplayName(notification.appTag)}"

        // Construire le message
        val message = buildNotificationMessage(notification)

        // Créer la notification
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Logo Kufay
            .setLargeIcon(largeIcon)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setColor(Color.parseColor(appColor))
            .setAutoCancel(true) // Efface au clic
            .setContentIntent(contentPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .addAction(
                R.drawable.ic_launcher_foreground, // Icône bouton play (tu peux créer ic_play)
                "▶ Écouter",
                playPendingIntent
            )

        // Vibration pattern pour transaction entrante
        if (notification.isIncomingTransaction) {
            builder.setVibrate(longArrayOf(0, 300, 200, 300))
        }

        // Afficher la notification
        notificationManager.notify(notificationId.toInt(), builder.build())
    }

    /**
     * Crée une icône circulaire avec la couleur de l'app et une flèche si entrante
     */
    private fun createColoredIcon(colorHex: String, isIncoming: Boolean): Bitmap {
        val size = 128
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Dessiner le cercle de couleur (pastille)
        val paint = Paint().apply {
            isAntiAlias = true
            color = Color.parseColor(colorHex)
            style = Paint.Style.FILL
        }
        canvas.drawCircle(size / 2f, size / 2f, size / 2.2f, paint)

        // Dessiner une bordure blanche
        paint.apply {
            color = Color.WHITE
            style = Paint.Style.STROKE
            strokeWidth = 4f
        }
        canvas.drawCircle(size / 2f, size / 2f, size / 2.2f, paint)

        // Si transaction entrante, dessiner une flèche verte descendante
        if (isIncoming) {
            val arrowPaint = Paint().apply {
                isAntiAlias = true
                color = Color.parseColor("#4CAF50") // Vert
                style = Paint.Style.FILL
                strokeWidth = 6f
            }

            // Dessiner flèche ↓ (ligne + triangle)
            val centerX = size / 2f
            val centerY = size / 2f
            val arrowLength = size / 3f

            // Ligne verticale
            canvas.drawLine(
                centerX,
                centerY - arrowLength / 2,
                centerX,
                centerY + arrowLength / 2,
                arrowPaint
            )

            // Triangle (pointe vers le bas)
            val path = android.graphics.Path().apply {
                moveTo(centerX, centerY + arrowLength / 2) // Pointe
                lineTo(centerX - 10, centerY + arrowLength / 2 - 15) // Gauche
                lineTo(centerX + 10, centerY + arrowLength / 2 - 15) // Droite
                close()
            }
            canvas.drawPath(path, arrowPaint)
        }

        return bitmap
    }

    /**
     * Retourne la couleur associée à l'app
     */
    private fun getAppColor(appTag: String?): String {
        return when (appTag) {
            "WAVE_PERSONAL" -> COLOR_WAVE_PERSONAL
            "WAVE_BUSINESS" -> COLOR_WAVE_BUSINESS
            "ORANGE_MONEY" -> COLOR_ORANGE_MONEY
            "MIXX" -> COLOR_MIXX
            else -> COLOR_WAVE_PERSONAL // Par défaut
        }
    }

    /**
     * Retourne le nom d'affichage de l'app
     */
    private fun getAppDisplayName(appTag: String?): String {
        return when (appTag) {
            "WAVE_PERSONAL" -> "Wave"
            "WAVE_BUSINESS" -> "Wave Business"
            "ORANGE_MONEY" -> "Orange Money"
            "MIXX" -> "Mixx by Yas"
            else -> "Transaction"
        }
    }

    /**
     * Construit le message de la notification
     */
    private fun buildNotificationMessage(notification: KufayNotification): String {
        val amount = notification.amount?.let {
            String.format("%,.0f", it)
        } ?: notification.amountText ?: "N/A"

        val currency = notification.currency ?: "F"

        return when {
            notification.isIncomingTransaction -> {
                val from = notification.label?.let { " de $it" } ?: ""
                "✅ Reçu : $amount $currency$from"
            }
            else -> {
                val to = notification.label?.let { " à $it" } ?: ""
                "💸 Envoyé : $amount $currency$to"
            }
        }
    }

    /**
     * Annule une notification
     */
    fun cancelNotification(notificationId: Int) {
        notificationManager.cancel(notificationId)
    }

    /**
     * Annule toutes les notifications Kufay
     */
    fun cancelAllNotifications() {
        notificationManager.cancelAll()
    }
}