package de.meson_labs.luna_coin.components.tasks

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp

@Composable
fun EmptyCard(
    text: String
) {
    val configuration = LocalConfiguration.current
    val isTabletLayout = configuration.smallestScreenWidthDp >= 600
    val isPhone = !isTabletLayout

    val verticalPadding = if (isPhone) {
        4.dp
    } else {
        6.dp
    }

    val innerPadding = if (isPhone) {
        12.dp
    } else {
        16.dp
    }

    val textStyle = if (isPhone) {
        MaterialTheme.typography.bodyMedium
    } else {
        MaterialTheme.typography.bodyLarge
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = verticalPadding),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        )
    ) {
        Text(
            text = text,
            style = textStyle,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(innerPadding)
        )
    }
}
