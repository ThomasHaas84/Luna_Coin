package de.meson_labs.luna_coin.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.meson_labs.luna_coin.models.Child

@Composable
fun LunaScreenHeader(
    title: String,
    selectedChild: Child?,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.displaySmall
            )

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${selectedChild?.name ?: ""}  ",
                    style = MaterialTheme.typography.headlineSmall
                )

                CoinDisplay(
                    amount = selectedChild?.coins ?: 0,
                    coinSize = 60.dp
                )
            }
        }

        OutlinedButton(
            onClick = onLogout
        ) {
            Text("Benutzer wechseln")
        }
    }
}