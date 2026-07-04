package de.meson_labs.luna_coin.components.tasks

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import de.meson_labs.luna_coin.models.DogScheduleItem

@Composable
fun DogScheduleCard(
    dogTask: DogScheduleItem,
    childName: String
) {
    val configuration = LocalConfiguration.current
    val isTabletLayout = configuration.smallestScreenWidthDp >= 600
    val isPhone = !isTabletLayout

    val cardPadding = if (isPhone) {
        12.dp
    } else {
        16.dp
    }

    val verticalPadding = if (isPhone) {
        4.dp
    } else {
        6.dp
    }

    val childNameStyle = if (isPhone) {
        MaterialTheme.typography.titleSmall
    } else {
        MaterialTheme.typography.titleMedium
    }

    val timeTextStyle = if (isPhone) {
        MaterialTheme.typography.bodyMedium
    } else {
        MaterialTheme.typography.bodyLarge
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = verticalPadding),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(cardPadding),
            verticalArrangement = Arrangement.spacedBy(if (isPhone) 3.dp else 4.dp)
        ) {
            Text(
                text = childName,
                style = childNameStyle,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = "Betreuung: ${dogTask.careStartTime} - ${dogTask.careEndTime} Uhr",
                style = timeTextStyle,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = "Füttern: ${dogTask.feedingTime} Uhr",
                style = timeTextStyle,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = "Gassi: ${dogTask.walkTime} Uhr",
                style = timeTextStyle,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

