package de.meson_labs.luna_coin.components.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

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
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            ) {
                Button(
                    onClick = {
                        onConfirm?.invoke()
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(confirmText, maxLines = 1)
                }

                dismissText?.let {
                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(it, maxLines = 1)
                    }
                }
            }
        }
    )
}
