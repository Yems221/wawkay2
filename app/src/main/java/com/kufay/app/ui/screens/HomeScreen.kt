package com.kufay.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import com.kufay.app.ui.components.*
import com.kufay.app.ui.models.NotificationDetailDialog
import com.kufay.app.ui.viewmodels.HomeViewModel
import com.kufay.app.data.db.entities.Notification
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import com.kufay.app.R
import com.kufay.app.ui.models.AppType
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.platform.LocalContext
import androidx.datastore.preferences.core.stringPreferencesKey
import com.kufay.app.data.preferences.dataStore
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import androidx.compose.foundation.shape.CircleShape
import java.util.Locale
import java.text.NumberFormat
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import com.kufay.app.ui.components.BannerAd
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import android.content.Context
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.*
import androidx.compose.foundation.border
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.BorderStroke

// Dashboard Hero Section that displays at the top of the home screen
// Dashboard Hero Section that displays at the top of the home screen
@Composable
fun DashboardHero(
    modifier: Modifier = Modifier,
    appColor: Color,
    totalIncomingAmount: Double? = null,
    incomingAmountByApp: Map<String, Double> = emptyMap(),
    dailyIncomingAmount: Double? = null,
    onReadDailyTotal: () -> Unit,
    isCollapsed: Boolean = false,
    incomingTransactions: List<Pair<String, Double>> = emptyList(),
    // ✨ NOUVEAUX paramètres pour SearchBar
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    notificationCount: Int
) {
    var isExpanded by remember { mutableStateOf(false) }
    var isAmountVisible by remember { mutableStateOf(false) }
    var showDebugPopup by remember { mutableStateOf(false) }

    val actuallyExpanded = isExpanded && !isCollapsed

    // ✨ Card Material Design
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = appColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { isExpanded = !isExpanded }
                .padding(16.dp)
        ) {
            // Title Row
            AnimatedVisibility(
                visible = !isCollapsed,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Text(
                    text = "Ku la fay ?",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // Total Amount Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        bottom = if (actuallyExpanded) 8.dp else if (isCollapsed) 0.dp else 8.dp
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ArrowDownward,
                        contentDescription = "Incoming",
                        tint = Color.White,
                        modifier = Modifier
                            .size(if (isCollapsed) 20.dp else 24.dp)
                            .background(
                                color = Color.White.copy(alpha = 0.3f),
                                shape = CircleShape
                            )
                            .padding(if (isCollapsed) 3.dp else 4.dp)
                    )

                    Text(
                        text = "Total Reçu \n Aujourd'hui",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onReadDailyTotal,
                        modifier = Modifier.size(if (isCollapsed) 24.dp else 28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Read Total du Jour",
                            tint = Color.White,
                            modifier = Modifier
                                .size(if (isCollapsed) 20.dp else 24.dp)
                                .background(
                                    color = Color.White.copy(alpha = 0.3f),
                                    shape = CircleShape
                                )
                                .padding(if (isCollapsed) 3.dp else 4.dp)
                        )
                    }

                    val formattedTotalAmount = dailyIncomingAmount?.let {
                        formatAmount(it)
                    } ?: "0 Franc CFA"

                    Box(
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .widthIn(min = 100.dp)
                    ) {
                        Text(
                            text = if (isAmountVisible) formattedTotalAmount else "*****",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Visible
                        )
                    }

                    // Debug button - commenté pour usage futur
                    /*
                    if (!isCollapsed) {
                        IconButton(
                            onClick = { showDebugPopup = true },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.BugReport,
                                contentDescription = "Debug Amounts",
                                tint = Color.White.copy(alpha = 0.7f)
                            )
                        }
                    }
                    */

                    IconButton(
                        onClick = { isAmountVisible = !isAmountVisible },
                        modifier = Modifier.size(if (isCollapsed) 24.dp else 28.dp)
                    ) {
                        Icon(
                            imageVector = if (isAmountVisible) Icons.Default.Visibility else Icons.Outlined.VisibilityOff,
                            contentDescription = if (isAmountVisible) "Hide Amount" else "Show Amount",
                            tint = Color.White
                        )
                    }
                }
            }

            // ✨ NOUVEAU : SearchBar intégrée dans le Card (visible seulement si pas collapsed)
            AnimatedVisibility(
                visible = !isCollapsed,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    placeholder = {
                        Text(
                            "Chercher des notifications...",
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Search",
                            tint = Color.White
                        )
                    },
                    trailingIcon = {
                        Text(
                            text = notificationCount.toString(),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color.White.copy(alpha = 0.5f),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                        cursorColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }

            // Expandable Content (breakdown par app)
            AnimatedVisibility(
                visible = actuallyExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    if (incomingAmountByApp.isNotEmpty()) {
                        Divider(
                            color = Color.White.copy(alpha = 0.3f),
                            thickness = 1.dp,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )

                        Text(
                            text = "Par Application",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White.copy(alpha = 0.1f))
                        ) {
                            incomingAmountByApp.forEach { (packageName, amount) ->
                                val appName = getAppNameFromPackage(packageName, "")
                                val formattedAmount = formatAmount(amount)

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp, horizontal = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = appName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.White,
                                        fontWeight = FontWeight.Normal
                                    )

                                    Text(
                                        text = if (isAmountVisible) formattedAmount else "*****",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.White,
                                        fontWeight = FontWeight.SemiBold,
                                        textAlign = TextAlign.End
                                    )
                                }

                                if (packageName != incomingAmountByApp.keys.last()) {
                                    Divider(
                                        color = Color.White.copy(alpha = 0.1f),
                                        thickness = 0.5.dp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Debug popup
            if (showDebugPopup) {
                AlertDialog(
                    onDismissRequest = { showDebugPopup = false },
                    title = { Text("Debug: Incoming Amounts") },
                    text = {
                        LazyColumn {
                            if (incomingTransactions.isEmpty()) {
                                item { Text("No incoming transactions found.") }
                            } else {
                                items(incomingTransactions) { (description, amount) ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = description,
                                            style = MaterialTheme.typography.bodyMedium,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Text(
                                            text = formatAmount(amount),
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Divider()
                                }
                                item {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("TOTAL", fontWeight = FontWeight.Bold)
                                        Text(
                                            text = formatAmount(incomingTransactions.sumOf { it.second }),
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        Button(onClick = { showDebugPopup = false }) {
                            Text("Close")
                        }
                    }
                )
            }
        }
    }
}

// Helper function to get app name from package name
// Remove private keyword
fun getAppNameFromPackage(packageName: String, title: String): String {
    return when {
        packageName == "com.wave.personal" -> "Wave"
        packageName == "com.wave.business" -> "Wave Business"
        packageName == "com.google.android.apps.messaging" -> {
            when {
                title.contains("OrangeMoney", ignoreCase = true) -> {
                    // Extraire le type d'opération du titre
                    val operation = extractOperationType(title, "OrangeMoney")
                    "Orange Money - $operation"
                }
                title.contains("Mixx by Yas", ignoreCase = true) -> {
                    // Extraire le type d'opération du titre
                    val operation = extractOperationType(title, "Mixx by Yas")
                    "Mixx by Yas - $operation"
                }
                else -> "Messaging Apps"
            }
        }
        else -> packageName.split(".").last().capitalize(java.util.Locale.getDefault())
    }
}
// Fonction auxiliaire pour extraire le type d'opération
private fun extractOperationType(title: String, serviceName: String): String {
    // Supprimer le nom du service du titre pour isoler l'opération
    val serviceRemoved = title.replace(serviceName, "", ignoreCase = true).trim()

    // Rechercher des mots clés courants pour les opérations
    return when {
        serviceRemoved.contains("recu", ignoreCase = true) -> "Transfert Reçu"
        serviceRemoved.contains("transfert", ignoreCase = true) -> "Transfert envoyé"
        serviceRemoved.contains("depot", ignoreCase = true) -> "Dépôt"
        serviceRemoved.contains("retire", ignoreCase = true) -> "Retrait"
        serviceRemoved.contains("operation de", ignoreCase = true) -> "Paiement"
        // Ajouter d'autres types d'opérations selon vos besoins
        else -> serviceRemoved.take(20) // Prendre les 20 premiers caractères si aucun mot-clé reconnu
    }
}

// Helper function to format amount with thousand separators
// Improved formatAmount function to handle larger amounts with proper formatting
private fun formatAmount(amount: Double): String {
    val formatter = NumberFormat.getNumberInstance(Locale.FRANCE) // Uses space as thousand separator
    val formattedNumber = formatter.format(amount.toLong()).replace(" ", ".")
    return "$formattedNumber Franc CFA"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel,
    onNavigateToSettings: () -> Unit,
    onNavigateToTrash: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val notifications by viewModel.notifications.collectAsState(initial = emptyList())
    val searchQuery by viewModel.searchQuery.collectAsState("")

    // Get incoming transaction stats
    val totalIncomingAmount by viewModel.totalIncomingAmount.collectAsState()
    val incomingAmountByApp by viewModel.incomingAmountByApp.collectAsState()
    val dailyIncomingAmount by viewModel.dailyIncomingAmount.collectAsState()

    // Get the user-selected app color from the theme
    val context = LocalContext.current
    val appColorFlow = remember {
        context.dataStore.data.map { preferences ->
            preferences[stringPreferencesKey("app_main_color")] ?: "#006400"
        }
    }
    val appColorHex by appColorFlow.collectAsState(initial = "#006400")
    val appColor = try {
        Color(android.graphics.Color.parseColor(appColorHex))
    } catch (e: Exception) {
        Color(0xFF006400) // Default green
    }

    // Filter states
    val selectedAppTypes by viewModel.selectedAppTypes.collectAsState()
    val dateFilterType by viewModel.dateFilterType.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val selectedDateRange by viewModel.selectedDateRange.collectAsState()

    // UI states
    val showFilters = remember { mutableStateOf(false) }
    val readingNotificationId = remember { mutableStateOf<Long?>(null) }
    val selectedNotification = remember { mutableStateOf<Notification?>(null) }

    // For date picker
    val showDatePicker = remember { mutableStateOf(false) }
    val showDateRangePicker = remember { mutableStateOf(false) }

    // In HomeScreen.kt, where DashboardHero is used
    val incomingTransactions by viewModel.incomingTransactions.collectAsState()

    // Scroll state for tracking
    val scrollState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Track the collapsed state of dashboard based on scroll position
    val isDashboardCollapsed = remember {
        derivedStateOf { scrollState.firstVisibleItemIndex > 0 || scrollState.firstVisibleItemScrollOffset > 100 }
    }

    // Collapsible filter section - INCREASED HEIGHT FROM 250dp to 350dp
    var filterSectionHeight by remember { mutableStateOf(0.dp) }
    var filterSectionExpanded by remember { mutableStateOf(false) }
    val maxHeight = remember { 350.dp } // FIXED: Increased from 250dp to accommodate all content
    val density = LocalDensity.current



    // Background color for the entire screen
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFF5F5F5)
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start
                        ) {
                            // Logo image
                            Image(
                                painter = painterResource(id = R.drawable.logo_kufay),
                                contentDescription = "Kufay Logo",
                                modifier = Modifier
                                    .height(80.dp)
                                    .padding(end = 8.dp)
                            )
                        }
                    },
                    actions = {
                        // Filter button
                        IconButton(onClick = {
                            showFilters.value = !showFilters.value
                            if (showFilters.value) {
                                filterSectionExpanded = true
                                filterSectionHeight = maxHeight
                            } else {
                                filterSectionExpanded = false
                                filterSectionHeight = 0.dp
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Outlined.FilterList,
                                contentDescription = "Filter",
                                tint = if (showFilters.value) MaterialTheme.colorScheme.primary else Color.Gray
                            )
                        }

                        IconButton(onClick = onNavigateToTrash) {
                            Icon(Icons.Default.Delete, contentDescription = "Trash")
                        }
                        IconButton(onClick = {
                            coroutineScope.launch {
                                scrollState.animateScrollToItem(0)
                            }
                        }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                        }
                        IconButton(onClick = onNavigateToSettings) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.White,
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            },
            containerColor = Color(0xFFF5F5F5)
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // This is the box that will stick to the top
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .zIndex(1f) // Makes sure it's above the scrollable content
                ) {
                    Column (
                        modifier = Modifier
                            .fillMaxWidth()
                            .zIndex(1f) // Makes sure it's above the scrollable content
                    )   {
                        // Dashboard hero that adapts based on scroll
                        // ✨ Dashboard hero avec SearchBar intégrée
                        DashboardHero(
                            modifier = Modifier.fillMaxWidth(),
                            appColor = appColor,
                            totalIncomingAmount = totalIncomingAmount,
                            incomingAmountByApp = incomingAmountByApp.mapKeys { (packageName, _) ->
                                val transactionForPackage = incomingTransactions.find { it.first.contains(packageName, ignoreCase = true) }
                                val title = transactionForPackage?.first ?: ""
                                getAppNameFromPackage(packageName, title)
                            },
                            dailyIncomingAmount = dailyIncomingAmount,
                            isCollapsed = isDashboardCollapsed.value,
                            incomingTransactions = incomingTransactions,
                            onReadDailyTotal = {
                                val totalText = if (dailyIncomingAmount != null)
                                    "Total du jour ${dailyIncomingAmount?.toLong() ?:0} franc CFA"
                                else
                                    "aucun encaissement"
                                viewModel.readDailyTotal(totalText)
                            },
                            // ✨ NOUVEAUX paramètres SearchBar
                            searchQuery = searchQuery,
                            onSearchQueryChange = viewModel::setSearchQuery,
                            notificationCount = notifications.size
                        )
                        // ✨ Ancien SearchBar SUPPRIMÉ (maintenant intégré dans DashboardHero)

                        // ✨ Filter section avec wrapContentHeight (s'adapte au contenu)
                        AnimatedVisibility(
                            visible = showFilters.value,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .wrapContentHeight() // ✨ S'adapte au contenu automatiquement
                                    .clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
                                    .background(Color(0xFFF0F0F5))
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.CenterHorizontally)
                                            .width(40.dp)
                                            .height(4.dp)
                                            .background(
                                                color = Color.Gray.copy(alpha = 0.5f),
                                                shape = RoundedCornerShape(2.dp)
                                            )
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    FilterSection(
                                        selectedAppTypes = selectedAppTypes,
                                        onAppTypesSelected = viewModel::setSelectedAppTypes,
                                        dateFilterType = dateFilterType,
                                        onDateFilterTypeSelected = { newType ->
                                            viewModel.setDateFilterType(newType)

                                            if (newType == DateFilterType.SINGLE_DAY && selectedDate == null) {
                                                viewModel.selectToday()
                                            }
                                            else if (newType == DateFilterType.DATE_RANGE && selectedDateRange == null) {
                                                viewModel.selectCurrentWeek()
                                            }
                                        },
                                        selectedDate = selectedDate,
                                        selectedDateRange = selectedDateRange,
                                        onDateSelected = { date ->
                                            showDatePicker.value = true
                                        },
                                        onDateRangeSelected = { range ->
                                            showDateRangePicker.value = true
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))
                                }
                            }
                        }
                    }
                }

                // Calculate the spacer height based on the header content
                // ✨ Hauteur TOTALE du header (dashboard + filtres si ouverts)
                val headerHeight by remember {
                    derivedStateOf {
                        val baseHeight = if (isDashboardCollapsed.value) 90.dp else 200.dp
                        if (showFilters.value) {
                            baseHeight + filterSectionHeight
                        } else {
                            baseHeight
                        }
                    }
                }

                // ✨ Hauteur FIXE de l'espace pub (ne change jamais)
                val adSpaceHeight = 75.dp

                // Banner Ad - descend avec le header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = headerHeight)  // ✅ Suit le header (dashboard + filtres)
                        .height(adSpaceHeight)  // ✅ Hauteur FIXE du Box pub
                        .zIndex(0.9f)
                ) {
                    BannerAd(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF5F5F5))
                            .height(75.dp)  // Hauteur réelle de la pub
                    )
                }

                // Main scrollable content - SUIT la pub naturellement
                LazyColumn(
                    state = scrollState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = headerHeight + adSpaceHeight),  // ✅ Header + espace pub
                    contentPadding = PaddingValues(top = 6.dp, bottom = 6.dp, start = 8.dp, end = 8.dp)
                ) {
                    // If no notifications, show empty state
                    if (notifications.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(300.dp)
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Notifications,
                                        contentDescription = null,
                                        modifier = Modifier.size(64.dp),
                                        tint = Color.LightGray
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "Aucune notification n'a été trouvée",
                                        style = MaterialTheme.typography.bodyLarge,
                                        textAlign = TextAlign.Center,
                                        color = Color.Gray
                                    )
                                    if (selectedAppTypes.isNotEmpty() || dateFilterType != DateFilterType.ALL_TIME) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Button(
                                            onClick = {
                                                viewModel.setSelectedAppTypes(emptySet())
                                                viewModel.setDateFilterType(DateFilterType.ALL_TIME)
                                                viewModel.setSelectedDate(null)
                                                viewModel.setSelectedDateRange(null)
                                            }
                                        ) {
                                            Text("Effacer les filtres")
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        // Show count and button to clear filters if any active
                        if (selectedAppTypes.isNotEmpty() || dateFilterType != DateFilterType.ALL_TIME) {
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Affichage de ${notifications.size} notifications",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.Gray
                                    )

                                    TextButton(
                                        onClick = {
                                            viewModel.setSelectedAppTypes(emptySet())
                                            viewModel.setDateFilterType(DateFilterType.ALL_TIME)
                                            viewModel.setSelectedDate(null)
                                            viewModel.setSelectedDateRange(null)
                                        }
                                    ) {
                                        Text("Effacer le(s) filtre(s)")
                                    }
                                }
                            }
                        }

                        // Notification items
                        items(notifications) { notification ->
                            val isReading = readingNotificationId.value == notification.id

                            NotificationCard(
                                notification = notification,
                                isReading = isReading,
                                onPlayPauseClick = {
                                    if (isReading) {
                                        viewModel.stopReading()
                                        readingNotificationId.value = null
                                    } else {
                                        viewModel.readNotification(notification)
                                        readingNotificationId.value = notification.id
                                    }
                                },
                                onDeleteClick = {
                                    viewModel.moveToTrash(notification)
                                },
                                onRestoreClick = {
                                    viewModel.restoreFromTrash(notification)
                                },
                                onCardClick = {
                                    selectedNotification.value = notification
                                }
                            )
                        }
                    }
                }
            }

            // FIXED: Move dialogs OUTSIDE the scrollable Box - place them at Scaffold level with high z-index
            // This ensures they appear on top of everything
            if (selectedNotification.value != null) {
                Box(modifier = Modifier.fillMaxSize().zIndex(10f)) {
                    selectedNotification.value?.let { notification ->
                        NotificationDetailDialog(
                            notification = notification,
                            onDismiss = { selectedNotification.value = null }
                        )
                    }
                }
            }

            // Date Picker Dialog for single day - FIXED: Moved outside scrollable content
            if (showDatePicker.value) {
                Box(modifier = Modifier.fillMaxSize().zIndex(10f)) {
                    SimpleDatePickerDialog(
                        onDismiss = { showDatePicker.value = false },
                        onDateSelected = { date ->
                            viewModel.setSelectedDate(date)
                            showDatePicker.value = false
                        }
                    )
                }
            }

            // Date Range Picker Dialog - FIXED: Moved outside scrollable content
            if (showDateRangePicker.value) {
                Box(modifier = Modifier.fillMaxSize().zIndex(10f)) {
                    SimpleDateRangePickerDialog(
                        onDismiss = { showDateRangePicker.value = false },
                        onDateRangeSelected = { startDate, endDate ->
                            viewModel.setSelectedDateRange(Pair(startDate, endDate))
                            showDateRangePicker.value = false
                        }
                    )
                }
            }
        }
    }
}

