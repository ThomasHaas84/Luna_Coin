package de.meson_labs.luna_coin.components.dialogs

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import de.meson_labs.luna_coin.models.TaskItem

@Composable
fun DeleteTaskDialog(
    task: TaskItem,
    onDelete: () -> Unit,
    onCancel: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancel,
        title = {
            Text("Aufgabe löschen?")
        },
        text = {
            Text(
                "Soll die Aufgabe \"${task.title}\" wirklich gelöscht werden?"
            )
        },
        dismissButton = {
            TextButton(
                onClick = onDelete
            ) {
                Text("Löschen")
            }
        },
        confirmButton = {
            TextButton(
                onClick = onCancel
            ) {
                Text("Abbrechen")
            }
        }
    )
}