package de.meson_labs.luna_coin.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import de.meson_labs.luna_coin.models.Child
import de.meson_labs.luna_coin.models.DayOfWeekName
import de.meson_labs.luna_coin.models.DogScheduleItem
import kotlinx.coroutines.launch

@Composable
fun DogScheduleEditorDialog(
    dogSchedule: List<DogScheduleItem>,
    children: List<Child>,
    onDismiss: () -> Unit,
    onAddDogSchedule: (String, DayOfWeekName, String, String, String, String) -> Unit,
    onUpdateDogSchedule: (String, String, DayOfWeekName, String, String, String, String) -> Unit,
    onDeleteDogSchedule: (String) -> Unit
) {
    var selectedEntry by remember { mutableStateOf<DogScheduleItem?>(null) }
    var entryToDelete by remember { mutableStateOf<DogScheduleItem?>(null) }

    var selectedChildId by remember {
        mutableStateOf(children.firstOrNull()?.id ?: "")
    }

    var selectedDay by remember {
        mutableStateOf(DayOfWeekName.MONDAY)
    }

    var careStartTime by remember { mutableStateOf("08:00") }
    var careEndTime by remember { mutableStateOf("16:00") }
    var feedingTime by remember { mutableStateOf("07:30") }
    var walkTime by remember { mutableStateOf("18:00") }

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    fun resetFields() {
        selectedEntry = null
        selectedChildId = children.firstOrNull()?.id ?: ""
        selectedDay = DayOfWeekName.MONDAY
        careStartTime = "08:00"
        careEndTime = "16:00"
        feedingTime = "07:30"
        walkTime = "18:00"
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Hundeplan bearbeiten")
        },
        text = {
            LazyColumn(
                state = listState
            ) {
                item {
                    Text(
                        text = if (selectedEntry == null) {
                            "Neuen Hundeplan-Eintrag anlegen"
                        } else {
                            "Hundeplan-Eintrag bearbeiten"
                        },
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Person",
                                style = MaterialTheme.typography.bodyMedium
                            )

                            children.forEach { child ->
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
                        }

                        Spacer(modifier = Modifier.padding(6.dp))

                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Wochentag",
                                style = MaterialTheme.typography.bodyMedium
                            )

                            DayOfWeekName.entries.forEach { day ->
                                TextButton(
                                    onClick = {
                                        selectedDay = day
                                    }
                                ) {
                                    Text(
                                        text = if (selectedDay == day) {
                                            "✓ ${dayToGermanShort(day)}"
                                        } else {
                                            dayToGermanShort(day)
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = careStartTime,
                        onValueChange = { careStartTime = it },
                        label = { Text("Betreuung von, z.B. 08:00") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = careEndTime,
                        onValueChange = { careEndTime = it },
                        label = { Text("Betreuung bis, z.B. 16:00") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = feedingTime,
                        onValueChange = { feedingTime = it },
                        label = { Text("Füttern um, z.B. 07:30") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = walkTime,
                        onValueChange = { walkTime = it },
                        label = { Text("Gassi um, z.B. 18:00") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row {
                        Button(
                            onClick = {
                                if (
                                    selectedChildId.isNotBlank() &&
                                    careStartTime.isNotBlank() &&
                                    careEndTime.isNotBlank() &&
                                    feedingTime.isNotBlank() &&
                                    walkTime.isNotBlank()
                                ) {
                                    if (selectedEntry == null) {
                                        onAddDogSchedule(
                                            selectedChildId,
                                            selectedDay,
                                            careStartTime,
                                            careEndTime,
                                            feedingTime,
                                            walkTime
                                        )
                                    } else {
                                        onUpdateDogSchedule(
                                            selectedEntry!!.id,
                                            selectedChildId,
                                            selectedDay,
                                            careStartTime,
                                            careEndTime,
                                            feedingTime,
                                            walkTime
                                        )
                                    }

                                    resetFields()
                                }
                            }
                        ) {
                            Text(
                                if (selectedEntry == null) {
                                    "Eintrag anlegen"
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
                                if (selectedEntry == null) {
                                    "Felder leeren"
                                } else {
                                    "Neuer Eintrag"
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Vorhandene Hundeplan-Einträge",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                }

                items(dogSchedule) { entry ->
                    val childName = children.firstOrNull { child ->
                        child.id == entry.childId
                    }?.name ?: "Unbekannt"

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Text(
                                text = "${dayToGerman(entry.dayOfWeek)} · $childName",
                                style = MaterialTheme.typography.titleMedium
                            )

                            Text("Betreuung: ${entry.careStartTime} - ${entry.careEndTime} Uhr")
                            Text("Füttern: ${entry.feedingTime} Uhr")
                            Text("Gassi: ${entry.walkTime} Uhr")

                            Row {
                                TextButton(
                                    onClick = {
                                        selectedEntry = entry
                                        selectedChildId = entry.childId
                                        selectedDay = entry.dayOfWeek
                                        careStartTime = entry.careStartTime
                                        careEndTime = entry.careEndTime
                                        feedingTime = entry.feedingTime
                                        walkTime = entry.walkTime

                                        coroutineScope.launch {
                                            listState.animateScrollToItem(0)
                                        }
                                    }
                                ) {
                                    Text("Bearbeiten")
                                }

                                TextButton(
                                    onClick = {
                                        entryToDelete = entry
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

    entryToDelete?.let { entry ->
        AlertDialog(
            onDismissRequest = {
                entryToDelete = null
            },
            title = {
                Text("Hundeplan-Eintrag löschen?")
            },
            text = {
                Text("Soll dieser Hundeplan-Eintrag wirklich gelöscht werden?")
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        onDeleteDogSchedule(entry.id)

                        if (selectedEntry?.id == entry.id) {
                            resetFields()
                        }

                        entryToDelete = null
                    }
                ) {
                    Text("Löschen")
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        entryToDelete = null
                    }
                ) {
                    Text("Abbrechen")
                }
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

private fun dayToGermanShort(
    day: DayOfWeekName
): String {
    return when (day) {
        DayOfWeekName.MONDAY -> "Mo"
        DayOfWeekName.TUESDAY -> "Di"
        DayOfWeekName.WEDNESDAY -> "Mi"
        DayOfWeekName.THURSDAY -> "Do"
        DayOfWeekName.FRIDAY -> "Fr"
        DayOfWeekName.SATURDAY -> "Sa"
        DayOfWeekName.SUNDAY -> "So"
    }
}