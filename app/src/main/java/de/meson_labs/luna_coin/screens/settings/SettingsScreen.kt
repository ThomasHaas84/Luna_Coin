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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import de.meson_labs.luna_coin.R
import de.meson_labs.luna_coin.components.CoinDisplay
import de.meson_labs.luna_coin.models.Child
import de.meson_labs.luna_coin.models.DayOfWeekName
import de.meson_labs.luna_coin.models.LunaCoinData
import de.meson_labs.luna_coin.models.TaskAssignmentType
import de.meson_labs.luna_coin.models.TaskCompletionMode
import de.meson_labs.luna_coin.models.TaskItem
import de.meson_labs.luna_coin.models.TaskRepeatType
import de.meson_labs.luna_coin.models.UserRole
import de.meson_labs.luna_coin.screens.LunaGifDialog
import kotlinx.coroutines.delay
import java.time.LocalDate
import de.meson_labs.luna_coin.components.CoinDisplay
import de.meson_labs.luna_coin.components.LunaScreenHeader

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    data: LunaCoinData,
    selectedChild: Child?,
    selectedDate: LocalDate,
    jsonText: String,
    onAddTask: (
        String,
        String,
        Int,
        TaskAssignmentType,
        TaskCompletionMode,
        TaskRepeatType,
        String?,
        String,
        String?,
        DayOfWeekName?,
        Boolean
    ) -> Unit,
    onUpdateTask: (
        String,
        String,
        String,
        Int,
        TaskAssignmentType,
        TaskCompletionMode,
        TaskRepeatType,
        String?,
        String,
        String?,
        DayOfWeekName?,
        Boolean
    ) -> Unit,
    onDeleteTask: (String) -> Unit,
    onAddShopItem: (String, String, Int) -> Unit,
    onUpdateShopItem: (String, String, String, Int) -> Unit,
    onDeleteShopItem: (String) -> Unit,
    onAddDogSchedule: (String, DayOfWeekName, String, String, String, String) -> Unit,
    onUpdateDogSchedule: (String, String, DayOfWeekName, String, String, String, String) -> Unit,
    onDeleteDogSchedule: (String) -> Unit,
    onUpdateChildCoins: (String, Int) -> Unit,
    onUndoLogEntry: (String) -> Unit,
    onResetDemoData: () -> Unit,
    onLogout: () -> Unit
) {
    var showResetDialog by remember { mutableStateOf(false) }
    var showTaskEditor by remember { mutableStateOf(false) }
    var showShopEditor by remember { mutableStateOf(false) }
    var showDogScheduleEditor by remember { mutableStateOf(false) }

    var showUsersAndCoins by remember { mutableStateOf(false) }
    var showLogs by remember { mutableStateOf(false) }
    var showWatchlist by remember { mutableStateOf(true) }
    var showAppSettings by remember { mutableStateOf(false) }

    var languageMessage by remember { mutableStateOf<String?>(null) }

    var showLanguageGif by remember { mutableStateOf(false) }
    var languageGifTitle by remember { mutableStateOf("") }
    var languageGifMessage by remember { mutableStateOf("") }
    var languageGifResId by remember { mutableIntStateOf(0) }

    var mimiModeEnabled by remember { mutableStateOf(false) }

    var childForCoinEdit by remember { mutableStateOf<Child?>(null) }
    var coinEditText by remember { mutableStateOf("") }
    var coinEditError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(languageMessage) {
        if (languageMessage != null) {
            delay(3000)
            languageMessage = null
        }
    }

    val canEdit =
        selectedChild?.role == UserRole.PARENT ||
                selectedChild?.role == UserRole.ADMIN

    val visibleUsers =
        if (canEdit) {
            data.children
        } else {
            data.children.filter { child ->
                child.id == selectedChild?.id
            }
        }

    val visibleLogs =
        if (canEdit) {
            data.logs
        } else {
            data.logs.filter { log ->
                log.childId == selectedChild?.id
            }
        }

    val watchlistTasks =
        data.tasks.filter { task ->
            task.isWatchlist
        }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
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

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        showAppSettings = !showAppSettings
                    }
                ) {
                    Text(
                        if (showAppSettings) {
                            "App-Einstellungen ausblenden"
                        } else {
                            "App-Einstellungen anzeigen"
                        }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
            }

            if (showAppSettings) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = 2.dp
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "App-Einstellungen",
                                style = MaterialTheme.typography.headlineSmall
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "Sprache:",
                                style = MaterialTheme.typography.titleMedium
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Column {
                                TextButton(
                                    onClick = {
                                        languageMessage = "Deutsch wurde ausgewählt."
                                    }
                                ) {
                                    Text("Deutsch")
                                }

                                TextButton(
                                    onClick = {
                                        languageMessage = "Englisch wurde noch nicht implementiert."
                                    }
                                ) {
                                    Text("Englisch")
                                }

                                TextButton(
                                    onClick = {
                                        languageGifTitle = "Französisch"
                                        languageGifMessage = "Haha, gay..."
                                        languageGifResId = R.drawable.gay
                                        showLanguageGif = true
                                    }
                                ) {
                                    Text("Französisch")
                                }

                                TextButton(
                                    onClick = {
                                        languageMessage = "Qapla'!"
                                    }
                                ) {
                                    Text("Klingonisch")
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Switch(
                                    checked = mimiModeEnabled,
                                    onCheckedChange = {
                                        mimiModeEnabled = it
                                    }
                                )

                                Text(
                                    text = "Mimi-Modus aktivieren",
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.padding(start = 12.dp)
                                )
                            }

                            Text(
                                text = "Dieser experimentelle Modus stellt die Helligkeit des Bildschirms herunter, die Lautstärke auf ein Minimum und lässt bei den meisten Sätzen Fragezeichen am Ende anzeigen?",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            item {
                if (canEdit) {
                    Button(
                        onClick = {
                            showUsersAndCoins = !showUsersAndCoins
                        }
                    ) {
                        Text(
                            if (showUsersAndCoins) {
                                "Benutzer & Coins ausblenden"
                            } else {
                                "Benutzer & Coins anzeigen"
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (showUsersAndCoins) {
                        Text(
                            text = "Benutzerkarte lange gedrückt halten, um Coins zu bearbeiten.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                    }
                } else {
                    Text(
                        text = "Meine Coins",
                        style = MaterialTheme.typography.headlineSmall
                    )

                    Spacer(modifier = Modifier.height(8.dp))
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
                                        coinEditError = null
                                    }
                                }
                            ),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = 1.dp
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${child.name}: ",
                                style = MaterialTheme.typography.bodyLarge
                            )

                            CoinDisplay(
                                amount = child.coins
                            )

                            Text(
                                text = " · ${roleText(child.role)}",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))

                if (canEdit) {
                    Text(
                        text = "Watchlist",
                        style = MaterialTheme.typography.headlineSmall
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            showWatchlist = !showWatchlist
                        }
                    ) {
                        Text(
                            if (showWatchlist) {
                                "Watchlist ausblenden"
                            } else {
                                "Watchlist anzeigen"
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            if (canEdit && showWatchlist) {
                if (watchlistTasks.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Keine Aufgaben auf der Watchlist.",
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                } else {
                    items(watchlistTasks) { task ->
                        WatchlistTaskCard(
                            task = task,
                            selectedDate = selectedDate,
                            children = data.children
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))

                if (canEdit) {
                    Text(
                        text = "Bearbeiten",
                        style = MaterialTheme.typography.headlineSmall
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row {
                        Button(
                            onClick = {
                                showTaskEditor = true
                            }
                        ) {
                            Text("Aufgaben bearbeiten")
                        }

                        Spacer(modifier = Modifier.padding(8.dp))

                        Button(
                            onClick = {
                                showShopEditor = true
                            }
                        ) {
                            Text("Shop bearbeiten")
                        }

                        Spacer(modifier = Modifier.padding(8.dp))

                        Button(
                            onClick = {
                                showDogScheduleEditor = true
                            }
                        ) {
                            Text("Hundeplan bearbeiten")
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }

                if (selectedChild?.role == UserRole.ADMIN) {
                    OutlinedButton(
                        onClick = {
                            showResetDialog = true
                        }
                    ) {
                        Text("Demo-Daten zurücksetzen")
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }

                if (canEdit) {
                    Button(
                        onClick = {
                            showLogs = !showLogs
                        }
                    ) {
                        Text(
                            if (showLogs) {
                                "Logs ausblenden"
                            } else {
                                "Logs anzeigen"
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                } else {
                    Text(
                        text = "Mein Log",
                        style = MaterialTheme.typography.headlineSmall
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (canEdit && showLogs) {
                    Text(
                        text = "Einträge lange drücken, um sie rückgängig zu machen.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            if (!canEdit || showLogs) {
                if (visibleLogs.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Noch keine Einträge vorhanden.",
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                } else {
                    items(visibleLogs) { log ->
                        LogCard(
                            log = log,
                            canUndo = canEdit,
                            onUndo = {
                                onUndoLogEntry(log.id)
                            }
                        )
                    }
                }
            }
        }

        languageMessage?.let { message ->
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(
                        start = 24.dp,
                        end = 24.dp,
                        bottom = 24.dp
                    ),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 12.dp
                )
            ) {
                Text(
                    text = message,
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }

    childForCoinEdit?.let { child ->
        AlertDialog(
            onDismissRequest = {
                childForCoinEdit = null
                coinEditText = ""
                coinEditError = null
            },
            title = {
                Text("Coins bearbeiten")
            },
            text = {
                Column {
                    Text(
                        text = "Benutzer: ${child.name}",
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = coinEditText,
                        onValueChange = { value ->
                            coinEditText = value.filterIndexed { index, char ->
                                char.isDigit() || (char == '-' && index == 0)
                            }
                            coinEditError = null
                        },
                        label = {
                            Text("Coins")
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        ),
                        isError = coinEditError != null,
                        supportingText = {
                            coinEditError?.let { error ->
                                Text(error)
                            }
                        }
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        childForCoinEdit = null
                        coinEditText = ""
                        coinEditError = null
                    }
                ) {
                    Text("Abbrechen")
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val newCoins = coinEditText.toIntOrNull()

                        if (newCoins == null) {
                            coinEditError = "Bitte eine gültige Zahl eingeben."
                        } else {
                            onUpdateChildCoins(child.id, newCoins)
                            childForCoinEdit = null
                            coinEditText = ""
                            coinEditError = null
                        }
                    }
                ) {
                    Text("Speichern")
                }
            }
        )
    }

    if (showTaskEditor) {
        TaskEditorDialog(
            tasks = data.tasks,
            children = data.children,
            onDismiss = {
                showTaskEditor = false
            },
            onAddTask = onAddTask,
            onUpdateTask = onUpdateTask,
            onDeleteTask = onDeleteTask
        )
    }

    if (showShopEditor) {
        ShopEditorDialog(
            items = data.shopItems,
            onDismiss = {
                showShopEditor = false
            },
            onAddShopItem = onAddShopItem,
            onUpdateShopItem = onUpdateShopItem,
            onDeleteShopItem = onDeleteShopItem
        )
    }

    if (showDogScheduleEditor) {
        DogScheduleEditorDialog(
            dogSchedule = data.dogSchedule,
            children = data.children,
            onDismiss = {
                showDogScheduleEditor = false
            },
            onAddDogSchedule = onAddDogSchedule,
            onUpdateDogSchedule = onUpdateDogSchedule,
            onDeleteDogSchedule = onDeleteDogSchedule
        )
    }

    if (showLanguageGif) {
        LunaGifDialog(
            title = languageGifTitle,
            message = languageGifMessage,
            gifResId = languageGifResId,
            contentDescription = languageGifTitle,
            onDismiss = {
                showLanguageGif = false
            }
        )
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = {
                showResetDialog = false
            },
            title = {
                Text("Demo-Daten zurücksetzen?")
            },
            text = {
                Text("Alle aktuellen Daten werden durch Demo-Daten ersetzt.")
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showResetDialog = false
                        onResetDemoData()
                    }
                ) {
                    Text("Zurücksetzen")
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showResetDialog = false
                    }
                ) {
                    Text("Abbrechen")
                }
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
    val isDoneToday =
        when (task.completionMode) {
            TaskCompletionMode.ONCE_TOTAL -> {
                task.completions.any { completion ->
                    completion.date == selectedDateText
                }
            }

            TaskCompletionMode.EACH_PERSON -> {
                val relevantChildren =
                    if (task.assignmentType == TaskAssignmentType.ASSIGNED && task.assignedChildId != null) {
                        children.filter { child ->
                            child.id == task.assignedChildId
                        }
                    } else {
                        children.filter { child ->
                            child.role == UserRole.CHILD
                        }
                    }

                relevantChildren.isNotEmpty() &&
                        relevantChildren.all { child ->
                            task.completions.any { completion ->
                                completion.childId == child.id &&
                                        completion.date == selectedDateText
                            }
                        }
            }
        }

    val assignedName =
        task.assignedChildId?.let { childId ->
            children.firstOrNull { child ->
                child.id == childId
            }?.name
        }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = if (isDoneToday) {
                    "✅ ${task.title}"
                } else {
                    "⬜ ${task.title}"
                },
                style = MaterialTheme.typography.titleMedium
            )

            if (task.description.isNotBlank()) {
                Text(
                    text = task.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = buildString {
                    append(task.rewardCoins)
                    append(" Coins")

                    if (assignedName != null) {
                        append(" · ")
                        append(assignedName)
                    }

                    append(" · ")
                    append(
                        if (isDoneToday) {
                            "Heute erledigt"
                        } else {
                            "Heute noch offen"
                        }
                    )
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}