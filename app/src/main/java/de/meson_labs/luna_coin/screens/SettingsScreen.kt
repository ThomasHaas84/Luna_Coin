// screens/settings/SettingsScreen.kt
package de.meson_labs.luna_coin.screens.settings

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.meson_labs.luna_coin.R
import de.meson_labs.luna_coin.components.CoinDisplay
import de.meson_labs.luna_coin.components.LunaScreenHeader
import de.meson_labs.luna_coin.components.common.LogCard
import de.meson_labs.luna_coin.components.common.toDisplayText
import de.meson_labs.luna_coin.components.dialogs.CoinEditDialog
import de.meson_labs.luna_coin.components.dialogs.ConfirmationDialog
import de.meson_labs.luna_coin.components.dialogs.DogScheduleEditorDialog
import de.meson_labs.luna_coin.components.dialogs.LunaGifDialog
import de.meson_labs.luna_coin.components.dialogs.ShopEditorDialog
import de.meson_labs.luna_coin.components.dialogs.TaskEditorDialog
import de.meson_labs.luna_coin.models.Child
import de.meson_labs.luna_coin.models.DayOfWeekName
import de.meson_labs.luna_coin.models.LunaCoinData
import de.meson_labs.luna_coin.models.TaskAssignmentType
import de.meson_labs.luna_coin.models.TaskCompletionMode
import de.meson_labs.luna_coin.models.TaskItem
import de.meson_labs.luna_coin.models.TaskRepeatType
import de.meson_labs.luna_coin.models.UserRole
import de.meson_labs.luna_coin.viewmodel.LunaCoinViewModel
import kotlinx.coroutines.delay
import java.time.LocalDate

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    data: LunaCoinData,
    selectedChild: Child?,
    selectedDate: LocalDate,
    jsonText: String,
    viewModel: LunaCoinViewModel,

    onAddTask: (String, String, Int, TaskAssignmentType, TaskCompletionMode, TaskRepeatType, String?, String, String?, DayOfWeekName?, Boolean) -> Unit,
    onUpdateTask: (String, String, String, Int, TaskAssignmentType, TaskCompletionMode, TaskRepeatType, String?, String, String?, DayOfWeekName?, Boolean) -> Unit,
    onDeleteTask: (String) -> Unit,

    onAddShopItem: (String, String, Int) -> Unit,
    onUpdateShopItem: (String, String, String, Int) -> Unit,
    onDeleteShopItem: (String) -> Unit,

    onAddDogSchedule: (String, DayOfWeekName, String, String, String, String) -> Unit,
    onUpdateDogSchedule: (String, String, DayOfWeekName, String, String, String, String) -> Unit,
    onDeleteDogSchedule: (String) -> Unit,

    onUpdateChildCoins: (String, Int, String?) -> Unit,
    onUndoLogEntry: (String) -> Unit,

    onResetDemoData: () -> Unit,
    onCreateCloudBackup: () -> Unit,
    onRestoreFromBackup: () -> Unit,
    onImportFromJson: () -> Unit,

    onLogout: () -> Unit,
) {
    val currentMessage by viewModel.message.collectAsState()

    LaunchedEffect(currentMessage) {
        if (currentMessage != null) {
            println("💬 $currentMessage")
        }
    }

    // Dialog & Section States
    var showResetDialog by remember { mutableStateOf(false) }
    var showCreateBackupDialog by remember { mutableStateOf(false) }
    var showRestoreDialog by remember { mutableStateOf(false) }
    var showImportJsonDialog by remember { mutableStateOf(false) }

    var showUsersAndCoins by remember { mutableStateOf(false) }
    var showLogs by remember { mutableStateOf(false) }
    var showWatchlist by remember { mutableStateOf(true) }
    var showAppSettings by remember { mutableStateOf(false) }
    var showShopEditor by remember { mutableStateOf(false) }
    var showDogScheduleEditor by remember { mutableStateOf(false) }
    var showTaskEditor by remember { mutableStateOf(false) }

    // Language GIF
    var languageMessage by remember { mutableStateOf<String?>(null) }
    var showLanguageGif by remember { mutableStateOf(false) }
    var languageGifTitle by remember { mutableStateOf("") }
    var languageGifMessage by remember { mutableStateOf("") }
    var languageGifResId by remember { mutableIntStateOf(0) }

    // Coin Edit
    var childForCoinEdit by remember { mutableStateOf<Child?>(null) }
    var coinEditText by remember { mutableStateOf("") }
    var coinEditCommentText by remember { mutableStateOf("") }
    var coinEditError by remember { mutableStateOf<String?>(null) }

    // Sonstiges
    var logSearchText by remember { mutableStateOf("") }
    var mimiModeEnabled by remember { mutableStateOf(false) }

    // Auto-hide language message
    LaunchedEffect(languageMessage) {
        if (languageMessage != null) {
            delay(3000)
            languageMessage = null
        }
    }

    val canEdit = selectedChild?.role == UserRole.PARENT || selectedChild?.role == UserRole.ADMIN
    val isAdmin = selectedChild?.role == UserRole.ADMIN

    val visibleUsers = if (canEdit) data.children else data.children.filter { it.id == selectedChild?.id }

    val baseVisibleLogs = if (canEdit) data.logs else data.logs.filter { it.childId == selectedChild?.id }

    val visibleLogs = if (logSearchText.isBlank()) {
        baseVisibleLogs.take(200)
    } else {
        baseVisibleLogs.filter { log ->
            log.text.contains(logSearchText, ignoreCase = true) ||
                    log.timestamp.contains(logSearchText, ignoreCase = true)
        }
    }

    val watchlistTasks = data.tasks.filter { it.isWatchlist }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            item {
                LunaScreenHeader(
                    title = "Einstellungen",
                    selectedChild = selectedChild,
                    onLogout = onLogout
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Nachricht anzeigen
            if (currentMessage != null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Text(
                            text = currentMessage!!,
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }

            // App-Einstellungen
            item {
                Button(onClick = { showAppSettings = !showAppSettings }) {
                    Text(if (showAppSettings) "App-Einstellungen ausblenden" else "App-Einstellungen anzeigen")
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (showAppSettings) {
                item {
                    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("App-Einstellungen", style = MaterialTheme.typography.headlineSmall)

                            Spacer(modifier = Modifier.height(16.dp))

                            Text("Sprache:", style = MaterialTheme.typography.titleMedium)

                            Column {
                                TextButton(onClick = { languageMessage = "Deutsch wurde ausgewählt." }) { Text("Deutsch") }
                                TextButton(onClick = { languageMessage = "Englisch wurde noch nicht implementiert." }) { Text("Englisch") }
                                TextButton(onClick = {
                                    languageGifTitle = "Französisch"
                                    languageGifMessage = "Haha, gay..."
                                    languageGifResId = R.drawable.gay
                                    showLanguageGif = true
                                }) { Text("Französisch") }
                                TextButton(onClick = { languageMessage = "Qapla'!" }) { Text("Klingonisch") }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Switch(checked = mimiModeEnabled, onCheckedChange = { mimiModeEnabled = it })
                                Text("Mimi-Modus aktivieren", modifier = Modifier.padding(start = 12.dp))
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            // Benutzer & Coins
            item {
                if (canEdit) {
                    Button(onClick = { showUsersAndCoins = !showUsersAndCoins }) {
                        Text(if (showUsersAndCoins) "Benutzer & Coins ausblenden" else "Benutzer & Coins anzeigen")
                    }
                } else {
                    Text("Meine Coins", style = MaterialTheme.typography.headlineSmall)
                }
            }

            if (!canEdit || showUsersAndCoins) {
                items(visibleUsers) { child ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .combinedClickable(
                                onClick = {},
                                onLongClick = {
                                    if (canEdit) {
                                        childForCoinEdit = child
                                        coinEditText = child.coins.toString()
                                        coinEditCommentText = ""
                                        coinEditError = null
                                    }
                                }
                            ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("${child.name}: ", style = MaterialTheme.typography.bodyLarge)
                            CoinDisplay(amount = child.coins)
                            Text(
                                text = " · ${child.role.toDisplayText()}",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }

            // Watchlist
            if (canEdit) {
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text("Watchlist", style = MaterialTheme.typography.headlineSmall)
                    Spacer(modifier = Modifier.height(8.dp))

                    Button(onClick = { showWatchlist = !showWatchlist }) {
                        Text(if (showWatchlist) "Watchlist ausblenden" else "Watchlist anzeigen")
                    }
                }

                if (showWatchlist) {
                    if (watchlistTasks.isEmpty()) {
                        item {
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Text("Keine Aufgaben auf der Watchlist.", modifier = Modifier.padding(16.dp))
                            }
                        }
                    } else {
                        items(watchlistTasks) { task ->
                            WatchlistTaskCard(task = task, selectedDate = selectedDate, children = data.children)
                        }
                    }
                }
            }

            // Logs
            item {
                Spacer(modifier = Modifier.height(24.dp))
                if (canEdit) {
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Button(onClick = { showLogs = !showLogs }) {
                            Text(if (showLogs) "Logs ausblenden" else "Logs anzeigen")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedTextField(
                            value = logSearchText,
                            onValueChange = { logSearchText = it },
                            label = { Text("Logs suchen") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            enabled = showLogs
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                } else {
                    Text("Mein Log", style = MaterialTheme.typography.headlineSmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { showLogs = !showLogs }) {
                        Text(if (showLogs) "Mein Log ausblenden" else "Mein Log anzeigen")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            if (showLogs) {
                if (visibleLogs.isEmpty()) {
                    item {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Text("Noch keine Einträge vorhanden.", modifier = Modifier.padding(16.dp))
                        }
                    }
                } else {
                    items(visibleLogs) { log ->
                        LogCard(
                            log = log,
                            canUndo = canEdit,
                            onUndo = { onUndoLogEntry(log.id) }
                        )
                    }
                }
            }

            // Verwaltung (Aufgaben, Shop, Hundeplan) → für PARENT + ADMIN
            if (canEdit) {
                item {
                    Spacer(modifier = Modifier.height(32.dp))
                    Text("Verwaltung", style = MaterialTheme.typography.headlineSmall)
                    Spacer(modifier = Modifier.height(16.dp))

                    Button(onClick = { showTaskEditor = true }) {
                        Text("Aufgaben bearbeiten")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(onClick = { showShopEditor = true }) {
                        Text("Shop bearbeiten")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(onClick = { showDogScheduleEditor = true }) {
                        Text("Hundeplan bearbeiten")
                    }
                }
            }

            // Nur Admin: Datensicherung
            if (isAdmin) {
                item {
                    Spacer(modifier = Modifier.height(32.dp))
                    Text("Admin - Datensicherung", style = MaterialTheme.typography.headlineSmall)
                    Spacer(modifier = Modifier.height(16.dp))

                    Row {
                        OutlinedButton(onClick = { showResetDialog = true }) {
                            Text("Demo-Daten zurücksetzen")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedButton(onClick = { showCreateBackupDialog = true }) {
                            Text("Cloud-Backup erstellen")
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row {
                        OutlinedButton(onClick = { showRestoreDialog = true }) {
                            Text("Backup wiederherstellen")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedButton(onClick = { showImportJsonDialog = true }) {
                            Text("JSON importieren")
                        }
                    }
                }
            }
        }
    }

    // ==================== ZENTRALISIERTE DIALOGE ====================

    if (showResetDialog) {
        ConfirmationDialog(
            title = "Demo-Daten zurücksetzen?",
            message = "Alle aktuellen Daten werden durch Demo-Daten ersetzt. Dies kann nicht rückgängig gemacht werden.",
            confirmText = "Zurücksetzen",
            dismissText = "Abbrechen",
            onConfirm = {
                showResetDialog = false
                onResetDemoData()
            },
            onDismiss = { showResetDialog = false }
        )
    }

    if (showCreateBackupDialog) {
        ConfirmationDialog(
            title = "Cloud-Backup erstellen?",
            message = "Der aktuelle Stand wird in der Cloud gesichert.",
            confirmText = "Backup erstellen",
            dismissText = "Abbrechen",
            onConfirm = {
                showCreateBackupDialog = false
                onCreateCloudBackup()
            },
            onDismiss = { showCreateBackupDialog = false }
        )
    }

    if (showRestoreDialog) {
        ConfirmationDialog(
            title = "Backup wiederherstellen?",
            message = "Aktuelle Daten werden durch ein Backup ersetzt. Dies kann nicht rückgängig gemacht werden.",
            confirmText = "Wiederherstellen",
            dismissText = "Abbrechen",
            onConfirm = {
                showRestoreDialog = false
                onRestoreFromBackup()
            },
            onDismiss = { showRestoreDialog = false }
        )
    }

    if (showImportJsonDialog) {
        ConfirmationDialog(
            title = "Daten aus JSON importieren?",
            message = "Aktuelle Daten werden durch die ausgewählte Datei ersetzt. Dies kann nicht rückgängig gemacht werden.",
            confirmText = "Importieren",
            dismissText = "Abbrechen",
            onConfirm = {
                showImportJsonDialog = false
                onImportFromJson()
            },
            onDismiss = { showImportJsonDialog = false }
        )
    }

    // Language GIF Dialog
    if (showLanguageGif) {
        LunaGifDialog(
            title = languageGifTitle,
            message = languageGifMessage,
            gifResId = languageGifResId,
            contentDescription = languageGifTitle,
            onDismiss = { showLanguageGif = false }
        )
    }

    // Coin Edit Dialog
    childForCoinEdit?.let { child ->
        CoinEditDialog(
            child = child,
            coinText = coinEditText,
            commentText = coinEditCommentText,
            errorMessage = coinEditError,
            onCoinTextChange = {
                coinEditText = it.filterIndexed { index, char ->
                    char.isDigit() || (char == '-' && index == 0)
                }
                coinEditError = null
            },
            onCommentTextChange = { coinEditCommentText = it },
            onSave = { newCoins ->
                onUpdateChildCoins(child.id, newCoins, coinEditCommentText.trim().ifBlank { null })
                childForCoinEdit = null
            },
            onDismiss = {
                childForCoinEdit = null
                coinEditText = ""
                coinEditCommentText = ""
                coinEditError = null
            }
        )
    }

    if (showShopEditor) {
        ShopEditorDialog(
            items = data.shopItems,
            onDismiss = { showShopEditor = false },
            onAddShopItem = onAddShopItem,
            onUpdateShopItem = onUpdateShopItem,
            onDeleteShopItem = onDeleteShopItem
        )
    }

    if (showDogScheduleEditor) {
        DogScheduleEditorDialog(
            dogSchedule = data.dogSchedule,
            children = data.children,
            onDismiss = { showDogScheduleEditor = false },
            onAddDogSchedule = onAddDogSchedule,
            onUpdateDogSchedule = onUpdateDogSchedule,
            onDeleteDogSchedule = onDeleteDogSchedule
        )
    }

    if (showTaskEditor) {
        TaskEditorDialog(
            tasks = data.tasks,
            children = data.children,
            onDismiss = { showTaskEditor = false },
            onAddTask = onAddTask,
            onUpdateTask = onUpdateTask,
            onDeleteTask = onDeleteTask
        )
    }
}

@Composable
private fun WatchlistTaskCard(
    task: TaskItem,
    selectedDate: LocalDate,
    children: List<Child>
) {
    val selectedDateText = selectedDate.toString()
    val isDoneToday = when (task.completionMode) {
        TaskCompletionMode.ONCE_TOTAL -> task.completions.any { it.date == selectedDateText }
        TaskCompletionMode.EACH_PERSON -> {
            val relevantChildren = if (task.assignmentType == TaskAssignmentType.ASSIGNED && task.assignedChildId != null) {
                children.filter { it.id == task.assignedChildId }
            } else {
                children.filter { it.role == UserRole.CHILD }
            }
            relevantChildren.isNotEmpty() && relevantChildren.all { child ->
                task.completions.any { it.childId == child.id && it.date == selectedDateText }
            }
        }
    }

    val assignedName = task.assignedChildId?.let { id ->
        children.firstOrNull { it.id == id }?.name
    }

    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = if (isDoneToday) "✅ ${task.title}" else "⬜ ${task.title}",
                style = MaterialTheme.typography.titleMedium
            )
            if (task.description.isNotBlank()) {
                Text(
                    task.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = buildString {
                    append(task.rewardCoins)
                    append(" Coins")
                    assignedName?.let { append(" · $it") }
                    append(" · ${if (isDoneToday) "Heute erledigt" else "Heute noch offen"}")
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}