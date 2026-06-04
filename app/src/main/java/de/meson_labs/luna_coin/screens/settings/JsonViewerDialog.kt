package de.meson_labs.luna_coin.screens.settings

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun JsonViewerDialog(
    jsonText: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Gespeicherte JSON-Daten")
        },
        text = {
            Text(
                text = jsonText.ifBlank {
                    "Noch keine JSON-Datei vorhanden."
                },
                modifier = Modifier.horizontalScroll(
                    rememberScrollState()
                )
            )
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("Schließen")
            }
        }
    )
}