package de.meson_labs.luna_coin.components.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import de.meson_labs.luna_coin.models.ShopItem

@Composable
fun ShopEditorDialog(
    items: List<ShopItem>,
    onDismiss: () -> Unit,
    onAddShopItem: (String, String, Int) -> Unit,
    onUpdateShopItem: (String, String, String, Int) -> Unit,
    onDeleteShopItem: (String) -> Unit
) {
    var selectedItem by remember {
        mutableStateOf<ShopItem?>(null)
    }

    var itemToDelete by remember {
        mutableStateOf<ShopItem?>(null)
    }

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var priceText by remember { mutableStateOf("1") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Shop bearbeiten")
        },
        text = {
            LazyColumn {

                item {

                    Text(
                        text = if (selectedItem == null) {
                            "Neuen Artikel anlegen"
                        } else {
                            "Artikel bearbeiten"
                        },
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Shop-Artikel") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Beschreibung") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = priceText,
                        onValueChange = { priceText = it },
                        label = { Text("Preis in Coins") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(
                        modifier = Modifier.height(12.dp)
                    )

                    Row {

                        Button(
                            onClick = {

                                val price =
                                    priceText.toIntOrNull() ?: 0

                                if (
                                    title.isNotBlank() &&
                                    price > 0
                                ) {

                                    if (selectedItem == null) {

                                        onAddShopItem(
                                            title,
                                            description,
                                            price
                                        )

                                    } else {

                                        onUpdateShopItem(
                                            selectedItem!!.id,
                                            title,
                                            description,
                                            price
                                        )
                                    }

                                    selectedItem = null
                                    title = ""
                                    description = ""
                                    priceText = "1"
                                }
                            }
                        ) {
                            Text(
                                if (selectedItem == null) {
                                    "Artikel anlegen"
                                } else {
                                    "Änderungen speichern"
                                }
                            )
                        }

                        Spacer(
                            modifier = Modifier.padding(6.dp)
                        )

                        OutlinedButton(
                            onClick = {
                                selectedItem = null
                                title = ""
                                description = ""
                                priceText = "1"
                            }
                        ) {
                            Text(
                                if (selectedItem == null) {
                                    "Felder leeren"
                                } else {
                                    "Neuer Artikel"
                                }
                            )
                        }
                    }

                    Spacer(
                        modifier = Modifier.height(16.dp)
                    )

                    Text(
                        text = "Shop-Artikel",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )
                }

                items(items) { item ->

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {

                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {

                            Text(
                                text = "${item.title} · ${item.priceCoins} Coins",
                                style = MaterialTheme.typography.titleMedium
                            )

                            if (item.description.isNotBlank()) {
                                Text(item.description)
                            }

                            Row {

                                TextButton(
                                    onClick = {
                                        selectedItem = item
                                        title = item.title
                                        description =
                                            item.description
                                        priceText =
                                            item.priceCoins.toString()
                                    }
                                ) {
                                    Text("Bearbeiten")
                                }

                                TextButton(
                                    onClick = {
                                        itemToDelete = item
                                    }
                                ) {
                                    Text("Löschen")
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("Schließen")
            }
        }
    )

    itemToDelete?.let { item ->

        DeleteShopItemDialog(
            item = item,
            onDelete = {
                onDeleteShopItem(item.id)
                itemToDelete = null
            },
            onCancel = {
                itemToDelete = null
            }
        )
    }
}