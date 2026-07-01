package de.meson_labs.luna_coin.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import de.meson_labs.luna_coin.components.CoinDisplay
import de.meson_labs.luna_coin.components.LunaScreenHeader
import de.meson_labs.luna_coin.components.dialogs.ConfirmationDialog
import de.meson_labs.luna_coin.models.Child
import de.meson_labs.luna_coin.models.DayOfWeekName
import de.meson_labs.luna_coin.models.DogScheduleItem
import de.meson_labs.luna_coin.models.LunaCoinData
import de.meson_labs.luna_coin.models.TaskAssignmentType
import de.meson_labs.luna_coin.models.TaskCompletionMode
import de.meson_labs.luna_coin.models.TaskItem
import de.meson_labs.luna_coin.models.TaskRepeatType
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
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
    onLogout: () -> Unit,
    canGoToPreviousDay: Boolean,
    canGoToNextDay: Boolean
) {
    val configuration = LocalConfiguration.current
    val isTabletLayout = configuration.screenWidthDp >= 900
    val isPhone = !isTabletLayout

    val screenPadding = if (isPhone) {
        14.dp
    } else {
        24.dp
    }

    val childId = selectedChild?.id
    val selectedDay = selectedDate.toDayOfWeekName()

    val tasksForDate = data.tasks.filter { task ->
        if (childId == null) {
            false
        } else {
            isTaskVisibleForChildAndDate(
                task = task,
                childId = childId,
                date = selectedDate
            )
        }
    }

    val dogTasksForDay = data.dogSchedule.filter { dogTask ->
        dogTask.dayOfWeek == selectedDay
    }

    var taskToComplete by remember { mutableStateOf<TaskItem?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(screenPadding)
    ) {
        item {
            LunaScreenHeader(
                title = "Aufgaben",
                selectedChild = selectedChild,
                onLogout = onLogout
            )

            Spacer(modifier = Modifier.height(if (isPhone) 12.dp else 16.dp))

            DateSelector(
                selectedDate = selectedDate,
                onPreviousDay = onPreviousDay,
                onNextDay = onNextDay,
                onToday = onToday,
                canGoToPreviousDay = canGoToPreviousDay,
                canGoToNextDay = canGoToNextDay
            )

            Spacer(modifier = Modifier.height(if (isPhone) 18.dp else 24.dp))

            Text(
                text = "Haushaltsaufgaben",
                style = if (isPhone) {
                    MaterialTheme.typography.titleLarge
                } else {
                    MaterialTheme.typography.headlineSmall
                }
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
                    selectedChild = selectedChild,
                    selectedDate = selectedDate,
                    onCompleteTask = { taskToComplete = task }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(if (isPhone) 18.dp else 24.dp))

            Divider()

            Spacer(modifier = Modifier.height(if (isPhone) 18.dp else 24.dp))

            Text(
                text = "Hund-Plan",
                style = if (isPhone) {
                    MaterialTheme.typography.titleLarge
                } else {
                    MaterialTheme.typography.headlineSmall
                }
            )

            Spacer(modifier = Modifier.height(8.dp))
        }

        if (dogTasksForDay.isEmpty()) {
            item {
                EmptyCard(
                    text = "Für diesen Tag ist kein Hund-Dienst eingetragen."
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

    taskToComplete?.let { task ->
        ConfirmationDialog(
            title = "Aufgabe abschließen?",
            message = "Möchtest du '${task.title}' wirklich als erledigt markieren?",
            confirmText = "Ja, erledigt",
            dismissText = "Abbrechen",
            onConfirm = {
                onCompleteTask(task.id)
                taskToComplete = null
            },
            onDismiss = {
                taskToComplete = null
            }
        )
    }
}

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
private fun DateSelector(
    selectedDate: LocalDate,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit,
    onToday: () -> Unit,
    canGoToPreviousDay: Boolean,
    canGoToNextDay: Boolean
) {
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp
    val isTabletLayout = screenWidthDp >= 900
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

@Composable
private fun TaskCard(
    task: TaskItem,
    children: List<Child>,
    selectedChild: Child?,
    selectedDate: LocalDate,
    onCompleteTask: () -> Unit
) {
    val childId = selectedChild?.id
    val dateText = selectedDate.toString()

    val assignedName = task.assignedChildId?.let { assignedChildId ->
        children.firstOrNull { child ->
            child.id == assignedChildId
        }?.name
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
                    .padding(start = 12.dp)
            ) {
                AnimatedStrikeThroughText(
                    text = task.title,
                    strikeThrough = isDone,
                    style = MaterialTheme.typography.titleMedium
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

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CoinDisplay(
                        amount = task.rewardCoins,
                        showPlus = true,
                        coinSize = 32.dp
                    )

                    Text(
                        text = " · ${repeatTypeText(task.repeatType)}",
                        style = MaterialTheme.typography.bodySmall
                    )

                    when (task.assignmentType) {
                        TaskAssignmentType.FREE_FOR_ALL -> {
                            Text(
                                text = if (task.completionMode == TaskCompletionMode.ONCE_TOTAL) {
                                    " · Für alle · einmal insgesamt"
                                } else {
                                    " · Für alle"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        TaskAssignmentType.ASSIGNED -> {
                            Text(
                                text = " · Für ${assignedName ?: "Unbekannt"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
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

private fun isTaskVisibleForChildAndDate(
    task: TaskItem,
    childId: String,
    date: LocalDate
): Boolean {
    if (
        task.assignmentType == TaskAssignmentType.ASSIGNED &&
        task.assignedChildId != childId
    ) {
        return false
    }

    if (!isTaskDueOnDate(task, date)) {
        return false
    }

    if (task.repeatType == TaskRepeatType.ONCE) {
        return when (task.completionMode) {
            TaskCompletionMode.EACH_PERSON -> {
                task.completions.none { completion ->
                    completion.childId == childId
                }
            }

            TaskCompletionMode.ONCE_TOTAL -> {
                task.completions.isEmpty()
            }
        }
    }

    if (task.completionMode == TaskCompletionMode.ONCE_TOTAL) {
        val alreadyCompletedOnDate = task.completions.any { completion ->
            completion.date == date.toString()
        }

        if (alreadyCompletedOnDate) {
            return false
        }
    }

    return true
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

private fun isTaskDueOnDate(
    task: TaskItem,
    date: LocalDate
): Boolean {
    val startDate = task.startDate.toLocalDateOrNull() ?: return true

    if (date.isBefore(startDate)) {
        return false
    }

    val dueDate = task.dueDate?.toLocalDateOrNull()
    if (dueDate != null && date.isAfter(dueDate)) {
        return false
    }

    return when (task.repeatType) {
        TaskRepeatType.ONCE -> {
            date == startDate
        }

        TaskRepeatType.DAILY -> {
            true
        }

        TaskRepeatType.WEEKDAYS -> {
            date.dayOfWeek.value in 1..5
        }

        TaskRepeatType.WEEKEND -> {
            date.dayOfWeek.value == 6 ||
                    date.dayOfWeek.value == 7
        }

        TaskRepeatType.WEEKLY -> {
            task.weeklyDay == date.toDayOfWeekName()
        }

        TaskRepeatType.BIWEEKLY -> {
            task.weeklyDay == date.toDayOfWeekName() &&
                    ChronoUnit.WEEKS.between(startDate, date) % 2L == 0L
        }

        TaskRepeatType.MONTHLY -> {
            date.dayOfMonth == startDate.dayOfMonth
        }

        TaskRepeatType.YEARLY -> {
            date.month == startDate.month &&
                    date.dayOfMonth == startDate.dayOfMonth
        }

        TaskRepeatType.EVERY_TWO_YEARS -> {
            date.month == startDate.month &&
                    date.dayOfMonth == startDate.dayOfMonth &&
                    ChronoUnit.YEARS.between(startDate, date) % 2L == 0L
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

private fun String.toLocalDateOrNull(): LocalDate? {
    return try {
        LocalDate.parse(this)
    } catch (_: Exception) {
        null
    }
}