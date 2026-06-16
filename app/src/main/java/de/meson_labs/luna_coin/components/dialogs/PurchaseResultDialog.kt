package de.meson_labs.luna_coin.components.dialogs

import androidx.compose.runtime.Composable

@Composable
fun PurchaseResultDialog(
    message: String,
    onDismiss: () -> Unit
) {
    LunaBaseDialog(
        title = "Erfolg! 🎉",
        message = message,
        confirmText = "Super",
        dismissText = null,           // nur Bestätigen-Button
        onConfirm = onDismiss,
        onDismiss = onDismiss
    )
}