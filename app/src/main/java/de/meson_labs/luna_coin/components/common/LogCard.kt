package de.meson_labs.luna_coin.components.common

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.meson_labs.luna_coin.components.CoinDisplay
import de.meson_labs.luna_coin.models.LogEntry
import kotlin.math.abs

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LogCard(
    log: LogEntry,
    canUndo: Boolean,
    onUndo: () -> Unit
) {
    var showUndoDialog by remember {
        mutableStateOf(false)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .combinedClickable(
                onClick = {},
                onLongClick = {
                    if (canUndo) {
                        showUndoDialog = true
                    }
                }
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

            if (log.coinChange != 0 || log.silverChange != 0L) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (log.coinChange > 0) {
                        CoinDisplay(
                            amount = log.coinChange,
                            showPlus = true,
                            coinSize = 28.dp
                        )
                    } else if (log.coinChange < 0) {
                        CoinDisplay(
                            amount = abs(log.coinChange),
                            showMinus = true,
                            coinSize = 28.dp
                        )
                    }

                    if (log.coinChange != 0 && log.silverChange != 0L) {
                        Spacer(modifier = Modifier.width(12.dp))
                    }

                    if (log.silverChange != 0L) {
                        Text(
                            text = buildString {
                                if (log.silverChange > 0L) {
                                    append("+")
                                } else {
                                    append("−")
                                }

                                append(abs(log.silverChange))
                                append(" Luna Silver")
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (log.silverChange > 0L) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.error
                            }
                        )
                    }
                }
            }

            if (canUndo) {
                Text(
                    text = "Lange drücken zum Rückgängig machen",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    if (showUndoDialog) {
        AlertDialog(
            onDismissRequest = {
                showUndoDialog = false
            },
            title = {
                Text("Log-Eintrag rückgängig machen?")
            },
            text = {
                Text(
                    text = "Die Coins beziehungsweise Luna Silver werden " +
                            "zurückgebucht und der Log-Eintrag wird entfernt."
                )
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showUndoDialog = false
                        onUndo()
                    }
                ) {
                    Text("Ja")
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showUndoDialog = false
                    }
                ) {
                    Text("Nein")
                }
            }
        )
    }
}
