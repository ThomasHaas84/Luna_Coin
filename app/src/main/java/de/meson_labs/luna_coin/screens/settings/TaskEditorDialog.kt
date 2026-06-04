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
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import de.meson_labs.luna_coin.models.TaskItem

@Composable
fun TaskEditorDialog(
    tasks: List<TaskItem>,
    defaultDate: String,
    onDismiss: () -> Unit,
    onAddTask: (String, String, Int, String) -> Unit,
    onUpdateTask: (String, String, String, Int, String) -> Unit,
    onDeleteTask: (String) -> Unit
) {
    var selectedTask by remember { mutableStateOf<TaskItem?>(null) }
    var taskToDelete by remember { mutableStateOf<TaskItem?>(null) }

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var coinsText by remember { mutableStateOf("1") }
    var date by remember { mutableStateOf(defaultDate) }

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
                        onValueChange = { title = it },
                        label = { Text("Aufgabe") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Beschreibung") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = coinsText,
                        onValueChange = { coinsText = it },
                        label = { Text("Coins") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = date,
                        onValueChange = { date = it },
                        label = { Text("Datum yyyy-MM-dd") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(
                        modifier = Modifier.height(12.dp)
                    )

                    Row {

                        Button(
                            onClick = {

                                val coins =
                                    coinsText.toIntOrNull() ?: 0

                                if (
                                    title.isNotBlank() &&
                                    coins > 0 &&
                                    date.isNotBlank()
                                ) {

                                    if (selectedTask == null) {

                                        onAddTask(
                                            title,
                                            description,
                                            coins,
                                            date
                                        )

                                    } else {

                                        onUpdateTask(
                                            selectedTask!!.id,
                                            title,
                                            description,
                                            coins,
                                            date
                                        )
                                    }

                                    selectedTask = null
                                    title = ""
                                    description = ""
                                    coinsText = "1"
                                    date = defaultDate
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
                                date = defaultDate
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

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {

                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {

                            Text(
                                text = "${task.title} · ${task.rewardCoins} Coins",
                                style = MaterialTheme.typography.titleMedium
                            )

                            if (task.description.isNotBlank()) {
                                Text(task.description)
                            }

                            Text(task.date)

                            Row {

                                TextButton(
                                    onClick = {
                                        selectedTask = task
                                        title = task.title
                                        description = task.description
                                        coinsText =
                                            task.rewardCoins.toString()
                                        date = task.date
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

                if (
                    selectedTask?.id ==
                    task.id
                ) {
                    selectedTask = null
                    title = ""
                    description = ""
                    coinsText = "1"
                    date = defaultDate
                }

                taskToDelete = null
            },
            onCancel = {
                taskToDelete = null
            }
        )
    }
}