package de.meson_labs.luna_coin.screens.settings

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import de.meson_labs.luna_coin.models.ShopItem

@Composable
fun DeleteShopItemDialog(
    item: ShopItem,
    onDelete: () -> Unit,
    onCancel: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancel,
        title = {
            Text("Shop-Artikel löschen?")
        },
        text = {
            Text(
                "Soll der Shop-Artikel \"${item.title}\" wirklich gelöscht werden?"
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