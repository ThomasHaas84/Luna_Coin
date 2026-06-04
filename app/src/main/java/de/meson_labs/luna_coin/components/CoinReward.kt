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
import androidx.compose.ui.unit.dp
import de.meson_labs.luna_coin.R

@Composable
fun CoinReward(
    amount: Int
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(
                id = R.drawable.luna_coin_small
            ),
            contentDescription = "Luna Coin",
            modifier = Modifier.size(18.dp)
        )

        Text(
            text = " +$amount",
            style = MaterialTheme.typography.bodySmall
        )
    }
}