package de.meson_labs.luna_coin.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.meson_labs.luna_coin.components.CoinDisplay
import de.meson_labs.luna_coin.models.LogEntry
import kotlin.math.abs

@Composable
fun LogCard(
    log: LogEntry
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
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

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (log.coinChange >= 0) {
                    CoinDisplay(
                        amount = log.coinChange,
                        showPlus = true
                    )
                } else {
                    CoinDisplay(
                        amount = abs(log.coinChange),
                        showMinus = true
                    )
                }
            }
        }
    }
}