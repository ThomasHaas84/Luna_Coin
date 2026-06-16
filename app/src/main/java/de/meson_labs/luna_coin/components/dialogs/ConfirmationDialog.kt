package de.meson_labs.luna_coin.components.dialogs

import androidx.compose.runtime.Composable

@Composable
fun ConfirmationDialog(
    title: String = "Bestätigen",
    message: String,
    confirmText: String = "Ja",
    dismissText: String = "Nein",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    LunaBaseDialog(
        title = title,
        message = message,
        confirmText = confirmText,
        dismissText = dismissText,
        onConfirm = onConfirm,
        onDismiss = onDismiss
    )
}