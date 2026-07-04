package de.meson_labs.luna_coin.components.tasks

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

@Composable
fun DateSelector(
    selectedDate: LocalDate,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit,
    onToday: () -> Unit,
    canGoToPreviousDay: Boolean,
    canGoToNextDay: Boolean
) {
    val configuration = LocalConfiguration.current
    val isTabletLayout = configuration.smallestScreenWidthDp >= 600
    val isPhone = !isTabletLayout

    val isToday = selectedDate == LocalDate.now()
    val daysDifference = ChronoUnit.DAYS.between(LocalDate.now(), selectedDate)

    val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    val dayFormatter = DateTimeFormatter.ofPattern("EEEE", Locale.GERMAN)

    if (isPhone) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 2.dp
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 12.dp,
                        vertical = 10.dp
                    ),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onPreviousDay,
                        enabled = canGoToPreviousDay,
                        modifier = Modifier.weight(0.8f)
                    ) {
                        Text("<")
                    }

                    Column(
                        modifier = Modifier.weight(2.4f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = selectedDate.format(dayFormatter)
                                .replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.titleLarge,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis
                        )

                        Text(
                            text = selectedDate.format(dateFormatter),
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            softWrap = false
                        )
                    }

                    OutlinedButton(
                        onClick = onNextDay,
                        enabled = canGoToNextDay,
                        modifier = Modifier.weight(0.8f)
                    ) {
                        Text(">")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                DayOffsetIndicator(
                    daysDifference = daysDifference,
                    compact = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                AssistChip(
                    onClick = onToday,
                    enabled = !isToday,
                    label = {
                        Text(
                            text = if (isToday) {
                                "Heute ausgewählt"
                            } else {
                                "zu Heute springen"
                            },
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                )
            }
        }
    } else {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onPreviousDay,
                    enabled = canGoToPreviousDay
                ) {
                    Text("<")
                }

                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = selectedDate.format(dayFormatter)
                            .replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.headlineMedium
                    )

                    Text(
                        text = selectedDate.format(dateFormatter),
                        style = MaterialTheme.typography.headlineSmall
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    AssistChip(
                        onClick = onToday,
                        enabled = !isToday,
                        label = {
                            Text(
                                text = if (isToday) {
                                    "Heute ausgewählt"
                                } else {
                                    "zu Heute springen"
                                },
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    )
                }

                OutlinedButton(
                    onClick = onNextDay,
                    enabled = canGoToNextDay
                ) {
                    Text(">")
                }
            }

            if (daysDifference < 0) {
                DayOffsetIndicator(
                    daysDifference = daysDifference,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 460.dp)
                )
            }

            if (daysDifference > 0) {
                DayOffsetIndicator(
                    daysDifference = daysDifference,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 460.dp)
                )
            }
        }
    }
}

@Composable
private fun DayOffsetIndicator(
    daysDifference: Long,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    val label = when {
        daysDifference == 0L -> "Heute"
        daysDifference == -1L -> "Gestern"
        daysDifference == 1L -> "Morgen"
        daysDifference < 0 -> "vor ${-daysDifference} Tagen"
        else -> "in $daysDifference Tagen"
    }

    val activeColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)
    val inactiveColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)

    val clamped = daysDifference.coerceIn(-2L, 2L).toInt()

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(if (compact) 6.dp else 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(5) { index ->
                val dotIndex = index - 2
                val isActive = dotIndex == clamped

                Box(
                    modifier = Modifier
                        .size(
                            when {
                                compact && isActive -> 10.dp
                                compact -> 7.dp
                                isActive -> 12.dp
                                else -> 8.dp
                            }
                        )
                        .clip(CircleShape)
                        .background(if (isActive) activeColor else inactiveColor)
                )
            }
        }

        Spacer(modifier = Modifier.height(if (compact) 4.dp else 5.dp))

        Text(
            text = label,
            style = if (compact) {
                MaterialTheme.typography.labelSmall
            } else {
                MaterialTheme.typography.labelMedium
            },
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis
        )
    }
}

