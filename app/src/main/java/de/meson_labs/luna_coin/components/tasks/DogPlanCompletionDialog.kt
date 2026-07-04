package de.meson_labs.luna_coin.components.tasks

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import de.meson_labs.luna_coin.models.DogPlanTaskTemplate

@Composable
fun DogPlanCompletionDialog(
    template: DogPlanTaskTemplate,
    onDismiss: () -> Unit,
    onConfirm: (
        peed: Boolean,
        pooped: Boolean,
        diarrhea: Boolean,
        comment: String
    ) -> Unit
) {
    var peed by remember(template.id) { mutableStateOf(false) }
    var pooped by remember(template.id) { mutableStateOf(false) }
    var diarrhea by remember(template.id) { mutableStateOf(false) }
    var comment by remember(template.id) { mutableStateOf("") }
    var errorText by remember(template.id) { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(template.title)
        },
        text = {
            LazyColumn {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = buildString {
                                if (template.time.isNotBlank()) {
                                    append("Uhrzeit: ${template.time}")
                                    append("\n")
                                }
                                append("Belohnung: ${template.rewardCoins} Luna Coins")
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        if (template.requiresWalkDetails) {
                            DogPlanCheckboxRow(
                                text = "Gepinkelt",
                                checked = peed,
                                onCheckedChange = { peed = it }
                            )

                            DogPlanCheckboxRow(
                                text = "Gekackt",
                                checked = pooped,
                                onCheckedChange = { pooped = it }
                            )

                            DogPlanCheckboxRow(
                                text = "Durchfall",
                                checked = diarrhea,
                                onCheckedChange = { diarrhea = it }
                            )

                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        OutlinedTextField(
                            value = comment,
                            onValueChange = {
                                comment = it
                                errorText = null
                            },
                            label = {
                                Text(
                                    if (template.requiresComment) {
                                        "Kommentar erforderlich"
                                    } else {
                                        "Kommentar optional"
                                    }
                                )
                            },
                            minLines = 3,
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (errorText != null) {
                            Text(
                                text = errorText!!,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
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
                    if (template.requiresComment && comment.trim().isBlank()) {
                        errorText = "Bitte einen Kommentar eingeben."
                        return@TextButton
                    }

                    onConfirm(
                        peed,
                        pooped,
                        diarrhea,
                        comment.trim()
                    )
                }
            ) {
                Text("Speichern")
            }
        }
    )
}

@Composable
private fun DogPlanCheckboxRow(
    text: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange
        )

        Text(text)
    }
}