// Simple date picker dialog
@Composable
fun SimpleDatePickerDialog(
    onDismiss: () -> Unit,
    onDateSelected: (Long) -> Unit
) {
    val calendar = remember { Calendar.getInstance() }
    var year by remember { mutableStateOf(calendar.get(Calendar.YEAR)) }
    var month by remember { mutableStateOf(calendar.get(Calendar.MONTH)) }
    var day by remember { mutableStateOf(calendar.get(Calendar.DAY_OF_MONTH)) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    "Sélectionner une date",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // More compact date picker
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Day
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Jour", style = MaterialTheme.typography.bodySmall)
                        IconButton(onClick = {
                            if (day > 1) day-- else {
                                // Move to previous month
                                if (month > 0) month-- else {
                                    month = 11
                                    year--
                                }
                                // Set day to last day of the month
                                calendar.set(year, month, 1)
                                day = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
                            }
                        }) {
                            Icon(Icons.Default.Remove, contentDescription = "Previous Day")
                        }
                        Text(day.toString(), style = MaterialTheme.typography.titleMedium)
                        IconButton(onClick = {
                            // Calculate max days in current month
                            calendar.set(year, month, 1)
                            val maxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

                            if (day < maxDay) day++ else {
                                day = 1
                                if (month < 11) month++ else {
                                    month = 0
                                    year++
                                }
                            }
                        }) {
                            Icon(Icons.Default.Add, contentDescription = "Next Day")
                        }
                    }

                    // Month
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Mois", style = MaterialTheme.typography.bodySmall)
                        IconButton(onClick = {
                            if (month > 0) month-- else {
                                month = 11
                                year--
                            }
                        }) {
                            Icon(Icons.Default.Remove, contentDescription = "Previous Month")
                        }
                        Text(
                            getMonthName(month).substring(0, 3),
                            style = MaterialTheme.typography.titleMedium
                        )
                        IconButton(onClick = {
                            if (month < 11) month++ else {
                                month = 0
                                year++
                            }
                        }) {
                            Icon(Icons.Default.Add, contentDescription = "Next Month")
                        }
                    }

                    // Year
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Année", style = MaterialTheme.typography.bodySmall)
                        IconButton(onClick = { year-- }) {
                            Icon(Icons.Default.Remove, contentDescription = "Previous Year")
                        }
                        Text(year.toString(), style = MaterialTheme.typography.titleMedium)
                        IconButton(onClick = { year++ }) {
                            Icon(Icons.Default.Add, contentDescription = "Next Year")
                        }
                    }
                }

                // Show selected date
                val dateFormat = SimpleDateFormat("EEEE d MMMM yyyy", Locale.FRANCE)
                calendar.set(year, month, day)
                Text(
                    dateFormat.format(calendar.time),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(vertical = 8.dp)
                )

                // Buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Annuler")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            calendar.set(year, month, day)
                            onDateSelected(calendar.timeInMillis)
                        }
                    ) {
                        Text("OK")
                    }
                }
            }
        }
    }
}

