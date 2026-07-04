package de.meson_labs.luna_coin.components.tasks

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import de.meson_labs.luna_coin.components.CoinDisplay
import de.meson_labs.luna_coin.models.Child
import de.meson_labs.luna_coin.models.TaskAssignmentType
import de.meson_labs.luna_coin.models.TaskCompletionMode
import de.meson_labs.luna_coin.models.TaskItem
import de.meson_labs.luna_coin.models.TaskRepeatType
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@Composable
private fun dueDateColor(
    dueDateText: String
): Color {
    val dueDate = dueDateText.toLocalDateOrNull()
        ?: return MaterialTheme.colorScheme.tertiary

    val daysLeft = ChronoUnit.DAYS.between(
        LocalDate.now(),
        dueDate
    )

    return when {
        daysLeft < 0 -> MaterialTheme.colorScheme.error
        daysLeft <= 2 -> Color(0xFFF97316)
        daysLeft <= 7 -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
}

@Composable
fun TaskCard(
    task: TaskItem,
    children: List<Child>,
    selectedChild: Child?,
    selectedDate: LocalDate,
    onCompleteTask: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val isTabletLayout = configuration.smallestScreenWidthDp >= 600
    val isPhone = !isTabletLayout

    val cardVerticalPadding = if (isPhone) {
        4.dp
    } else {
        6.dp
    }

    val cardInnerPadding = if (isPhone) {
        12.dp
    } else {
        16.dp
    }

    val contentStartPadding = if (isPhone) {
        8.dp
    } else {
        12.dp
    }

    val rewardCoinSize = if (isPhone) {
        26.dp
    } else {
        32.dp
    }

    val titleStyle = if (isPhone) {
        MaterialTheme.typography.titleSmall
    } else {
        MaterialTheme.typography.titleMedium
    }

    val childId = selectedChild?.id
    val dateText = selectedDate.toString()

    val assignedName = task.assignedChildId?.let { assignedChildId ->
        children.firstOrNull { child ->
            child.id == assignedChildId
        }?.name
    }

    val assignmentText = when (task.assignmentType) {
        TaskAssignmentType.FREE_FOR_ALL -> {
            if (task.completionMode == TaskCompletionMode.ONCE_TOTAL) {
                "Für alle · einmal insgesamt"
            } else {
                "Für alle"
            }
        }

        TaskAssignmentType.ASSIGNED -> {
            "Für ${assignedName ?: "Unbekannt"}"
        }
    }

    val isDone = when (task.completionMode) {
        TaskCompletionMode.EACH_PERSON -> {
            if (task.repeatType == TaskRepeatType.ONCE) {
                task.completions.any { completion ->
                    completion.childId == childId
                }
            } else {
                task.completions.any { completion ->
                    completion.childId == childId &&
                            completion.date == dateText
                }
            }
        }

        TaskCompletionMode.ONCE_TOTAL -> {
            if (task.repeatType == TaskRepeatType.ONCE) {
                task.completions.isNotEmpty()
            } else {
                task.completions.any { completion ->
                    completion.date == dateText
                }
            }
        }
    }

    val canComplete =
        childId != null &&
                canTaskBeCompleted(
                    task = task,
                    childId = childId,
                    date = selectedDate
                )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = cardVerticalPadding),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(cardInnerPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isDone,
                onCheckedChange = {
                    if (canComplete) {
                        onCompleteTask()
                    }
                },
                enabled = canComplete || isDone
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = contentStartPadding)
            ) {
                AnimatedStrikeThroughText(
                    text = task.title,
                    strikeThrough = isDone,
                    style = titleStyle
                )

                if (task.description.isNotBlank()) {
                    AnimatedStrikeThroughText(
                        text = task.description,
                        strikeThrough = isDone,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                task.dueDate?.let { dueDate ->
                    Text(
                        text = "Fällig bis: ${formatDateGerman(dueDate)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = dueDateColor(dueDate)
                    )
                }

                if (isPhone) {
                    Column(
                        modifier = Modifier.padding(top = 2.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CoinDisplay(
                                amount = task.rewardCoins,
                                showPlus = true,
                                coinSize = rewardCoinSize
                            )

                            Text(
                                text = " · ${repeatTypeText(task.repeatType)}",
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Text(
                            text = assignmentText,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CoinDisplay(
                            amount = task.rewardCoins,
                            showPlus = true,
                            coinSize = rewardCoinSize
                        )

                        Text(
                            text = " · ${repeatTypeText(task.repeatType)}",
                            style = MaterialTheme.typography.bodySmall
                        )

                        Text(
                            text = " · $assignmentText",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (
                    task.repeatType == TaskRepeatType.DAILY &&
                    selectedDate != LocalDate.now()
                ) {
                    Text(
                        text = "Tägliche Aufgaben können nur heute abgehakt werden.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (task.repeatType == TaskRepeatType.ONCE) {
                    Text(
                        text = "Diese Aufgabe ist einmalig.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (isDone) {
                    Text(
                        text = "Erledigt",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
private fun AnimatedStrikeThroughText(
    text: String,
    strikeThrough: Boolean,
    style: TextStyle,
    modifier: Modifier = Modifier
) {
    val progress by animateFloatAsState(
        targetValue = if (strikeThrough) {
            1f
        } else {
            0f
        },
        animationSpec = tween(
            durationMillis = 700
        ),
        label = "strikeThroughAnimation"
    )

    val lineColor = MaterialTheme.colorScheme.onSurface.copy(
        alpha = 0.85f
    )

    Box(
        modifier = modifier
    ) {
        Text(
            text = text,
            style = style,
            color = if (strikeThrough) {
                MaterialTheme.colorScheme.onSurface.copy(
                    alpha = 0.65f
                )
            } else {
                Color.Unspecified
            }
        )

        Canvas(
            modifier = Modifier.matchParentSize()
        ) {
            if (progress > 0f) {
                val y = size.height * 0.58f

                drawLine(
                    color = lineColor,
                    start = Offset(
                        x = 0f,
                        y = y
                    ),
                    end = Offset(
                        x = size.width * progress,
                        y = y
                    ),
                    strokeWidth = 3.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }
    }
}


private fun canTaskBeCompleted(
    task: TaskItem,
    childId: String,
    date: LocalDate
): Boolean {
    if (
        task.repeatType == TaskRepeatType.DAILY &&
        date != LocalDate.now()
    ) {
        return false
    }

    if (
        task.assignmentType == TaskAssignmentType.ASSIGNED &&
        task.assignedChildId != childId
    ) {
        return false
    }

    return when (task.completionMode) {
        TaskCompletionMode.EACH_PERSON -> {
            if (task.repeatType == TaskRepeatType.ONCE) {
                task.completions.none { completion ->
                    completion.childId == childId
                }
            } else {
                task.completions.none { completion ->
                    completion.childId == childId &&
                            completion.date == date.toString()
                }
            }
        }

        TaskCompletionMode.ONCE_TOTAL -> {
            if (task.repeatType == TaskRepeatType.ONCE) {
                task.completions.isEmpty()
            } else {
                task.completions.none { completion ->
                    completion.date == date.toString()
                }
            }
        }
    }
}

private fun repeatTypeText(
    repeatType: TaskRepeatType
): String {
    return when (repeatType) {
        TaskRepeatType.ONCE -> "Einmalig"

        TaskRepeatType.DAILY -> "Täglich"

        TaskRepeatType.WEEKDAYS -> "Montag bis Freitag"

        TaskRepeatType.WEEKEND -> "Samstag bis Sonntag"

        TaskRepeatType.WEEKLY -> "Wöchentlich"

        TaskRepeatType.BIWEEKLY -> "Zweiwöchentlich"

        TaskRepeatType.MONTHLY -> "Monatlich"

        TaskRepeatType.YEARLY -> "Jährlich"

        TaskRepeatType.EVERY_TWO_YEARS -> "Alle zwei Jahre"
    }
}

private fun formatDateGerman(
    dateText: String
): String {
    val date = dateText.toLocalDateOrNull() ?: return dateText
    val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    return date.format(formatter)
}

private fun String.toLocalDateOrNull(): LocalDate? {
    return try {
        LocalDate.parse(this)
    } catch (_: Exception) {
        null
    }
}
