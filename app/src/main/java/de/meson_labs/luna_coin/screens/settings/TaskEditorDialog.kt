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
import androidx.compose.material3.Checkbox
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
import de.meson_labs.luna_coin.models.TaskAssignmentType
import de.meson_labs.luna_coin.models.TaskCompletionMode
import de.meson_labs.luna_coin.models.TaskItem
import de.meson_labs.luna_coin.models.TaskRepeatType
import de.meson_labs.luna_coin.models.UserRole
import java.time.LocalDate

@Composable
fun TaskEditorDialog(
    tasks: List<TaskItem>,
    children: List<Child>,
    onDismiss: () -> Unit,
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
    onDeleteTask: (String) -> Unit
) {
    var selectedTask by remember { mutableStateOf<TaskItem?>(null) }
    var taskToDelete by remember { mutableStateOf<TaskItem?>(null) }

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var coinsText by remember { mutableStateOf("1") }

    var assignmentType by remember { mutableStateOf(TaskAssignmentType.FREE_FOR_ALL) }
    var completionMode by remember { mutableStateOf(TaskCompletionMode.EACH_PERSON) }
    var repeatType by remember { mutableStateOf(TaskRepeatType.DAILY) }

    var selectedChildId by remember { mutableStateOf<String?>(null) }
    var startDate by remember { mutableStateOf(isoDateToGerman(LocalDate.now().toString())) }
    var dueDate by remember { mutableStateOf("") }
    var selectedWeeklyDay by remember { mutableStateOf(DayOfWeekName.SATURDAY) }
    var isWatchlist by remember { mutableStateOf(false) }

    val childUsers = children.filter { child ->
        child.role == UserRole.CHILD
    }

    fun resetFields() {
        selectedTask = null
        title = ""
        description = ""
        coinsText = "1"
        assignmentType = TaskAssignmentType.FREE_FOR_ALL
        completionMode = TaskCompletionMode.EACH_PERSON
        repeatType = TaskRepeatType.DAILY
        selectedChildId = null
        startDate = isoDateToGerman(LocalDate.now().toString())
        dueDate = ""
        selectedWeeklyDay = DayOfWeekName.SATURDAY
        isWatchlist = false
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

                    Spacer(modifier = Modifier.height(8.dp))

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

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Sichtbarkeit",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Row {
                        TextButton(
                            onClick = {
                                assignmentType = TaskAssignmentType.FREE_FOR_ALL
                                selectedChildId = null
                            }
                        ) {
                            Text(
                                if (assignmentType == TaskAssignmentType.FREE_FOR_ALL) {
                                    "✓ Für alle"
                                } else {
                                    "Für alle"
                                }
                            )
                        }

                        TextButton(
                            onClick = {
                                assignmentType = TaskAssignmentType.ASSIGNED

                                if (selectedChildId == null) {
                                    selectedChildId = childUsers.firstOrNull()?.id
                                }

                                completionMode = TaskCompletionMode.EACH_PERSON
                            }
                        ) {
                            Text(
                                if (assignmentType == TaskAssignmentType.ASSIGNED) {
                                    "✓ Person"
                                } else {
                                    "Person"
                                }
                            )
                        }
                    }

                    if (assignmentType == TaskAssignmentType.ASSIGNED) {
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
                                    if (selectedChildId == child.id) {
                                        "✓ ${child.name}"
                                    } else {
                                        child.name
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Erledigung",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Row {
                        TextButton(
                            onClick = {
                                completionMode = TaskCompletionMode.EACH_PERSON
                            }
                        ) {
                            Text(
                                if (completionMode == TaskCompletionMode.EACH_PERSON) {
                                    "✓ Jeder einzeln"
                                } else {
                                    "Jeder einzeln"
                                }
                            )
                        }

                        TextButton(
                            onClick = {
                                completionMode = TaskCompletionMode.ONCE_TOTAL
                            },
                            enabled = assignmentType == TaskAssignmentType.FREE_FOR_ALL
                        ) {
                            Text(
                                if (completionMode == TaskCompletionMode.ONCE_TOTAL) {
                                    "✓ Einmal insgesamt"
                                } else {
                                    "Einmal insgesamt"
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Intervall",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Column {
                        TextButton(
                            onClick = {
                                repeatType = TaskRepeatType.DAILY
                            }
                        ) {
                            Text(
                                if (repeatType == TaskRepeatType.DAILY) {
                                    "✓ Täglich"
                                } else {
                                    "Täglich"
                                }
                            )
                        }

                        TextButton(
                            onClick = {
                                repeatType = TaskRepeatType.WEEKLY
                            }
                        ) {
                            Text(
                                if (repeatType == TaskRepeatType.WEEKLY) {
                                    "✓ Wöchentlich"
                                } else {
                                    "Wöchentlich"
                                }
                            )
                        }

                        TextButton(
                            onClick = {
                                repeatType = TaskRepeatType.BIWEEKLY
                            }
                        ) {
                            Text(
                                if (repeatType == TaskRepeatType.BIWEEKLY) {
                                    "✓ Zweiwöchentlich"
                                } else {
                                    "Zweiwöchentlich"
                                }
                            )
                        }

                        TextButton(
                            onClick = {
                                repeatType = TaskRepeatType.YEARLY
                            }
                        ) {
                            Text(
                                if (repeatType == TaskRepeatType.YEARLY) {
                                    "✓ Jährlich"
                                } else {
                                    "Jährlich"
                                }
                            )
                        }

                        TextButton(
                            onClick = {
                                repeatType = TaskRepeatType.EVERY_TWO_YEARS
                            }
                        ) {
                            Text(
                                if (repeatType == TaskRepeatType.EVERY_TWO_YEARS) {
                                    "✓ Alle zwei Jahre"
                                } else {
                                    "Alle zwei Jahre"
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = startDate,
                        onValueChange = {
                            startDate = it
                        },
                        label = {
                            Text("Startdatum, z.B. 30.06.26")
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = dueDate,
                        onValueChange = {
                            dueDate = it
                        },
                        label = {
                            Text("Ablauffrist optional, z.B. 30.06.26")
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (
                        repeatType == TaskRepeatType.WEEKLY ||
                        repeatType == TaskRepeatType.BIWEEKLY
                    ) {
                        Spacer(modifier = Modifier.height(12.dp))

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
                                    if (selectedWeeklyDay == day) {
                                        "✓ ${dayToGerman(day)}"
                                    } else {
                                        dayToGerman(day)
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Optionen",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Row {
                        Checkbox(
                            checked = isWatchlist,
                            onCheckedChange = {
                                isWatchlist = it
                            }
                        )

                        Text(
                            text = "In Watchlist anzeigen",
                            modifier = Modifier.padding(top = 12.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row {
                        Button(
                            onClick = {
                                val coins = coinsText.toIntOrNull() ?: 0

                                val validAssigned =
                                    assignmentType == TaskAssignmentType.FREE_FOR_ALL ||
                                            selectedChildId != null

                                val startDateIso =
                                    germanDateToIsoOrNull(startDate) ?: startDate

                                val dueDateIso =
                                    if (dueDate.isBlank()) {
                                        null
                                    } else {
                                        germanDateToIsoOrNull(dueDate) ?: dueDate
                                    }

                                val weeklyDay =
                                    if (
                                        repeatType == TaskRepeatType.WEEKLY ||
                                        repeatType == TaskRepeatType.BIWEEKLY
                                    ) {
                                        selectedWeeklyDay
                                    } else {
                                        null
                                    }

                                if (
                                    title.isNotBlank() &&
                                    coins > 0 &&
                                    startDateIso.isNotBlank() &&
                                    validAssigned
                                ) {
                                    if (selectedTask == null) {
                                        onAddTask(
                                            title,
                                            description,
                                            coins,
                                            assignmentType,
                                            completionMode,
                                            repeatType,
                                            selectedChildId,
                                            startDateIso,
                                            dueDateIso,
                                            weeklyDay,
                                            isWatchlist
                                        )
                                    } else {
                                        onUpdateTask(
                                            selectedTask!!.id,
                                            title,
                                            description,
                                            coins,
                                            assignmentType,
                                            completionMode,
                                            repeatType,
                                            selectedChildId,
                                            startDateIso,
                                            dueDateIso,
                                            weeklyDay,
                                            isWatchlist
                                        )
                                    }

                                    resetFields()
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

                        Spacer(modifier = Modifier.padding(6.dp))

                        OutlinedButton(
                            onClick = {
                                resetFields()
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

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Aufgaben",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.height(8.dp))
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
                                text = if (task.isWatchlist) {
                                    "👁 ${task.title}"
                                } else {
                                    task.title
                                },
                                style = MaterialTheme.typography.titleMedium
                            )

                            if (task.description.isNotBlank()) {
                                Text(
                                    text = task.description,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }

                            CoinDisplay(
                                amount = task.rewardCoins,
                                coinSize = 28.dp
                            )

                            Text(
                                text = buildString {
                                    append(assignmentTypeText(task.assignmentType))

                                    if (task.assignmentType == TaskAssignmentType.ASSIGNED) {
                                        append(" · ")
                                        append(assignedName ?: "Unbekannt")
                                    }

                                    append(" · ")
                                    append(completionModeText(task.completionMode))

                                    append(" · ")
                                    append(repeatTypeText(task.repeatType))

                                    if (
                                        task.repeatType == TaskRepeatType.WEEKLY ||
                                        task.repeatType == TaskRepeatType.BIWEEKLY
                                    ) {
                                        append(" · ")
                                        append(dayToGerman(task.weeklyDay ?: DayOfWeekName.MONDAY))
                                    }

                                    if (!task.dueDate.isNullOrBlank()) {
                                        append(" · Fällig: ")
                                        append(isoDateToGerman(task.dueDate))
                                    }

                                    if (task.isWatchlist) {
                                        append(" · Watchlist")
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
                                        assignmentType = task.assignmentType
                                        completionMode = task.completionMode
                                        repeatType = task.repeatType
                                        selectedChildId = task.assignedChildId
                                        startDate = isoDateToGerman(task.startDate)
                                        dueDate = task.dueDate?.let {
                                            isoDateToGerman(it)
                                        } ?: ""
                                        selectedWeeklyDay =
                                            task.weeklyDay ?: DayOfWeekName.SATURDAY
                                        isWatchlist = task.isWatchlist
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
                    resetFields()
                }

                taskToDelete = null
            },
            onCancel = {
                taskToDelete = null
            }
        )
    }
}

private fun assignmentTypeText(
    assignmentType: TaskAssignmentType
): String {
    return when (assignmentType) {
        TaskAssignmentType.FREE_FOR_ALL -> "Für alle"
        TaskAssignmentType.ASSIGNED -> "Person"
    }
}

private fun completionModeText(
    completionMode: TaskCompletionMode
): String {
    return when (completionMode) {
        TaskCompletionMode.EACH_PERSON -> "Jeder einzeln"
        TaskCompletionMode.ONCE_TOTAL -> "Einmal insgesamt"
    }
}

private fun repeatTypeText(
    repeatType: TaskRepeatType
): String {
    return when (repeatType) {
        TaskRepeatType.DAILY -> "Täglich"
        TaskRepeatType.WEEKLY -> "Wöchentlich"
        TaskRepeatType.BIWEEKLY -> "Zweiwöchentlich"
        TaskRepeatType.YEARLY -> "Jährlich"
        TaskRepeatType.EVERY_TWO_YEARS -> "Alle zwei Jahre"
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

private fun germanDateToIsoOrNull(
    input: String
): String? {
    val cleaned = input.trim()

    return try {
        val parts = cleaned.split(".")
        if (parts.size != 3) return null

        val day = parts[0].toInt()
        val month = parts[1].toInt()
        val yearRaw = parts[2].toInt()

        val year = if (yearRaw < 100) {
            2000 + yearRaw
        } else {
            yearRaw
        }

        LocalDate.of(
            year,
            month,
            day
        ).toString()
    } catch (_: Exception) {
        null
    }
}

private fun isoDateToGerman(
    input: String
): String {
    return try {
        val date = LocalDate.parse(input)

        String.format(
            "%02d.%02d.%02d",
            date.dayOfMonth,
            date.monthValue,
            date.year % 100
        )
    } catch (_: Exception) {
        input
    }
}