package de.meson_labs.luna_coin.components.tasks

import de.meson_labs.luna_coin.models.DayOfWeekName
import de.meson_labs.luna_coin.models.TaskAssignmentType
import de.meson_labs.luna_coin.models.TaskCompletionMode
import de.meson_labs.luna_coin.models.TaskItem
import de.meson_labs.luna_coin.models.TaskRepeatType
import java.time.LocalDate
import java.time.temporal.ChronoUnit

fun isTaskVisibleForChildAndDate(
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

fun LocalDate.toDayOfWeekName(): DayOfWeekName {
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

private fun String.toLocalDateOrNull(): LocalDate? {
    return try {
        LocalDate.parse(this)
    } catch (_: Exception) {
        null
    }
}
