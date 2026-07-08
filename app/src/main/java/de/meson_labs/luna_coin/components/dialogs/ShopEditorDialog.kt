package de.meson_labs.luna_coin.components.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import de.meson_labs.luna_coin.models.ShopItem

@Composable
fun ShopEditorDialog(
    items: List<ShopItem>,
    onDismiss: () -> Unit,
    onAddShopItem: (String, String, Int, Int) -> Unit,
    onUpdateShopItem: (String, String, String, Int, Int) -> Unit,
    onDeleteShopItem: (String) -> Unit
) {
    var itemForEdit by remember { mutableStateOf<ShopItem?>(null) }
    var showItemDialog by remember { mutableStateOf(false) }
    var itemToDelete by remember { mutableStateOf<ShopItem?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Shop bearbeiten")
        },
        text = {
            LazyColumn {
                item {
                    Button(
                        onClick = {
                            itemForEdit = null
                            showItemDialog = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Neuen Artikel hinzufügen", maxLines = 1)
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                }

                if (items.isEmpty()) {
                    item {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "Noch keine Shop-Artikel vorhanden.",
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                } else {
                    items(
                        items.sortedWith(
                            compareBy<ShopItem> { it.priceCoins }
                                .thenBy { it.title.lowercase() }
                        )
                    ) { item ->
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

                                Text(
                                    text = if (item.maxPurchasesPerDay <= 0) {
                                        "Tageslimit: unbegrenzt"
                                    } else {
                                        "Tageslimit: ${item.maxPurchasesPerDay}x pro Kind pro Tag"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedButton(
                                    onClick = {
                                        itemForEdit = item
                                        showItemDialog = true
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Bearbeiten", maxLines = 1)
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                OutlinedButton(
                                    onClick = {
                                        itemToDelete = item
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = MaterialTheme.colorScheme.error
                                    )
                                ) {
                                    Text("Löschen", maxLines = 1)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            ) {
                Text("Schließen", maxLines = 1)
            }
        }
    )

    if (showItemDialog) {
        ShopItemEditorDialog(
            item = itemForEdit,
            onDismiss = {
                showItemDialog = false
                itemForEdit = null
            },
            onSave = { title, description, price, maxPurchasesPerDay ->
                val currentItem = itemForEdit

                if (currentItem == null) {
                    onAddShopItem(
                        title,
                        description,
                        price,
                        maxPurchasesPerDay
                    )
                } else {
                    onUpdateShopItem(
                        currentItem.id,
                        title,
                        description,
                        price,
                        maxPurchasesPerDay
                    )
                }

                showItemDialog = false
                itemForEdit = null
            }
        )
    }

    itemToDelete?.let { item ->
        DeleteShopItemDialog(
            item = item,
            onDelete = {
                onDeleteShopItem(item.id)
                itemToDelete = null

                if (itemForEdit?.id == item.id) {
                    itemForEdit = null
                    showItemDialog = false
                }
            },
            onCancel = {
                itemToDelete = null
            }
        )
    }
}

@Composable
private fun ShopItemEditorDialog(
    item: ShopItem?,
    onDismiss: () -> Unit,
    onSave: (String, String, Int, Int) -> Unit
) {
    var title by remember(item?.id) { mutableStateOf(item?.title ?: "") }
    var description by remember(item?.id) { mutableStateOf(item?.description ?: "") }
    var priceText by remember(item?.id) { mutableStateOf(item?.priceCoins?.toString() ?: "1") }
    var maxPurchasesPerDayText by remember(item?.id) {
        mutableStateOf(item?.maxPurchasesPerDay?.toString() ?: "0")
    }

    fun clearFields() {
        title = ""
        description = ""
        priceText = "1"
        maxPurchasesPerDayText = "0"
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (item == null) "Shop-Artikel hinzufügen" else "Shop-Artikel bearbeiten")
        },
        text = {
            LazyColumn {
                item {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Shop-Artikel") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Beschreibung") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = priceText,
                        onValueChange = { priceText = it.filter { char -> char.isDigit() } },
                        label = { Text("Preis in Coins") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = maxPurchasesPerDayText,
                        onValueChange = {
                            maxPurchasesPerDayText = it.filter { char -> char.isDigit() }
                        },
                        label = {
                            Text("Max. Käufe pro Tag")
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "0 = unbegrenzt",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                }
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
                        val price = priceText.toIntOrNull() ?: 0
                        val maxPurchasesPerDay = maxPurchasesPerDayText.toIntOrNull() ?: 0

                        if (
                            title.isNotBlank() &&
                            price > 0 &&
                            maxPurchasesPerDay >= 0
                        ) {
                            onSave(
                                title,
                                description,
                                price,
                                maxPurchasesPerDay
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Speichern", maxLines = 1)
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Abbrechen", maxLines = 1)
                }
            }
        }
    )
}
