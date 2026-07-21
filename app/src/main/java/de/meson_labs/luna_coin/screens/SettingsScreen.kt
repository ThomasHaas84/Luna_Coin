// screens/settings/SettingsScreen.kt
package de.meson_labs.luna_coin.screens.settings

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import de.meson_labs.luna_coin.R
import de.meson_labs.luna_coin.components.CoinDisplay
import de.meson_labs.luna_coin.components.LunaScreenHeader
import de.meson_labs.luna_coin.components.common.LogCard
import de.meson_labs.luna_coin.components.common.toDisplayText
import de.meson_labs.luna_coin.components.dialogs.ConfirmationDialog
import de.meson_labs.luna_coin.components.dialogs.CoinTransferDialog
import de.meson_labs.luna_coin.components.dialogs.DogPlanEditorDialog
import de.meson_labs.luna_coin.components.dialogs.LunaGifDialog
import de.meson_labs.luna_coin.components.dialogs.ShopEditorDialog
import de.meson_labs.luna_coin.components.dialogs.TaskEditorDialog
import de.meson_labs.luna_coin.models.Child
import de.meson_labs.luna_coin.models.DayOfWeekName
import de.meson_labs.luna_coin.models.LunaCoinData
import de.meson_labs.luna_coin.models.LunaGameType
import de.meson_labs.luna_coin.models.TaskAssignmentType
import de.meson_labs.luna_coin.models.TaskCompletionMode
import de.meson_labs.luna_coin.models.TaskItem
import de.meson_labs.luna_coin.models.TaskRepeatType
import de.meson_labs.luna_coin.models.UserRole
import de.meson_labs.luna_coin.manager.ProgressManager
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

    onAddShopItem: (String, String, Int, Int) -> Unit,
    onUpdateShopItem: (String, String, String, Int, Int) -> Unit,
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

    autoWeeklyHighscoreResetEnabled: Boolean = false,
    lastAutomaticHighscoreResetDate: String? = null,
    onAutoWeeklyHighscoreResetChanged: (Boolean) -> Unit = {},
    onResetMemoryHighscores: () -> Unit = {},
    onResetNumberGuessHighscores: () -> Unit = {},
    onResetMultiplicationHighscores: () -> Unit = {},
    onResetWordGuessHighscores: () -> Unit = {},
    onResetAllHighscores: () -> Unit = {},

    onKlingonModeChanged: (Boolean) -> Unit = {},
    onLogout: () -> Unit,
) {
    val currentMessage by viewModel.message.collectAsState()

    val context = LocalContext.current
    val navigationPreferences = remember(context) {
        context.getSharedPreferences(
            NAVIGATION_PREFERENCES_NAME,
            android.content.Context.MODE_PRIVATE
        )
    }
    var selectedLoginDestination by remember {
        mutableStateOf(
            LoginDestination.fromStoredValue(
                navigationPreferences.getString(
                    NAVIGATION_DESTINATION_KEY,
                    LoginDestination.HOUSEHOLD_TASKS.storedValue
                )
            )
        )
    }

    val configuration = LocalConfiguration.current
    val isTabletLayout = configuration.smallestScreenWidthDp >= 600
    val isPhone = !isTabletLayout

    val screenPadding = if (isPhone) {
        14.dp
    } else {
        24.dp
    }

    val smallSpacerHeight = if (isPhone) {
        6.dp
    } else {
        8.dp
    }

    val sectionSpacerHeight = if (isPhone) {
        18.dp
    } else {
        24.dp
    }

    LaunchedEffect(currentMessage) {
        if (currentMessage != null) {
            println("💬 $currentMessage")
        }
    }

    var showResetDialog by remember { mutableStateOf(false) }
    var showCreateBackupDialog by remember { mutableStateOf(false) }
    var showRestoreDialog by remember { mutableStateOf(false) }
    var showImportJsonDialog by remember { mutableStateOf(false) }

    var showUsersAndCoins by remember { mutableStateOf(false) }
    var showCoinTransferDialog by remember { mutableStateOf(false) }
    var showLogs by remember { mutableStateOf(false) }
    var showWatchlist by remember { mutableStateOf(true) }
    var showAppSettings by remember { mutableStateOf(false) }
    var showShopEditor by remember { mutableStateOf(false) }
    var showDogPlanEditor by remember { mutableStateOf(false) }
    var showTaskEditor by remember { mutableStateOf(false) }
    var showAdminBackup by remember { mutableStateOf(false) }
    var showGameSettings by remember { mutableStateOf(false) }
    var pendingHighscoreReset by remember { mutableStateOf<HighscoreResetTarget?>(null) }

    var showUserEditorDialog by remember { mutableStateOf(false) }
    var userForEdit by remember { mutableStateOf<Child?>(null) }
    var userForDelete by remember { mutableStateOf<Child?>(null) }

    var languageMessage by remember { mutableStateOf<String?>(null) }
    var showLanguageGif by remember { mutableStateOf(false) }
    var languageGifTitle by remember { mutableStateOf("") }
    var languageGifMessage by remember { mutableStateOf("") }
    var languageGifResId by remember { mutableIntStateOf(0) }
    var klingonModeEnabled by remember { mutableStateOf(false) }

    var childForProgressEdit by remember { mutableStateOf<Child?>(null) }

    var logSearchText by remember { mutableStateOf("") }
    var mimiModeEnabled by remember { mutableStateOf(false) }

    LaunchedEffect(languageMessage) {
        if (languageMessage != null) {
            delay(3000)
            languageMessage = null
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            onKlingonModeChanged(false)
        }
    }

    fun disableKlingonMode() {
        klingonModeEnabled = false
        onKlingonModeChanged(false)
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

    val watchlistTasks = data.tasks.filter { it.watchlist }

    val memoryRecordHolders = getGameRecordHolderText(
        data = data,
        game = LunaGameType.MEMORY
    )
    val numberGuessRecordHolders = getGameRecordHolderText(
        data = data,
        game = LunaGameType.NUMBER_GUESS
    )
    val multiplicationRecordHolders = getGameRecordHolderText(
        data = data,
        game = LunaGameType.MULTIPLICATION
    )
    val wordGuessRecordHolders = getGameRecordHolderText(
        data = data,
        game = LunaGameType.WORD_GUESS
    )

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(screenPadding)
        ) {
            item {
                LunaScreenHeader(
                    title = if (klingonModeEnabled) "SeHlaw" else "Einstellungen",
                    selectedChild = selectedChild,
                    onLogout = onLogout
                )
                Spacer(modifier = Modifier.height(sectionSpacerHeight))
            }

            if (currentMessage != null) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Text(
                            text = currentMessage!!,
                            modifier = Modifier.padding(if (isPhone) 12.dp else 16.dp),
                            style = if (isPhone) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }

            item {
                SettingsSectionHeader(
                    title = if (klingonModeEnabled) "SeHlaw" else "App-Einstellungen",
                    isPhone = isPhone
                )

                Button(
                    onClick = { showAppSettings = !showAppSettings },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        if (klingonModeEnabled) {
                            if (showAppSettings) "SeHlaw So'." else "SeHlaw cha'."
                        } else {
                            if (showAppSettings) "App-Einstellungen ausblenden" else "App-Einstellungen anzeigen"
                        }
                    )
                }
                Spacer(modifier = Modifier.height(smallSpacerHeight))
            }

            if (showAppSettings) {
                item {
                    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = if (klingonModeEnabled) "Hol:" else "Sprache:",
                                style = MaterialTheme.typography.titleMedium
                            )

                            Column {
                                TextButton(onClick = {
                                    disableKlingonMode()
                                    languageMessage = "Deutsch wurde ausgewählt."
                                }) {
                                    Text(if (klingonModeEnabled) "DIvI' HolHa'" else "Deutsch")
                                }

                                TextButton(onClick = {
                                    disableKlingonMode()
                                    languageMessage = "Diese Sprache wurde noch nicht implementiert."
                                }) {
                                    Text(if (klingonModeEnabled) "DIvI' Hol" else "Englisch")
                                }

                                TextButton(onClick = {
                                    disableKlingonMode()
                                    languageGifTitle = "Französisch"
                                    languageGifMessage = "Haha, gay..."
                                    languageGifResId = R.drawable.gay
                                    showLanguageGif = true
                                }) {
                                    Text(if (klingonModeEnabled) "vIraS Hol" else "Französisch")
                                }

                                TextButton(onClick = {
                                    klingonModeEnabled = true
                                    onKlingonModeChanged(true)
                                    languageMessage = "Qapla'"
                                }) {
                                    Text(if (klingonModeEnabled) "tlhIngan Hol" else "Klingonisch")
                                }
                            }

                            if (languageMessage != null) {
                                Spacer(modifier = Modifier.height(8.dp))

                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer
                                    )
                                ) {
                                    Text(
                                        text = languageMessage!!,
                                        modifier = Modifier.padding(12.dp),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(if (isPhone) 12.dp else 16.dp))

                            HorizontalDivider()

                            Spacer(modifier = Modifier.height(if (isPhone) 12.dp else 16.dp))

                            Text(
                                text = if (klingonModeEnabled) "ghoS:" else "Navigation:",
                                style = MaterialTheme.typography.titleMedium
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = if (klingonModeEnabled) {
                                    "login DI' nuqDaq ghoS 'e' wIv."
                                } else {
                                    "Wähle aus, welcher Bereich nach dem Login geöffnet wird."
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            NavigationDestinationOption(
                                text = if (klingonModeEnabled) "juH Qu'mey" else "Haushaltsaufgaben",
                                selected = selectedLoginDestination == LoginDestination.HOUSEHOLD_TASKS,
                                onClick = {
                                    selectedLoginDestination = LoginDestination.HOUSEHOLD_TASKS
                                    navigationPreferences.edit()
                                        .putString(
                                            NAVIGATION_DESTINATION_KEY,
                                            LoginDestination.HOUSEHOLD_TASKS.storedValue
                                        )
                                        .apply()
                                }
                            )

                            NavigationDestinationOption(
                                text = if (klingonModeEnabled) "targh nab" else "Hundeplan",
                                selected = selectedLoginDestination == LoginDestination.DOG_PLAN,
                                onClick = {
                                    selectedLoginDestination = LoginDestination.DOG_PLAN
                                    navigationPreferences.edit()
                                        .putString(
                                            NAVIGATION_DESTINATION_KEY,
                                            LoginDestination.DOG_PLAN.storedValue
                                        )
                                        .apply()
                                }
                            )

                            Spacer(modifier = Modifier.height(if (isPhone) 12.dp else 16.dp))

                            HorizontalDivider()

                            Spacer(modifier = Modifier.height(if (isPhone) 12.dp else 16.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Switch(checked = mimiModeEnabled, onCheckedChange = { mimiModeEnabled = it })
                                Text(
                                    text = if (klingonModeEnabled) "Mimi SeH chu'" else "Mimi-Modus aktivieren",
                                    modifier = Modifier.padding(start = 12.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = if (klingonModeEnabled) {
                                    "Mimi SeHlaw vaj wovmoHghach chuS je tInHa'moH. 'ej not mevbogh Deghmey? chel."
                                } else {
                                    "Der Mimi-Modus stellt die Bildschirmhelligkeit und die Lautstärke der App herab.\nAußerdem werden ständig zufällig-passivaggressive Fragezeichen eingefügt?"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(sectionSpacerHeight))
                }
            }

            item {
                SettingsSectionHeader(
                    title = if (canEdit) {
                        if (klingonModeEnabled) "lo'wI'pu' DeQmey je" else "Benutzer & Coins"
                    } else {
                        if (klingonModeEnabled) "DeQmeywIj" else "Meine Coins"
                    },
                    isPhone = isPhone
                )

                if (canEdit) {
                    Button(
                        onClick = { showUsersAndCoins = !showUsersAndCoins },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (showUsersAndCoins) "Benutzer & Coins ausblenden" else "Benutzer & Coins anzeigen")
                    }
                }
            }

            if (!canEdit || showUsersAndCoins) {
                if (isAdmin) {
                    item {
                        Spacer(modifier = Modifier.height(if (isPhone) 10.dp else 12.dp))

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "Benutzerverwaltung",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Button(
                                    onClick = {
                                        userForEdit = null
                                        showUserEditorDialog = true
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("👤 + Benutzer hinzufügen")
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(smallSpacerHeight))
                    }
                }

                items(visibleUsers) { child ->
                    if (!canEdit && child.role == UserRole.CHILD) {
                        ChildCoinCard(
                            coins = child.coins
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = { showCoinTransferDialog = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Image(
                                painter = painterResource(R.drawable.luna_coin_small),
                                contentDescription = null,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Luna Coins senden")
                        }
                    } else {
                        UserManagementCard(
                            child = child,
                            canEditCoins = canEdit,
                            canManageUsers = isAdmin,
                            onEditCoins = {
                                childForProgressEdit = child
                            },
                            onEditUser = {
                                userForEdit = child
                                showUserEditorDialog = true
                            },
                            onDeleteUser = {
                                userForDelete = child
                            }
                        )
                    }
                }
            }

            if (canEdit) {
                item {
                    SettingsSectionHeader(
                        title = if (klingonModeEnabled) "bej tetlh" else "Watchlist",
                        isPhone = isPhone
                    )

                    Button(
                        onClick = { showWatchlist = !showWatchlist },
                        modifier = Modifier.fillMaxWidth()
                    ) {
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

            item {
                SettingsSectionHeader(
                    title = if (canEdit) {
                        if (klingonModeEnabled) "QonoS" else "Protokoll"
                    } else {
                        if (klingonModeEnabled) "QonoSwIj" else "Mein Log"
                    },
                    isPhone = isPhone
                )

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
                    Spacer(modifier = Modifier.height(smallSpacerHeight))
                } else {
                    Button(
                        onClick = { showLogs = !showLogs },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (showLogs) "Mein Log ausblenden" else "Mein Log anzeigen")
                    }
                    Spacer(modifier = Modifier.height(smallSpacerHeight))
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

            if (canEdit) {
                item {
                    SettingsSectionHeader(
                        title = if (klingonModeEnabled) "Quj" else "Spiele",
                        isPhone = isPhone,
                        largeTopSpacing = true
                    )

                    Button(
                        onClick = { showGameSettings = !showGameSettings },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            if (showGameSettings) {
                                "Spiele-Einstellungen ausblenden"
                            } else {
                                "Spiele-Einstellungen anzeigen"
                            }
                        )
                    }

                    if (showGameSettings) {
                        Spacer(modifier = Modifier.height(if (isPhone) 12.dp else 16.dp))

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Automatische wöchentliche Rücksetzung",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )

                                        Text(
                                            text = "Beim ersten App-Start einer neuen Kalenderwoche werden alle Highscores zurückgesetzt.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Switch(
                                        checked = autoWeeklyHighscoreResetEnabled,
                                        onCheckedChange = onAutoWeeklyHighscoreResetChanged
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    text = "Letzte automatische Rücksetzung:",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )

                                Text(
                                    text = lastAutomaticHighscoreResetDate ?: "Noch keine",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(16.dp))
                                HorizontalDivider()
                                Spacer(modifier = Modifier.height(16.dp))

                                Text(
                                    text = "Highscores manuell zurücksetzen",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                HighscoreResetButton(
                                    title = "Memory zurücksetzen",
                                    recordHolderText = memoryRecordHolders,
                                    onClick = {
                                        pendingHighscoreReset = HighscoreResetTarget.MEMORY
                                    }
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                HighscoreResetButton(
                                    title = "Zahlenraten zurücksetzen",
                                    recordHolderText = numberGuessRecordHolders,
                                    onClick = {
                                        pendingHighscoreReset = HighscoreResetTarget.NUMBER_GUESS
                                    }
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                HighscoreResetButton(
                                    title = "Einmaleins zurücksetzen",
                                    recordHolderText = multiplicationRecordHolders,
                                    onClick = {
                                        pendingHighscoreReset = HighscoreResetTarget.MULTIPLICATION
                                    }
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                HighscoreResetButton(
                                    title = "Wort-Raten zurücksetzen",
                                    recordHolderText = wordGuessRecordHolders,
                                    onClick = {
                                        pendingHighscoreReset = HighscoreResetTarget.WORD_GUESS
                                    }
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Button(
                                    onClick = { pendingHighscoreReset = HighscoreResetTarget.ALL },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.error,
                                        contentColor = MaterialTheme.colorScheme.onError
                                    )
                                ) {
                                    Text("Alle Highscores zurücksetzen")
                                }
                            }
                        }
                    }
                }
            }

            if (canEdit) {
                item {
                    SettingsSectionHeader(
                        title = if (klingonModeEnabled) "loH" else "Verwaltung",
                        isPhone = isPhone,
                        largeTopSpacing = true
                    )

                    Button(
                        onClick = { showTaskEditor = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (klingonModeEnabled) "✅ Qu\'mey choH" else "✅ Aufgaben bearbeiten")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = { showShopEditor = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (klingonModeEnabled) "🛒 Suy choH" else "🛒 Shop bearbeiten")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = { showDogPlanEditor = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (klingonModeEnabled) "🐶 targh nab choH" else "🐶 Hundeplan bearbeiten")
                    }
                }
            }

            if (isAdmin) {
                item {
                    SettingsSectionHeader(
                        title = "Admin – Datensicherung",
                        isPhone = isPhone,
                        largeTopSpacing = true
                    )

                    OutlinedButton(
                        onClick = { showAdminBackup = !showAdminBackup },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            if (showAdminBackup) {
                                "Admin - Datensicherung ausblenden"
                            } else {
                                "Admin - Datensicherung anzeigen"
                            }
                        )
                    }

                    if (showAdminBackup) {
                        Spacer(modifier = Modifier.height(if (isPhone) 12.dp else 16.dp))

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                OutlinedButton(
                                    onClick = { showCreateBackupDialog = true },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Cloud-Backup erstellen")
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedButton(
                                    onClick = { showRestoreDialog = true },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Cloud-Backup wiederherstellen")
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedButton(
                                    onClick = { showResetDialog = true },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Demo-Daten zurücksetzen")
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedButton(
                                    onClick = { showImportJsonDialog = true },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("JSON importieren")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    pendingHighscoreReset?.let { resetTarget ->
        ConfirmationDialog(
            title = resetTarget.dialogTitle,
            message = resetTarget.dialogMessage,
            confirmText = "Zurücksetzen",
            dismissText = "Abbrechen",
            onConfirm = {
                when (resetTarget) {
                    HighscoreResetTarget.MEMORY -> onResetMemoryHighscores()
                    HighscoreResetTarget.NUMBER_GUESS -> onResetNumberGuessHighscores()
                    HighscoreResetTarget.MULTIPLICATION -> onResetMultiplicationHighscores()
                    HighscoreResetTarget.WORD_GUESS -> onResetWordGuessHighscores()
                    HighscoreResetTarget.ALL -> onResetAllHighscores()
                }
                pendingHighscoreReset = null
            },
            onDismiss = {
                pendingHighscoreReset = null
            }
        )
    }

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

    if (showLanguageGif) {
        LunaGifDialog(
            title = languageGifTitle,
            message = languageGifMessage,
            gifResId = languageGifResId,
            contentDescription = languageGifTitle,
            onDismiss = { showLanguageGif = false }
        )
    }

    childForProgressEdit?.let { child ->
        ProgressEditDialog(
            child = child,
            onDismiss = {
                childForProgressEdit = null
            },
            onSave = { coins, experience, availableSkillPoints, intelligence, strength, agility, comment ->
                viewModel.updateChildProgressAsAdmin(
                    childId = child.id,
                    coins = coins,
                    experience = experience,
                    availableSkillPoints = availableSkillPoints,
                    intelligence = intelligence,
                    strength = strength,
                    agility = agility,
                    comment = comment
                )

                childForProgressEdit = null
            }
        )
    }

    if (showUserEditorDialog) {
        UserEditorDialog(
            child = userForEdit,
            onDismiss = {
                showUserEditorDialog = false
                userForEdit = null
            },
            onSaveNew = { name, role, password, age, coins, passwordRequired, allowRememberLogin ->
                viewModel.addChild(
                    name = name,
                    role = role,
                    password = password,
                    age = age,
                    coins = coins,
                    passwordRequired = passwordRequired,
                    allowRememberLogin = allowRememberLogin
                )
                showUserEditorDialog = false
                userForEdit = null
            },
            onSaveExisting = { updatedChild ->
                viewModel.updateChild(updatedChild)
                showUserEditorDialog = false
                userForEdit = null
            }
        )
    }

    userForDelete?.let { child ->
        ConfirmationDialog(
            title = "Benutzer löschen?",
            message = "Soll ${child.name} wirklich gelöscht werden? Zugewiesene Aufgaben werden freigegeben, Hundeplan-Einträge und Spielstände dieses Benutzers werden entfernt.",
            confirmText = "Löschen",
            dismissText = "Abbrechen",
            onConfirm = {
                viewModel.deleteChild(child.id)
                userForDelete = null
            },
            onDismiss = {
                userForDelete = null
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

    if (showDogPlanEditor) {
        DogPlanEditorDialog(
            templates = data.dogPlan.templates,
            onDismiss = { showDogPlanEditor = false },
            onSaveTemplate = { template ->
                viewModel.saveDogPlanTaskTemplate(template)
            },
            onDeleteTemplate = { templateId ->
                viewModel.deleteDogPlanTaskTemplate(templateId)
            }
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

    if (showCoinTransferDialog && selectedChild != null) {
        CoinTransferDialog(
            sender = selectedChild,
            recipients = data.children.filter { it.id != selectedChild.id },
            onDismiss = { showCoinTransferDialog = false },
            onSend = { recipientId, amount, comment, onResult ->
                viewModel.transferCoins(selectedChild.id, recipientId, amount, comment) { success, _ ->
                    onResult(success)
                }
            }
        )
    }
}

private const val NAVIGATION_PREFERENCES_NAME = "luna_navigation_preferences"
private const val NAVIGATION_DESTINATION_KEY = "login_destination"

private enum class LoginDestination(
    val storedValue: String
) {
    HOUSEHOLD_TASKS("household_tasks"),
    DOG_PLAN("dog_plan");

    companion object {
        fun fromStoredValue(value: String?): LoginDestination {
            return entries.firstOrNull { it.storedValue == value } ?: HOUSEHOLD_TASKS
        }
    }
}

@Composable
private fun NavigationDestinationOption(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick
        )

        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
private fun HighscoreResetButton(
    title: String,
    recordHolderText: String,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(3.dp))

            Text(
                text = recordHolderText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

private fun getGameRecordHolderText(
    data: LunaCoinData,
    game: LunaGameType
): String {
    val holderNames = data.gameHighscores
        .asSequence()
        .filter { highscore -> highscore.game == game }
        .mapNotNull { highscore ->
            data.children.firstOrNull { child ->
                child.id == highscore.childId
            }?.name?.trim()
        }
        .filter { name -> name.isNotBlank() }
        .distinct()
        .sorted()
        .toList()

    return when {
        holderNames.isEmpty() -> "🏆 Noch kein Rekord"
        holderNames.size == 1 -> "🏆 Rekordhalter: ${holderNames.first()}"
        else -> "🏆 Rekordhalter: ${holderNames.joinToString(", ")}"
    }
}

private enum class HighscoreResetTarget(
    val dialogTitle: String,
    val dialogMessage: String
) {
    MEMORY(
        dialogTitle = "Memory-Highscores zurücksetzen?",
        dialogMessage = "Alle gespeicherten Memory-Highscores werden gelöscht. Dies kann nicht rückgängig gemacht werden."
    ),
    NUMBER_GUESS(
        dialogTitle = "Zahlenraten-Highscores zurücksetzen?",
        dialogMessage = "Alle gespeicherten Zahlenraten-Highscores werden gelöscht. Dies kann nicht rückgängig gemacht werden."
    ),
    MULTIPLICATION(
        dialogTitle = "Einmaleins-Highscores zurücksetzen?",
        dialogMessage = "Alle gespeicherten Einmaleins-Highscores werden gelöscht. Dies kann nicht rückgängig gemacht werden."
    ),
    WORD_GUESS(
        dialogTitle = "Wort-Raten-Highscores zurücksetzen?",
        dialogMessage = "Alle gespeicherten Wort-Raten-Highscores werden gelöscht. Dies kann nicht rückgängig gemacht werden."
    ),
    ALL(
        dialogTitle = "Alle Highscores zurücksetzen?",
        dialogMessage = "Alle Highscores aller Spiele werden gelöscht. Dies kann nicht rückgängig gemacht werden."
    )
}

@Composable
private fun SettingsSectionHeader(
    title: String,
    isPhone: Boolean,
    largeTopSpacing: Boolean = false
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                top = when {
                    largeTopSpacing && isPhone -> 24.dp
                    largeTopSpacing -> 32.dp
                    isPhone -> 18.dp
                    else -> 24.dp
                },
                bottom = if (isPhone) 10.dp else 14.dp
            )
    ) {
        Text(
            text = title,
            style = if (isPhone) {
                MaterialTheme.typography.titleLarge
            } else {
                MaterialTheme.typography.headlineSmall
            },
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(if (isPhone) 6.dp else 8.dp))

        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant
        )
    }
}

@Composable
private fun ChildCoinCard(
    coins: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        LargeCoinAmountDisplay(
            amount = coins,
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun UserManagementCard(
    child: Child,
    canEditCoins: Boolean,
    canManageUsers: Boolean,
    onEditCoins: () -> Unit,
    onEditUser: () -> Unit,
    onDeleteUser: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .combinedClickable(
                onClick = {},
                onLongClick = {
                    if (canEditCoins) {
                        onEditCoins()
                    }
                }
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("${child.name}: ", style = MaterialTheme.typography.bodyLarge)

                CoinDisplay(amount = child.coins)

                Text(
                    text = " · ${child.role.toDisplayText()}",
                    style = MaterialTheme.typography.bodyLarge
                )

                if (child.isBuiltInAdmin) {
                    Text(
                        text = " · Standard-Admin",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (canManageUsers) {
                Text(
                    text = "Level ${child.level} · EP ${child.experience} · Skillpunkte ${child.availableSkillPoints} · 🧠 ${child.intelligence} · 💪 ${child.strength} · ⚡ ${child.agility}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Text(
                    text = buildString {
                        append("Alter: ${child.age}")
                        append(" · Passwort: ")
                        append(
                            if (!child.passwordRequired || child.password.isBlank()) {
                                "nicht erforderlich"
                            } else {
                                "erforderlich"
                            }
                        )
                        append(" · Merken: ")
                        append(if (child.allowRememberLogin) "erlaubt" else "nicht erlaubt")
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            if (canManageUsers) {
                Spacer(modifier = Modifier.height(8.dp))

                val isPhone = LocalConfiguration.current.smallestScreenWidthDp < 600

                if (isPhone) {
                    Column {
                        OutlinedButton(
                            onClick = onEditCoins,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("🎮 Fortschritt", maxLines = 1)
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        OutlinedButton(
                            onClick = onEditUser,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Bearbeiten", maxLines = 1)
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        OutlinedButton(
                            onClick = onDeleteUser,
                            enabled = !child.isBuiltInAdmin,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("Löschen", maxLines = 1)
                        }
                    }
                } else {
                    Row {
                        OutlinedButton(onClick = onEditCoins) {
                            Text("🎮 Fortschritt", maxLines = 1)
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        OutlinedButton(onClick = onEditUser) {
                            Text("Bearbeiten", maxLines = 1)
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        OutlinedButton(
                            onClick = onDeleteUser,
                            enabled = !child.isBuiltInAdmin,
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("Löschen", maxLines = 1)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LargeCoinAmountDisplay(
    amount: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.luna_coin_small),
            contentDescription = "Luna Coin",
            modifier = Modifier.size(200.dp)
        )

        Text(
            text = amount.toString(),
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.tertiary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}


@Composable
private fun ProgressEditDialog(
    child: Child,
    onDismiss: () -> Unit,
    onSave: (
        coins: Int,
        experience: Int,
        availableSkillPoints: Int,
        intelligence: Int,
        strength: Int,
        agility: Int,
        comment: String?
    ) -> Unit
) {
    var coinsText by remember(child.id) { mutableStateOf(child.coins.toString()) }
    var experienceText by remember(child.id) { mutableStateOf(child.experience.toString()) }
    var skillPointsText by remember(child.id) { mutableStateOf(child.availableSkillPoints.toString()) }

    var intelligence by remember(child.id) { mutableIntStateOf(child.intelligence.coerceIn(1, 100)) }
    var strength by remember(child.id) { mutableIntStateOf(child.strength.coerceIn(1, 100)) }
    var agility by remember(child.id) { mutableIntStateOf(child.agility.coerceIn(1, 100)) }

    var commentText by remember(child.id) { mutableStateOf("") }
    var errorText by remember(child.id) { mutableStateOf<String?>(null) }

    val coins = coinsText.toIntOrNull() ?: 0
    val experience = experienceText.toIntOrNull() ?: 0
    val availableSkillPoints = skillPointsText.toIntOrNull() ?: 0

    val calculatedLevel = ProgressManager.levelForExperience(experience)
    val currentLevelStartExperience = ProgressManager.experienceNeededForCurrentLevel(calculatedLevel)
    val nextLevelStartExperience = ProgressManager.experienceNeededForCurrentLevel(
        (calculatedLevel + 1).coerceAtMost(ProgressManager.MAX_LEVEL)
    )

    val experienceInCurrentLevel = (experience - currentLevelStartExperience).coerceAtLeast(0)
    val experienceNeededForCurrentLevel = (nextLevelStartExperience - currentLevelStartExperience).coerceAtLeast(1)
    val experienceUntilNextLevel = if (calculatedLevel >= ProgressManager.MAX_LEVEL) {
        0
    } else {
        (experienceNeededForCurrentLevel - experienceInCurrentLevel).coerceAtLeast(0)
    }

    fun setExperienceForLevel(level: Int) {
        val safeLevel = level.coerceIn(ProgressManager.MIN_LEVEL, ProgressManager.MAX_LEVEL)
        experienceText = ProgressManager.experienceNeededForCurrentLevel(safeLevel).toString()
    }

    fun changeSkillPoints(delta: Int) {
        val currentValue = skillPointsText.toIntOrNull() ?: 0
        skillPointsText = (currentValue + delta).coerceAtLeast(0).toString()
    }

    fun changeSkill(
        currentValue: Int,
        delta: Int,
        onValueChanged: (Int) -> Unit
    ) {
        onValueChanged((currentValue + delta).coerceIn(1, 100))
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Spielfortschritt bearbeiten")
        },
        text = {
            LazyColumn {
                item {
                    Text(
                        text = child.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("💰 Währung", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = coinsText,
                        onValueChange = {
                            coinsText = it.filter { char -> char.isDigit() }
                            errorText = null
                        },
                        label = { Text("Coins") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            coinsText = ((coinsText.toIntOrNull() ?: 0) + 100).toString()
                        }
                    ) {
                        Text("🎁 +100 Coins")
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Text("⭐ Fortschritt", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = experienceText,
                        onValueChange = {
                            experienceText = it.filter { char -> char.isDigit() }
                            errorText = null
                        },
                        label = { Text("EP") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (calculatedLevel >= ProgressManager.MAX_LEVEL) {
                            "Level $calculatedLevel · Maximallevel erreicht"
                        } else {
                            "Level $calculatedLevel · nächstes Level in $experienceUntilNextLevel EP"
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row {
                        OutlinedButton(
                            enabled = calculatedLevel > ProgressManager.MIN_LEVEL,
                            onClick = {
                                setExperienceForLevel(calculatedLevel - 1)
                                changeSkillPoints(-1)
                            }
                        ) {
                            Text("➖ Level")
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        OutlinedButton(
                            enabled = calculatedLevel < ProgressManager.MAX_LEVEL,
                            onClick = {
                                setExperienceForLevel(calculatedLevel + 1)
                                changeSkillPoints(1)
                            }
                        ) {
                            Text("➕ Level")
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            experienceText = ((experienceText.toIntOrNull() ?: 0) + 100).toString()
                        }
                    ) {
                        Text("🎁 +100 EP")
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Text("🎯 Skillpunkte", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = skillPointsText,
                        onValueChange = {
                            skillPointsText = it.filter { char -> char.isDigit() }
                            errorText = null
                        },
                        label = { Text("Verfügbare Skillpunkte") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Text("Skills", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                    Spacer(modifier = Modifier.height(8.dp))

                    SkillAdminRow(
                        label = "🧠 Intelligenz",
                        value = intelligence,
                        onMinus = {
                            changeSkill(intelligence, -1) { intelligence = it }
                        },
                        onPlus = {
                            changeSkill(intelligence, 1) { intelligence = it }
                        }
                    )

                    SkillAdminRow(
                        label = "💪 Stärke",
                        value = strength,
                        onMinus = {
                            changeSkill(strength, -1) { strength = it }
                        },
                        onPlus = {
                            changeSkill(strength, 1) { strength = it }
                        }
                    )

                    SkillAdminRow(
                        label = "⚡ Geschicklichkeit",
                        value = agility,
                        onMinus = {
                            changeSkill(agility, -1) { agility = it }
                        },
                        onPlus = {
                            changeSkill(agility, 1) { agility = it }
                        }
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    OutlinedTextField(
                        value = commentText,
                        onValueChange = { commentText = it },
                        label = { Text("Kommentar fürs Log (optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )

                    if (errorText != null) {
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = errorText!!,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        },
        confirmButton = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            ) {
                Button(
                    onClick = {
                        if (coinsText.isBlank()) {
                            errorText = "Bitte Coins eingeben."
                            return@Button
                        }

                        if (experienceText.isBlank()) {
                            errorText = "Bitte EP eingeben."
                            return@Button
                        }

                        if (skillPointsText.isBlank()) {
                            errorText = "Bitte Skillpunkte eingeben."
                            return@Button
                        }

                        onSave(
                            coins.coerceAtLeast(0),
                            experience.coerceAtLeast(0),
                            availableSkillPoints.coerceAtLeast(0),
                            intelligence.coerceIn(1, 100),
                            strength.coerceIn(1, 100),
                            agility.coerceIn(1, 100),
                            commentText.trim().ifBlank { null }
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Speichern", maxLines = 1)
                }

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Abbrechen", maxLines = 1)
                }
            }
        }
    )
}

@Composable
private fun SkillAdminRow(
    label: String,
    value: Int,
    onMinus: () -> Unit,
    onPlus: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold
        )

        OutlinedButton(
            onClick = onMinus,
            enabled = value > 1
        ) {
            Text("−")
        }

        Text(
            text = value.toString(),
            modifier = Modifier.width(48.dp),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        OutlinedButton(
            onClick = onPlus,
            enabled = value < 100
        ) {
            Text("+")
        }
    }
}


@Composable
private fun UserEditorDialog(
    child: Child?,
    onDismiss: () -> Unit,
    onSaveNew: (String, UserRole, String, Int, Int, Boolean, Boolean) -> Unit,
    onSaveExisting: (Child) -> Unit
) {
    val isEditMode = child != null
    val isBuiltInAdmin = child?.isBuiltInAdmin == true

    var nameText by remember(child?.id) { mutableStateOf(child?.name ?: "") }
    var ageText by remember(child?.id) { mutableStateOf((child?.age ?: 0).toString()) }
    var coinsText by remember(child?.id) { mutableStateOf((child?.coins ?: 0).toString()) }
    var passwordText by remember(child?.id) { mutableStateOf(child?.password ?: "") }
    var selectedRole by remember(child?.id) { mutableStateOf(child?.role ?: UserRole.CHILD) }
    var passwordRequired by remember(child?.id) { mutableStateOf(child?.passwordRequired ?: true) }
    var allowRememberLogin by remember(child?.id) { mutableStateOf(child?.allowRememberLogin ?: true) }
    var errorText by remember(child?.id) { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (isEditMode) "Benutzer bearbeiten" else "Benutzer hinzufügen")
        },
        text = {
            LazyColumn {
                item {
                    OutlinedTextField(
                        value = nameText,
                        onValueChange = {
                            nameText = it
                            errorText = null
                        },
                        label = { Text("Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = ageText,
                        onValueChange = {
                            ageText = it.filter { char -> char.isDigit() }
                            errorText = null
                        },
                        label = { Text("Alter / Sortierung") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    if (!isEditMode) {
                        OutlinedTextField(
                            value = coinsText,
                            onValueChange = {
                                coinsText = it.filterIndexed { index, char ->
                                    char.isDigit() || (char == '-' && index == 0)
                                }
                                errorText = null
                            },
                            label = { Text("Start-Coins") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    OutlinedTextField(
                        value = passwordText,
                        onValueChange = {
                            passwordText = it
                            errorText = null
                        },
                        label = { Text("Passwort") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Rolle", style = MaterialTheme.typography.titleMedium)

                    RoleOption(
                        text = "Kind",
                        selected = selectedRole == UserRole.CHILD,
                        enabled = !isBuiltInAdmin,
                        onClick = { selectedRole = UserRole.CHILD }
                    )

                    RoleOption(
                        text = "Eltern",
                        selected = selectedRole == UserRole.PARENT,
                        enabled = !isBuiltInAdmin,
                        onClick = { selectedRole = UserRole.PARENT }
                    )

                    RoleOption(
                        text = "Admin",
                        selected = selectedRole == UserRole.ADMIN,
                        enabled = true,
                        onClick = { selectedRole = UserRole.ADMIN }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable(enabled = !isBuiltInAdmin) {
                            passwordRequired = !passwordRequired
                        }
                    ) {
                        Checkbox(
                            checked = passwordRequired,
                            onCheckedChange = {
                                if (!isBuiltInAdmin) {
                                    passwordRequired = it
                                }
                            },
                            enabled = !isBuiltInAdmin
                        )
                        Text("Passwort erforderlich")
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable {
                            allowRememberLogin = !allowRememberLogin
                        }
                    ) {
                        Checkbox(
                            checked = allowRememberLogin,
                            onCheckedChange = { allowRememberLogin = it }
                        )
                        Text("Passwort auf Gerät merken erlauben")
                    }

                    if (isBuiltInAdmin) {
                        Text(
                            text = "Der Standard-Admin bleibt immer Admin und benötigt ein Passwort, sobald eines gesetzt ist.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    if (errorText != null) {
                        Text(
                            text = errorText!!,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            ) {
                Button(
                    onClick = {
                        val name = nameText.trim()
                        val age = ageText.toIntOrNull()
                        val coins = coinsText.toIntOrNull() ?: 0

                        if (name.isBlank()) {
                            errorText = "Bitte einen Namen eingeben."
                            return@Button
                        }

                        if (age == null) {
                            errorText = "Bitte ein gültiges Alter eingeben."
                            return@Button
                        }

                        if (isEditMode && child != null) {
                            val updatedChild = child.copy(
                                name = name,
                                age = age,
                                role = if (isBuiltInAdmin) UserRole.ADMIN else selectedRole,
                                password = passwordText,
                                passwordRequired = if (isBuiltInAdmin) true else passwordRequired,
                                allowRememberLogin = allowRememberLogin
                            )

                            onSaveExisting(updatedChild)
                        } else {
                            onSaveNew(
                                name,
                                selectedRole,
                                passwordText,
                                age,
                                coins,
                                passwordRequired,
                                allowRememberLogin
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Speichern", maxLines = 1)
                }

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Abbrechen", maxLines = 1)
                }
            }
        }
    )
}

@Composable
private fun RoleOption(
    text: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.clickable(enabled = enabled) {
            onClick()
        }
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick,
            enabled = enabled
        )

        Text(
            text = text,
            color = if (enabled) {
                MaterialTheme.colorScheme.onSurface
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
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
        TaskCompletionMode.ONCE_TOTAL -> {
            if (task.repeatType == TaskRepeatType.ONCE) {
                task.completions.isNotEmpty()
            } else {
                task.completions.any { it.date == selectedDateText }
            }
        }

        TaskCompletionMode.EACH_PERSON -> {
            val relevantChildren = if (task.assignmentType == TaskAssignmentType.ASSIGNED && task.assignedChildId != null) {
                children.filter { it.id == task.assignedChildId }
            } else {
                children.filter { it.role == UserRole.CHILD }
            }

            relevantChildren.isNotEmpty() && relevantChildren.all { child ->
                if (task.repeatType == TaskRepeatType.ONCE) {
                    task.completions.any { it.childId == child.id }
                } else {
                    task.completions.any { it.childId == child.id && it.date == selectedDateText }
                }
            }
        }
    }

    val assignedName = task.assignedChildId?.let { id ->
        children.firstOrNull { it.id == id }?.name
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
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
                    append(" · ${if (isDoneToday) "Erledigt" else "Offen"}")
                    if (task.repeatType == TaskRepeatType.ONCE) {
                        append(" · Einmalig")
                    }
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}