package de.meson_labs.luna_coin.lunarim

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun LunarimBottomBar(
    currentDestination: LunarimDestination,
    onDestinationSelected: (LunarimDestination) -> Unit
) {
    NavigationBar(
        modifier = Modifier
            .fillMaxWidth()
            .height(68.dp)
            .padding(bottom = 4.dp),
        windowInsets = WindowInsets(0, 0, 0, 0)
    ) {
        LunarimDestination.entries.forEach { destination ->
            NavigationBarItem(
                selected = currentDestination == destination,
                onClick = {
                    onDestinationSelected(destination)
                },
                icon = {
                    Text(
                        text = destination.symbol,
                        fontSize = 21.sp
                    )
                },
                label = {
                    Text(
                        text = destination.title,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = 11.sp
                    )
                },
                alwaysShowLabel = true
            )
        }
    }
}
