package de.meson_labs.luna_coin.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import de.meson_labs.luna_coin.components.CoinDisplay
import de.meson_labs.luna_coin.models.Child
import de.meson_labs.luna_coin.models.DayOfWeekName
import de.meson_labs.luna_coin.models.TaskItem
import de.meson_labs.luna_coin.models.TaskRepeatType
import de.meson_labs.luna_coin.models.UserRole

@Composable
fun TaskEditorDialog(
    tasks: List<TaskItem>,
    children: List<Child>,
    onDismiss: () -> Unit,
    onAddTask: (String, String, Int, TaskRepeatType, String?, DayOfWeekName?) -> Unit,
    onUpdateTask: (String, String, String, Int, TaskRepeatType, String?, DayOfWeekName?) -> Unit,
    onDeleteTask: (String) -> Unit
) {
    var selectedTask by remember {
        mutableStateOf<TaskItem?>(null)
    }

    var taskToDelete by remember {
        mutableStateOf<TaskItem?>(null)
    }

    var title by remember {
        mutableStateOf("")
    }

    var description by remember {
        mutableStateOf("")
    }

    var coinsText by remember {
        mutableStateOf("1")
    }

    var repeatType by remember {
        mutableStateOf(TaskRepeatType.DAILY)
    }

    var selectedChildId by remember {
        mutableStateOf<String?>(null)
    }

    var selectedWeeklyDay by remember {
        mutableStateOf(DayOfWeekName.SATURDAY)
    }

    val childUsers = children.filter { child ->
        child.role == UserRole.CHILD
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Aufgaben bearbeiten")
        },
        text = {
            LazyColumn {
                item {
                    Text(
                        text = if (selectedTask == null) {
                            "Neue Aufgabe anlegen"
                        } else {
                            "Aufgabe bearbeiten"
                        },
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )

                    OutlinedTextField(
                        value = title,
                        onValueChange = {
                            title = it
                        },
                        label = {
                            Text("Aufgabe")
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = description,
                        onValueChange = {
                            description = it
                        },
                        label = {
                            Text("Beschreibung")
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = coinsText,
                        onValueChange = {
                            coinsText = it
                        },
                        label = {
                            Text("Coins")
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(
                        modifier = Modifier.height(16.dp)
                    )

                    Text(
                        text = "Aufgabentyp",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Row {
                        TextButton(
                            onClick = {
                                repeatType = TaskRepeatType.DAILY
                                selectedChildId = null
                            }
                        ) {
                            Text(
                                text = if (repeatType == TaskRepeatType.DAILY) {
                                    "✓ Täglich"
                                } else {
                                    "Täglich"
                                }
                            )
                        }

                        TextButton(
                            onClick = {
                                repeatType = TaskRepeatType.WEEKLY

                                if (selectedChildId == null) {
                                    selectedChildId = childUsers.firstOrNull()?.id
                                }
                            }
                        ) {
                            Text(
                                text = if (repeatType == TaskRepeatType.WEEKLY) {
                                    "✓ Wöchentlich"
                                } else {
                                    "Wöchentlich"
                                }
                            )
                        }
                    }

                    if (repeatType == TaskRepeatType.DAILY) {
                        Text(
                            text = "Tägliche Aufgaben werden jedem Kind jeden Tag angezeigt.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (repeatType == TaskRepeatType.WEEKLY) {
                        Spacer(
                            modifier = Modifier.height(12.dp)
                        )

                        Text(
                            text = "Person",
                            style = MaterialTheme.typography.titleMedium
                        )

                        childUsers.forEach { child ->
                            TextButton(
                                onClick = {
                                    selectedChildId = child.id
                                }
                            ) {
                                Text(
                                    text = if (selectedChildId == child.id) {
                                        "✓ ${child.name}"
                                    } else {
                                        child.name
                                    }
                                )
                            }
                        }

                        Spacer(
                            modifier = Modifier.height(8.dp)
                        )

                        Text(
                            text = "Wochentag",
                            style = MaterialTheme.typography.titleMedium
                        )

                        DayOfWeekName.entries.forEach { day ->
                            TextButton(
                                onClick = {
                                    selectedWeeklyDay = day
                                }
                            ) {
                                Text(
                                    text = if (selectedWeeklyDay == day) {
                                        "✓ ${dayToGerman(day)}"
                                    } else {
                                        dayToGerman(day)
                                    }
                                )
                            }
                        }
                    }

                    Spacer(
                        modifier = Modifier.height(12.dp)
                    )

                    Row {
                        Button(
                            onClick = {
                                val coins = coinsText.toIntOrNull() ?: 0

                                val canSaveDaily =
                                    repeatType == TaskRepeatType.DAILY

                                val canSaveWeekly =
                                    repeatType == TaskRepeatType.WEEKLY &&
                                            selectedChildId != null

                                if (
                                    title.isNotBlank() &&
                                    coins > 0 &&
                                    (canSaveDaily || canSaveWeekly)
                                ) {
                                    if (selectedTask == null) {
                                        onAddTask(
                                            title,
                                            description,
                                            coins,
                                            repeatType,
                                            if (repeatType == TaskRepeatType.WEEKLY) {
                                                selectedChildId
                                            } else {
                                                null
                                            },
                                            if (repeatType == TaskRepeatType.WEEKLY) {
                                                selectedWeeklyDay
                                            } else {
                                                null
                                            }
                                        )
                                    } else {
                                        onUpdateTask(
                                            selectedTask!!.id,
                                            title,
                                            description,
                                            coins,
                                            repeatType,
                                            if (repeatType == TaskRepeatType.WEEKLY) {
                                                selectedChildId
                                            } else {
                                                null
                                            },
                                            if (repeatType == TaskRepeatType.WEEKLY) {
                                                selectedWeeklyDay
                                            } else {
                                                null
                                            }
                                        )
                                    }

                                    selectedTask = null
                                    title = ""
                                    description = ""
                                    coinsText = "1"
                                    repeatType = TaskRepeatType.DAILY
                                    selectedChildId = null
                                    selectedWeeklyDay = DayOfWeekName.SATURDAY
                                }
                            }
                        ) {
                            Text(
                                if (selectedTask == null) {
                                    "Aufgabe anlegen"
                                } else {
                                    "Änderungen speichern"
                                }
                            )
                        }

                        Spacer(
                            modifier = Modifier.padding(6.dp)
                        )

                        OutlinedButton(
                            onClick = {
                                selectedTask = null
                                title = ""
                                description = ""
                                coinsText = "1"
                                repeatType = TaskRepeatType.DAILY
                                selectedChildId = null
                                selectedWeeklyDay = DayOfWeekName.SATURDAY
                            }
                        ) {
                            Text(
                                if (selectedTask == null) {
                                    "Felder leeren"
                                } else {
                                    "Neue Aufgabe"
                                }
                            )
                        }
                    }

                    Spacer(
                        modifier = Modifier.height(16.dp)
                    )

                    Text(
                        text = "Aufgaben",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        text = "Zum Bearbeiten auf 'Bearbeiten' tippen.",
                        style = MaterialTheme.typography.bodySmall
                    )

                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )
                }

                items(tasks) { task ->
                    val assignedName = task.assignedChildId?.let { assignedId ->
                        children.firstOrNull { child ->
                            child.id == assignedId
                        }?.name
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Text(
                                text = task.title,
                                style = MaterialTheme.typography.titleMedium
                            )

                            if (task.description.isNotBlank()) {
                                Text(
                                    text = task.description,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }

                            Row {
                                CoinDisplay(
                                    amount = task.rewardCoins,
                                    coinSize = 28.dp
                                )
                            }

                            Text(
                                text = when (task.repeatType) {
                                    TaskRepeatType.DAILY -> {
                                        "Täglich · Für alle Kinder"
                                    }

                                    TaskRepeatType.WEEKLY -> {
                                        "Wöchentlich · ${dayToGerman(task.weeklyDay ?: DayOfWeekName.MONDAY)} · ${assignedName ?: "Unbekannt"}"
                                    }
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Row {
                                TextButton(
                                    onClick = {
                                        selectedTask = task
                                        title = task.title
                                        description = task.description
                                        coinsText = task.rewardCoins.toString()
                                        repeatType = task.repeatType
                                        selectedChildId = task.assignedChildId
                                        selectedWeeklyDay =
                                            task.weeklyDay ?: DayOfWeekName.SATURDAY
                                    }
                                ) {
                                    Text("Bearbeiten")
                                }

                                TextButton(
                                    onClick = {
                                        taskToDelete = task
                                    }
                                ) {
                                    Text("Löschen")
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("Schließen")
            }
        }
    )

    taskToDelete?.let { task ->
        DeleteTaskDialog(
            task = task,
            onDelete = {
                onDeleteTask(task.id)

                if (selectedTask?.id == task.id) {
                    selectedTask = null
                    title = ""
                    description = ""
                    coinsText = "1"
                    repeatType = TaskRepeatType.DAILY
                    selectedChildId = null
                    selectedWeeklyDay = DayOfWeekName.SATURDAY
                }

                taskToDelete = null
            },
            onCancel = {
                taskToDelete = null
            }
        )
    }
}

private fun dayToGerman(
    day: DayOfWeekName
): String {
    return when (day) {
        DayOfWeekName.MONDAY -> "Montag"
        DayOfWeekName.TUESDAY -> "Dienstag"
        DayOfWeekName.WEDNESDAY -> "Mittwoch"
        DayOfWeekName.THURSDAY -> "Donnerstag"
        DayOfWeekName.FRIDAY -> "Freitag"
        DayOfWeekName.SATURDAY -> "Samstag"
        DayOfWeekName.SUNDAY -> "Sonntag"
    }
}