// Simple date range picker
// Modern date range picker with quick select and calendar
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleDateRangePickerDialog(
    onDismiss: () -> Unit,
    onDateRangeSelected: (Long, Long) -> Unit
) {
    var startDate by remember {
        mutableStateOf(Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        }.timeInMillis)
    }
    var endDate by remember { mutableStateOf(Calendar.getInstance().timeInMillis) }
    var showCalendarFor by remember { mutableStateOf<String?>(null) } // "start" or "end"
    var selectedQuickButton by remember { mutableStateOf("this_week") }

    // Get theme colors
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceColor = MaterialTheme.colorScheme.surface

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = surfaceColor,
            tonalElevation = 3.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // Title
                Text(
                    text = "Sélectionner une période", // TODO: Use stringResource when strings added
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 20.dp)
                )

                // Date Input Fields
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // From Date
                    DateInputField(
                        label = "Du:",
                        date = startDate,
                        primaryColor = primaryColor,
                        onClick = { showCalendarFor = "start" }
                    )

                    // To Date
                    DateInputField(
                        label = "Au:",
                        date = endDate,
                        primaryColor = primaryColor,
                        onClick = { showCalendarFor = "end" }
                    )
                }

                // Quick Select Label
                Text(
                    text = "Sélection rapide :",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 20.dp, bottom = 12.dp)
                )

                // Quick Select Buttons
                QuickSelectButtons(
                    selectedButton = selectedQuickButton,
                    primaryColor = primaryColor,
                    onRangeSelected = { start, end, buttonId ->
                        startDate = start
                        endDate = end
                        selectedQuickButton = buttonId
                    }
                )

                // Selected Range Display
                SelectedRangeDisplay(
                    startDate = startDate,
                    endDate = endDate,
                    primaryColor = primaryColor,
                    modifier = Modifier.padding(top = 20.dp, bottom = 20.dp)
                )

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Annuler")
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Button(
                        onClick = {
                            if (startDate <= endDate) {
                                onDateRangeSelected(startDate, endDate)
                            } else {
                                onDateRangeSelected(endDate, startDate)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFDB913) // Kufay yellow
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Appliquer",
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }

    // Calendar Popup
    if (showCalendarFor != null) {
        MiniCalendarDialog(
            initialDate = if (showCalendarFor == "start") startDate else endDate,
            onDismiss = { showCalendarFor = null },
            onDateSelected = { selectedDate ->
                when (showCalendarFor) {
                    "start" -> {
                        startDate = selectedDate
                        if (startDate > endDate) endDate = startDate
                    }
                    "end" -> {
                        endDate = selectedDate
                        if (endDate < startDate) startDate = endDate
                    }
                }
                showCalendarFor = null
            }
        )
    }
}

