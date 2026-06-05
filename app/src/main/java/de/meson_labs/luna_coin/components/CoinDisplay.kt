package de.meson_labs.luna_coin.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import de.meson_labs.luna_coin.R

@Composable
fun CoinDisplay(
    amount: Int,
    modifier: Modifier = Modifier,
    showPlus: Boolean = false,
    showMinus: Boolean = false,
    coinSize: Dp = 60.dp
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(
                id = R.drawable.luna_coin_small
            ),
            contentDescription = "Luna Coin",
            modifier = Modifier.size(coinSize)
        )

        Text(
            text = buildString {
                append(" ")

                if (showPlus && amount > 0) {
                    append("+")
                }

                if (showMinus && amount > 0) {
                    append("-")
                }

                append(amount)
            },
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.tertiary
        )
    }
}