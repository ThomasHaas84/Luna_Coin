package de.meson_labs.luna_coin.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.meson_labs.luna_coin.models.Child

@Composable
fun LunaScreenHeader(
    title: String,
    selectedChild: Child?,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp
    val isPhone = screenWidthDp < 600

    val titleStyle = if (isPhone) {
        MaterialTheme.typography.headlineSmall
    } else {
        MaterialTheme.typography.displaySmall
    }

    val childNameStyle = if (isPhone) {
        MaterialTheme.typography.titleMedium
    } else {
        MaterialTheme.typography.headlineSmall
    }

    val coinSize = if (isPhone) {
        38.dp
    } else {
        60.dp
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = if (isPhone) 56.dp else 80.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = titleStyle,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(top = if (isPhone) 2.dp else 4.dp)
            ) {
                Text(
                    text = selectedChild?.name ?: "",
                    style = childNameStyle,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis
                )

                CoinDisplay(
                    amount = selectedChild?.coins ?: 0,
                    coinSize = coinSize
                )
            }
        }

        OutlinedButton(
            onClick = onLogout,
            modifier = Modifier.heightIn(
                min = if (isPhone) 38.dp else 44.dp
            )
        ) {
            Text(
                text = "Benutzer wechseln",
                fontSize = if (isPhone) 11.sp else 14.sp,
                maxLines = 1,
                softWrap = false
            )
        }
    }
}