// Date Input Field Component
@Composable
private fun DateInputField(
    label: String,
    date: Long,
    primaryColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(50.dp)
        )

        Surface(
            modifier = Modifier
                .weight(1f)
                .clickable(onClick = onClick),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(date)),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )

                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = "Calendar",
                    tint = primaryColor,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

// Quick Select Buttons
@Composable
private fun QuickSelectButtons(
    selectedButton: String,
    primaryColor: Color,
    onRangeSelected: (Long, Long, String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Row 1
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            QuickSelectButton(
                text = "Aujourd'hui",
                isSelected = selectedButton == "today",
                primaryColor = primaryColor,
                modifier = Modifier.weight(1f),
                onClick = {
                    val cal = Calendar.getInstance()
                    cal.set(Calendar.HOUR_OF_DAY, 0)
                    cal.set(Calendar.MINUTE, 0)
                    cal.set(Calendar.SECOND, 0)
                    val today = cal.timeInMillis
                    onRangeSelected(today, today, "today")
                }
            )

            QuickSelectButton(
                text = "7 derniers j",
                isSelected = selectedButton == "last_7d",
                primaryColor = primaryColor,
                modifier = Modifier.weight(1f),
                onClick = {
                    val cal = Calendar.getInstance()
                    val end = cal.timeInMillis
                    cal.add(Calendar.DAY_OF_YEAR, -6)
                    onRangeSelected(cal.timeInMillis, end, "last_7d")
                }
            )

            QuickSelectButton(
                text = "30 derniers j",
                isSelected = selectedButton == "last_30d",
                primaryColor = primaryColor,
                modifier = Modifier.weight(1f),
                onClick = {
                    val cal = Calendar.getInstance()
                    val end = cal.timeInMillis
                    cal.add(Calendar.DAY_OF_YEAR, -29)
                    onRangeSelected(cal.timeInMillis, end, "last_30d")
                }
            )
        }

        // Row 2
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            QuickSelectButton(
                text = "Cette semaine",
                isSelected = selectedButton == "this_week",
                primaryColor = primaryColor,
                modifier = Modifier.weight(1f),
                onClick = {
                    val cal = Calendar.getInstance()
                    val end = cal.timeInMillis
                    cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                    onRangeSelected(cal.timeInMillis, end, "this_week")
                }
            )

            QuickSelectButton(
                text = "Ce mois-ci",
                isSelected = selectedButton == "this_month",
                primaryColor = primaryColor,
                modifier = Modifier.weight(1f),
                onClick = {
                    val cal = Calendar.getInstance()
                    val end = cal.timeInMillis
                    cal.set(Calendar.DAY_OF_MONTH, 1)
                    onRangeSelected(cal.timeInMillis, end, "this_month")
                }
            )

            QuickSelectButton(
                text = "Cette année",
                isSelected = selectedButton == "this_year",
                primaryColor = primaryColor,
                modifier = Modifier.weight(1f),
                onClick = {
                    val cal = Calendar.getInstance()
                    val end = cal.timeInMillis
                    cal.set(Calendar.DAY_OF_YEAR, 1)
                    onRangeSelected(cal.timeInMillis, end, "this_year")
                }
            )
        }
    }
}

