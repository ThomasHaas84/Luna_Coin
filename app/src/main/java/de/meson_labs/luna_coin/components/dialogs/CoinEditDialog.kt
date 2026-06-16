package de.meson_labs.luna_coin.components.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import de.meson_labs.luna_coin.models.Child

@Composable
fun CoinEditDialog(
    child: Child,
    coinText: String,
    commentText: String,
    errorMessage: String?,
    onCoinTextChange: (String) -> Unit,
    onCommentTextChange: (String) -> Unit,
    onSave: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Coins bearbeiten") },
        text = {
            Column {
                Text(
                    text = "Benutzer: ${child.name}",
                    style = androidx.compose.material3.MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = coinText,
                    onValueChange = onCoinTextChange,
                    label = { Text("Coins") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = errorMessage != null
                )

                if (errorMessage != null) {
                    Text(
                        text = errorMessage,
                        color = androidx.compose.material3.MaterialTheme.colorScheme.error,
                        style = androidx.compose.material3.MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = commentText,
                    onValueChange = onCommentTextChange,
                    label = { Text("Kommentar (optional)") },
                    singleLine = false,
                    minLines = 2,
                    maxLines = 4
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Abbrechen")
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val newCoins = coinText.toIntOrNull()
                if (newCoins != null) {
                    onSave(newCoins)
                }
            }) {
                Text("Speichern")
            }
        }
    )
}