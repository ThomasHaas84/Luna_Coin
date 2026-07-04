package de.meson_labs.luna_coin.components.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import de.meson_labs.luna_coin.models.DogPlanTaskTemplate
import de.meson_labs.luna_coin.models.DogPlanTaskType

@Composable
fun DogPlanEditorDialog(
    templates: List<DogPlanTaskTemplate>,
    onDismiss: () -> Unit,
    onSaveTemplate: (DogPlanTaskTemplate) -> Unit,
    onDeleteTemplate: (String) -> Unit
) {
    var templateForEdit by remember { mutableStateOf<DogPlanTaskTemplate?>(null) }
    var showTemplateDialog by remember { mutableStateOf(false) }
    var templateForDelete by remember { mutableStateOf<DogPlanTaskTemplate?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("🐶 Hundeplan bearbeiten")
        },
        text = {
            LazyColumn {
                item {
                    Button(
                        onClick = {
                            templateForEdit = null
                            showTemplateDialog = true
                        }
                    ) {
                        Text("Neue Aufgabe hinzufügen")
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                }

                val sortedTemplates = templates.sortedWith(
                    compareBy<DogPlanTaskTemplate> { it.sortOrder }
                        .thenBy { it.time }
                        .thenBy { it.title }
                )

                if (sortedTemplates.isEmpty()) {
                    item {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "Noch keine Hundeplan-Aufgaben vorhanden.",
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                } else {
                    items(sortedTemplates) { template ->
                        DogPlanTemplateCard(
                            template = template,
                            onEdit = {
                                templateForEdit = template
                                showTemplateDialog = true
                            },
                            onDelete = {
                                templateForDelete = template
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Schließen")
            }
        }
    )

    if (showTemplateDialog) {
        DogPlanTemplateEditorDialog(
            template = templateForEdit,
            templates = templates,
            onDismiss = {
                showTemplateDialog = false
                templateForEdit = null
            },
            onSave = { template ->
                onSaveTemplate(template)
                showTemplateDialog = false
                templateForEdit = null
            }
        )
    }

    templateForDelete?.let { template ->
        AlertDialog(
            onDismissRequest = { templateForDelete = null },
            title = { Text("Aufgabe löschen?") },
            text = {
                Text("Soll \"${template.title}\" wirklich gelöscht werden?")
            },
            dismissButton = {
                TextButton(onClick = { templateForDelete = null }) {
                    Text("Abbrechen")
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteTemplate(template.id)
                        templateForDelete = null
                    }
                ) {
                    Text("Löschen")
                }
            }
        )
    }
}

@Composable
private fun DogPlanTemplateCard(
    template: DogPlanTaskTemplate,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "${template.type.toIcon()} ${template.title}",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = buildString {
                    append(template.type.toDisplayText())

                    if (template.time.isNotBlank()) {
                        append(" · ${template.time}")
                    }

                    append(" · ${template.rewardCoins} Coins")
                    append(" · Reihenfolge ${template.sortOrder}")
                    append(" · ")
                    append(if (template.isActive) "Aktiv" else "Inaktiv")
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (template.requiresWalkDetails || template.requiresComment) {
                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = buildString {
                        val details = mutableListOf<String>()

                        if (template.requiresWalkDetails) {
                            details += "Gassi-Details"
                        }

                        if (template.requiresComment) {
                            details += "Kommentar Pflicht"
                        }

                        append(details.joinToString(" · "))
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row {
                OutlinedButton(onClick = onEdit) {
                    Text("Bearbeiten")
                }

                Spacer(modifier = Modifier.width(8.dp))

                OutlinedButton(onClick = onDelete) {
                    Text("Löschen")
                }
            }
        }
    }
}

@Composable
private fun DogPlanTemplateEditorDialog(
    template: DogPlanTaskTemplate?,
    templates: List<DogPlanTaskTemplate>,
    onDismiss: () -> Unit,
    onSave: (DogPlanTaskTemplate) -> Unit
) {
    val isEditMode = template != null

    var titleText by remember(template?.id) { mutableStateOf(template?.title ?: "") }
    var selectedType by remember(template?.id) { mutableStateOf(template?.type ?: DogPlanTaskType.WALK) }
    var timeText by remember(template?.id) { mutableStateOf(template?.time ?: "") }
    var coinsText by remember(template?.id) { mutableStateOf((template?.rewardCoins ?: 0).toString()) }
    var sortOrderText by remember(template?.id) {
        mutableStateOf(
            (template?.sortOrder ?: ((templates.maxOfOrNull { it.sortOrder } ?: 0) + 10)).toString()
        )
    }
    var isActive by remember(template?.id) { mutableStateOf(template?.isActive ?: true) }
    var requiresWalkDetails by remember(template?.id) {
        mutableStateOf(template?.requiresWalkDetails ?: true)
    }
    var requiresComment by remember(template?.id) {
        mutableStateOf(template?.requiresComment ?: false)
    }
    var errorText by remember(template?.id) { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (isEditMode) "Hunde-Aufgabe bearbeiten" else "Hunde-Aufgabe hinzufügen")
        },
        text = {
            LazyColumn {
                item {
                    OutlinedTextField(
                        value = titleText,
                        onValueChange = {
                            titleText = it
                            errorText = null
                        },
                        label = { Text("Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Typ", style = MaterialTheme.typography.titleMedium)

                    DogPlanTypeOption(
                        text = "Gassi",
                        selected = selectedType == DogPlanTaskType.WALK,
                        onClick = {
                            selectedType = DogPlanTaskType.WALK
                            requiresWalkDetails = true
                        }
                    )

                    DogPlanTypeOption(
                        text = "Füttern Frühschicht",
                        selected = selectedType == DogPlanTaskType.FEEDING_EARLY,
                        onClick = {
                            selectedType = DogPlanTaskType.FEEDING_EARLY
                            requiresWalkDetails = false
                        }
                    )

                    DogPlanTypeOption(
                        text = "Füttern Spätschicht",
                        selected = selectedType == DogPlanTaskType.FEEDING_LATE,
                        onClick = {
                            selectedType = DogPlanTaskType.FEEDING_LATE
                            requiresWalkDetails = false
                        }
                    )

                    DogPlanTypeOption(
                        text = "Sonstiges",
                        selected = selectedType == DogPlanTaskType.OTHER,
                        onClick = {
                            selectedType = DogPlanTaskType.OTHER
                            requiresWalkDetails = false
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = timeText,
                        onValueChange = {
                            timeText = it
                            errorText = null
                        },
                        label = { Text("Uhrzeit, z. B. 07:00") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

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

                    OutlinedTextField(
                        value = sortOrderText,
                        onValueChange = {
                            sortOrderText = it.filter { char -> char.isDigit() }
                            errorText = null
                        },
                        label = { Text("Reihenfolge") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    DogPlanCheckboxRow(
                        checked = isActive,
                        text = "Aktiv",
                        onCheckedChange = { isActive = it }
                    )

                    DogPlanCheckboxRow(
                        checked = requiresWalkDetails,
                        text = "Gassi-Details beim Abhaken anzeigen",
                        enabled = selectedType == DogPlanTaskType.WALK || selectedType == DogPlanTaskType.OTHER,
                        onCheckedChange = { requiresWalkDetails = it }
                    )

                    DogPlanCheckboxRow(
                        checked = requiresComment,
                        text = "Kommentar erforderlich",
                        onCheckedChange = { requiresComment = it }
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
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Abbrechen")
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val title = titleText.trim()
                    val coins = coinsText.toIntOrNull()
                    val sortOrder = sortOrderText.toIntOrNull()

                    if (title.isBlank()) {
                        errorText = "Bitte einen Namen eingeben."
                        return@TextButton
                    }

                    if (coins == null) {
                        errorText = "Bitte gültige Coins eingeben."
                        return@TextButton
                    }

                    if (sortOrder == null) {
                        errorText = "Bitte eine gültige Reihenfolge eingeben."
                        return@TextButton
                    }

                    onSave(
                        DogPlanTaskTemplate(
                            id = template?.id ?: "",
                            familyId = template?.familyId ?: "",
                            title = title,
                            type = selectedType,
                            time = timeText.trim(),
                            rewardCoins = coins,
                            isActive = isActive,
                            sortOrder = sortOrder,
                            requiresWalkDetails = requiresWalkDetails,
                            requiresComment = requiresComment,
                            createdAt = template?.createdAt,
                            updatedAt = template?.updatedAt
                        )
                    )
                }
            ) {
                Text("Speichern")
            }
        }
    )
}

@Composable
private fun DogPlanTypeOption(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.clickable { onClick() }
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick
        )

        Text(text = text)
    }
}

@Composable
private fun DogPlanCheckboxRow(
    checked: Boolean,
    text: String,
    enabled: Boolean = true,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.clickable(enabled = enabled) {
            onCheckedChange(!checked)
        }
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
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

private fun DogPlanTaskType.toDisplayText(): String {
    return when (this) {
        DogPlanTaskType.WALK -> "Gassi"
        DogPlanTaskType.FEEDING_EARLY -> "Füttern Früh"
        DogPlanTaskType.FEEDING_LATE -> "Füttern Spät"
        DogPlanTaskType.OTHER -> "Sonstiges"
    }
}

private fun DogPlanTaskType.toIcon(): String {
    return when (this) {
        DogPlanTaskType.WALK -> "🚶"
        DogPlanTaskType.FEEDING_EARLY -> "🍖"
        DogPlanTaskType.FEEDING_LATE -> "🍖"
        DogPlanTaskType.OTHER -> "🐾"
    }
}