// Individual Quick Button
@Composable
private fun QuickSelectButton(
    text: String,
    isSelected: Boolean,
    primaryColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) primaryColor else Color.Transparent,
        border = BorderStroke(
            width = 1.5.dp,
            color = if (isSelected) primaryColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                fontSize = 12.sp
            )
        }
    }
}

// Selected Range Display
@Composable
private fun SelectedRangeDisplay(
    startDate: Long,
    endDate: Long,
    primaryColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = primaryColor.copy(alpha = 0.1f),
        border = BorderStroke(2.dp, primaryColor.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                text = "PÉRIODE SÉLECTIONNÉE",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = primaryColor,
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            val sdf = SimpleDateFormat("EEE d MMM yyyy", Locale.FRANCE)
            Text(
                text = "${sdf.format(Date(startDate))} → ${sdf.format(Date(endDate))}",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// Mini Calendar Dialog
@Composable
private fun MiniCalendarDialog(
    initialDate: Long,
    onDismiss: () -> Unit,
    onDateSelected: (Long) -> Unit
) {
    val cal = remember { Calendar.getInstance().apply { timeInMillis = initialDate } }
    var displayMonth by remember { mutableStateOf(cal.get(Calendar.MONTH)) }
    var displayYear by remember { mutableStateOf(cal.get(Calendar.YEAR)) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 3.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${getMonthName(displayMonth)} $displayYear",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Row {
                        IconButton(onClick = {
                            if (displayMonth == 0) {
                                displayMonth = 11
                                displayYear--
                            } else displayMonth--
                        }) {
                            Icon(Icons.Default.ChevronLeft, "Précédent")
                        }

                        IconButton(onClick = {
                            if (displayMonth == 11) {
                                displayMonth = 0
                                displayYear++
                            } else displayMonth++
                        }) {
                            Icon(Icons.Default.ChevronRight, "Suivant")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Day headers
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    listOf("Lu", "Ma", "Me", "Je", "Ve", "Sa", "Di").forEach {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.width(40.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Calendar grid
                CalendarGrid(displayMonth, displayYear, initialDate, onDateSelected)
            }
        }
    }
}

// Calendar Grid
@Composable
private fun CalendarGrid(
    month: Int,
    year: Int,
    selectedDate: Long,
    onDateSelected: (Long) -> Unit
) {
    val cal = Calendar.getInstance()
    cal.set(year, month, 1)
    val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
    val startOffset = if (firstDayOfWeek == Calendar.SUNDAY) 6 else firstDayOfWeek - 2
    val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

    val selectedCal = Calendar.getInstance().apply { timeInMillis = selectedDate }
    val today = Calendar.getInstance()

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        var dayCounter = 1 - startOffset

        repeat(6) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                repeat(7) {
                    if (dayCounter in 1..daysInMonth) {
                        val currentDay = dayCounter
                        cal.set(year, month, currentDay)

                        val isSelected = selectedCal.get(Calendar.YEAR) == year &&
                                selectedCal.get(Calendar.MONTH) == month &&
                                selectedCal.get(Calendar.DAY_OF_MONTH) == currentDay

                        val isToday = today.get(Calendar.YEAR) == year &&
                                today.get(Calendar.MONTH) == month &&
                                today.get(Calendar.DAY_OF_MONTH) == currentDay

                        CalendarDay(currentDay, isSelected, isToday) {
                            cal.set(year, month, currentDay)
                            cal.set(Calendar.HOUR_OF_DAY, 0)
                            cal.set(Calendar.MINUTE, 0)
                            cal.set(Calendar.SECOND, 0)
                            onDateSelected(cal.timeInMillis)
                        }
                    } else {
                        Spacer(Modifier.size(40.dp))
                    }
                    dayCounter++
                }
            }
        }
    }
}

// Calendar Day
@Composable
private fun CalendarDay(
    day: Int,
    isSelected: Boolean,
    isToday: Boolean,
    onClick: () -> Unit
) {
    val primaryColor = MaterialTheme.colorScheme.primary

    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(if (isSelected) primaryColor else Color.Transparent)
            .border(
                width = if (isToday && !isSelected) 2.dp else 0.dp,
                color = primaryColor,
                shape = CircleShape
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day.toString(),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
            color = when {
                isSelected -> Color.White
                isToday -> primaryColor
                else -> MaterialTheme.colorScheme.onSurface
            }
        )
    }
}  // ✅ FERMER ICI - C'EST CRUCIAL

// Helper function to get month name - VERSION CORRECTE
fun getMonthName(month: Int): String {
    return when (month) {
        0 -> "Janvier"
        1 -> "Février"
        2 -> "Mars"
        3 -> "Avril"
        4 -> "Mai"
        5 -> "Juin"
        6 -> "Juillet"
        7 -> "Août"
        8 -> "Septembre"
        9 -> "Octobre"
        10 -> "Novembre"
        11 -> "Décembre"
        else -> "Inconnu"
    }
}

@Composable
fun AdMobBanner(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    AndroidView(
        modifier = modifier,
        factory = { ctx: Context ->
            AdView(ctx).apply {
                setAdSize(AdSize.BANNER)
                adUnitId = "ca-app-pub-5150393955061751/5025492745"
                loadAd(AdRequest.Builder().build())
            }
        }
    )
}
