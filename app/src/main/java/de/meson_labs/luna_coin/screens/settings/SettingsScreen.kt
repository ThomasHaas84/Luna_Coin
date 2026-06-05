package de.meson_labs.luna_coin.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.meson_labs.luna_coin.components.CoinDisplay
import de.meson_labs.luna_coin.models.Child
import de.meson_labs.luna_coin.models.DayOfWeekName
import de.meson_labs.luna_coin.models.LunaCoinData
import de.meson_labs.luna_coin.models.TaskAssignmentType
import de.meson_labs.luna_coin.models.TaskCompletionMode
import de.meson_labs.luna_coin.models.TaskItem
import de.meson_labs.luna_coin.models.TaskRepeatType
import de.meson_labs.luna_coin.models.UserRole
import java.time.LocalDate

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

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Einstellungen",
                        style = MaterialTheme.typography.displaySmall
                    )

                    Text(
                        text = "Angemeldet als ${selectedChild?.name ?: ""}",
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                OutlinedButton(
                    onClick = onLogout
                ) {
                    Text("Benutzer wechseln")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

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
                        .padding(vertical = 4.dp),
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