package de.meson_labs.luna_coin.components.dialogs

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun LunaBaseDialog(
    title: String,
    message: String? = null,
    confirmText: String = "OK",
    dismissText: String? = "Abbrechen",
    onConfirm: (() -> Unit)? = null,
    onDismiss: () -> Unit,
    content: @Composable (() -> Unit)? = null
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            if (content != null) {
                content()
            } else if (message != null) {
                Text(message)
            }
        },
        confirmButton = {
            onConfirm?.let {
                TextButton(onClick = {
                    it()
                    onDismiss()
                }) {
                    Text(confirmText)
                }
            } ?: TextButton(onClick = onDismiss) { Text(confirmText) }
        },
        dismissButton = dismissText?.let {
            { TextButton(onClick = onDismiss) { Text(it) } }
        }
    )
}