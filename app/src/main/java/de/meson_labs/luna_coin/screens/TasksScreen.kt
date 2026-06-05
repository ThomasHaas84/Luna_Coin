package de.meson_labs.luna_coin.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import de.meson_labs.luna_coin.components.CoinDisplay
import de.meson_labs.luna_coin.models.Child
import de.meson_labs.luna_coin.models.DayOfWeekName
import de.meson_labs.luna_coin.models.DogScheduleItem
import de.meson_labs.luna_coin.models.LunaCoinData
import de.meson_labs.luna_coin.models.TaskItem
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun TasksScreen(
    modifier: Modifier = Modifier,
    data: LunaCoinData,
    selectedChild: Child?,
    selectedDate: LocalDate,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit,
    onToday: () -> Unit,
    onCompleteTask: (String) -> Unit,
    onLogout: () -> Unit
) {
    val childId = selectedChild?.id
    val dateText = selectedDate.toString()

    val tasksForDate = data.tasks.filter { task ->
        task.date == dateText &&
                (
                        task.assignedChildId == null ||
                                task.assignedChildId == childId
                        )
    }

    val dogTasksForDay = data.dogSchedule.filter { dogTask ->
        dogTask.dayOfWeek == selectedDate.toDayOfWeekName()
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        item {
            HeaderRow(
                title = "Aufgaben",
                selectedChild = selectedChild,
                onLogout = onLogout
            )

            Spacer(modifier = Modifier.height(16.dp))

            DateSelector(
                selectedDate = selectedDate,
                onPreviousDay = onPreviousDay,
                onNextDay = onNextDay,
                onToday = onToday
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Haushaltsaufgaben",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(8.dp))
        }

        if (tasksForDate.isEmpty()) {
            item {
                EmptyCard(
                    text = "Für diesen Tag gibt es keine Aufgaben."
                )
            }
        } else {
            items(tasksForDate) { task ->
                TaskCard(
                    task = task,
                    children = data.children,
                    onCompleteTask = onCompleteTask
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))

            Divider()

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Hunde-Plan",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(8.dp))
        }

        if (dogTasksForDay.isEmpty()) {
            item {
                EmptyCard(
                    text = "Für diesen Tag ist kein Hunde-Dienst eingetragen."
                )
            }
        } else {
            items(dogTasksForDay) { dogTask ->
                val childName = data.children.firstOrNull { child ->
                    child.id == dogTask.childId
                }?.name ?: "Unbekannt"

                DogScheduleCard(
                    dogTask = dogTask,
                    childName = childName
                )
            }
        }
    }
}

@Composable
private fun HeaderRow(
    title: String,
    selectedChild: Child?,
    onLogout: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
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
                    style = MaterialTheme.typography.titleLarge
                )

                CoinDisplay(
                    amount = selectedChild?.coins ?: 0
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

@Composable
private fun DateSelector(
    selectedDate: LocalDate,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit,
    onToday: () -> Unit
) {
    val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

    val dayFormatter = DateTimeFormatter.ofPattern(
        "EEEE",
        Locale.GERMAN
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedButton(
            onClick = onPreviousDay
        ) {
            Text("<")
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = selectedDate.format(dayFormatter)
                    .replaceFirstChar {
                        it.uppercase()
                    },
                style = MaterialTheme.typography.headlineSmall
            )

            Text(
                text = selectedDate.format(dateFormatter),
                style = MaterialTheme.typography.titleLarge
            )

            AssistChip(
                onClick = onToday,
                enabled = selectedDate != LocalDate.now(),
                label = {
                    Text("Heute")
                }
            )
        }

        OutlinedButton(
            onClick = onNextDay
        ) {
            Text(">")
        }
    }
}

@Composable
private fun TaskCard(
    task: TaskItem,
    children: List<Child>,
    onCompleteTask: (String) -> Unit
) {
    val assignedName = task.assignedChildId?.let { childId ->
        children.firstOrNull { child ->
            child.id == childId
        }?.name
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = task.done,
                onCheckedChange = {
                    if (!task.done) {
                        onCompleteTask(task.id)
                    }
                },
                enabled = !task.done
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp)
            ) {
                AnimatedStrikeThroughText(
                    text = task.title,
                    strikeThrough = task.done,
                    style = MaterialTheme.typography.titleMedium
                )

                if (task.description.isNotBlank()) {
                    AnimatedStrikeThroughText(
                        text = task.description,
                        strikeThrough = task.done,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CoinDisplay(
                        amount = task.rewardCoins,
                        showPlus = true
                    )

                    if (assignedName != null) {
                        Text(
                            text = " · Für $assignedName",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                if (task.done) {
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

@Composable
private fun DogScheduleCard(
    dogTask: DogScheduleItem,
    childName: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = childName,
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = "Betreuung: ${dogTask.careStartTime} - ${dogTask.careEndTime} Uhr",
                style = MaterialTheme.typography.bodyLarge
            )

            Text(
                text = "Füttern: ${dogTask.feedingTime} Uhr",
                style = MaterialTheme.typography.bodyLarge
            )

            Text(
                text = "Gassi: ${dogTask.walkTime} Uhr",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
private fun EmptyCard(
    text: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(16.dp)
        )
    }
}

private fun LocalDate.toDayOfWeekName(): DayOfWeekName {
    return when (this.dayOfWeek.value) {
        1 -> DayOfWeekName.MONDAY
        2 -> DayOfWeekName.TUESDAY
        3 -> DayOfWeekName.WEDNESDAY
        4 -> DayOfWeekName.THURSDAY
        5 -> DayOfWeekName.FRIDAY
        6 -> DayOfWeekName.SATURDAY
        else -> DayOfWeekName.SUNDAY
    }
}