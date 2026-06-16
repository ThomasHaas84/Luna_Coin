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
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.text.TextStyle
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
    canGoToPreviousDay: Boolean,   // ← neu
    canGoToNextDay: Boolean        // ← neu
) {
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
            .padding(24.dp)
    ) {
        item {
            LunaScreenHeader(
                title = "Aufgaben",
                selectedChild = selectedChild,
                onLogout = onLogout
            )

            Spacer(modifier = Modifier.height(16.dp))

            DateSelector(
                selectedDate = selectedDate,
                onPreviousDay = onPreviousDay,
                onNextDay = onNextDay,
                onToday = onToday,
                canGoToPreviousDay = canGoToPreviousDay,   // ← neu
                canGoToNextDay = canGoToNextDay            // ← neu
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
                    selectedChild = selectedChild,
                    selectedDate = selectedDate,
                    onCompleteTask = { taskToComplete = task }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))

            Divider()

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Hund-Plan",
                style = MaterialTheme.typography.headlineSmall
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

    // ==================== ZENTRALISIERTER DIALOG ====================
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
    canGoToPreviousDay: Boolean,   // ← neu
    canGoToNextDay: Boolean        // ← neu
) {
    val isToday = selectedDate == LocalDate.now()
    val daysDifference = ChronoUnit.DAYS.between(LocalDate.now(), selectedDate)

    val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    val dayFormatter = DateTimeFormatter.ofPattern("EEEE", Locale.GERMAN)

    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        // ==================== STABILES HAUPT-LAYOUT ====================
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = onPreviousDay,
                enabled = canGoToPreviousDay          // ← deaktiviert bei Limit
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
                    label = { Text("Heute") }
                )
            }

            OutlinedButton(
                onClick = onNextDay,
                enabled = canGoToNextDay              // ← deaktiviert bei Limit
            ) {
                Text(">")
            }
        }

        // ==================== PUNKTE-INDIKATOR (Overlay) ====================
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

@Composable
private fun DayOffsetIndicator(
    daysDifference: Long,
    modifier: Modifier = Modifier
) {
    val label = when {
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
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(5) { index ->
                val dotIndex = index - 2
                val isActive = dotIndex == clamped

                Box(
                    modifier = Modifier
                        .size(if (isActive) 12.dp else 8.dp)
                        .clip(CircleShape)
                        .background(if (isActive) activeColor else inactiveColor)
                )
            }
        }

        Spacer(modifier = Modifier.height(5.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
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
            task.completions.any { completion ->
                completion.childId == childId &&
                        completion.date == dateText
            }
        }

        TaskCompletionMode.ONCE_TOTAL -> {
            task.completions.any { completion ->
                completion.date == dateText
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
            task.completions.none { completion ->
                completion.childId == childId &&
                        completion.date == date.toString()
            }
        }

        TaskCompletionMode.ONCE_TOTAL -> {
            task.completions.none { completion ->
                completion.date == date.toString()
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

    return when (task.repeatType) {
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