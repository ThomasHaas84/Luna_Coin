package de.meson_labs.luna_coin.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.meson_labs.luna_coin.models.LogEntry
import de.meson_labs.luna_coin.models.LunaCoinData

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    data: LunaCoinData,
    jsonText: String,
    onResetDemoData: () -> Unit,
    onLogout: () -> Unit
) {
    var showJsonDialog by remember {
        mutableStateOf(false)
    }

    var showResetDialog by remember {
        mutableStateOf(false)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Einstellungen",
                        style = MaterialTheme.typography.displaySmall
                    )

                    Text(
                        text = "Verwaltung, Log und JSON-Daten",
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                OutlinedButton(
                    onClick = onLogout
                ) {
                    Text("Benutzer wechseln")
                }
            }

            Spacer(
                modifier = Modifier.height(24.dp)
            )

            Text(
                text = "Kinder",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            data.children.forEach { child ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 1.dp
                    )
                ) {
                    Text(
                        text = "${child.name}: ${child.coins} Luna Coins",
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(24.dp)
            )

            Row {
                Button(
                    onClick = {
                        showJsonDialog = true
                    }
                ) {
                    Text("JSON anzeigen")
                }

                Spacer(
                    modifier = Modifier.padding(8.dp)
                )

                OutlinedButton(
                    onClick = {
                        showResetDialog = true
                    }
                ) {
                    Text("Demo-Daten zurücksetzen")
                }
            }

            Spacer(
                modifier = Modifier.height(24.dp)
            )

            Text(
                text = "Log",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )
        }

        if (data.logs.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Noch keine Einträge vorhanden.",
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            items(data.logs) { log ->
                LogCard(
                    log = log
                )
            }
        }
    }

    if (showJsonDialog) {
        AlertDialog(
            onDismissRequest = {
                showJsonDialog = false
            },
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
                    onClick = {
                        showJsonDialog = false
                    }
                ) {
                    Text("Schließen")
                }
            }
        )
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = {
                showResetDialog = false
            },
            title = {
                Text("Demo-Daten zurücksetzen?")
            },
            text = {
                Text("Alle aktuellen Daten werden durch Demo-Daten ersetzt.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showResetDialog = false
                        onResetDemoData()
                    }
                ) {
                    Text("Zurücksetzen")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showResetDialog = false
                    }
                ) {
                    Text("Abbrechen")
                }
            }
        )
    }
}

@Composable
private fun LogCard(
    log: LogEntry
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                vertical = 4.dp
            ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = log.text,
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = log.timestamp,
                style = MaterialTheme.typography.bodySmall
            )

            Text(
                text = if (log.coinChange >= 0) {
                    "+${log.coinChange} Luna Coins"
                } else {
                    "${log.coinChange} Luna Coins"
                },
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}