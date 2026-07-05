package de.meson_labs.luna_coin.manager

import de.meson_labs.luna_coin.data.repository.DataRepository
import de.meson_labs.luna_coin.models.Child
import de.meson_labs.luna_coin.models.LogEntry
import de.meson_labs.luna_coin.models.LogType
import de.meson_labs.luna_coin.models.DayOfWeekName
import de.meson_labs.luna_coin.models.LunaCoinData
import de.meson_labs.luna_coin.models.TaskAssignmentType
import de.meson_labs.luna_coin.models.TaskCompletion
import de.meson_labs.luna_coin.models.TaskCompletionMode
import de.meson_labs.luna_coin.models.TaskItem
import de.meson_labs.luna_coin.models.TaskRepeatType
import java.time.DayOfWeek
import java.time.LocalDate

class TaskManager(
    private val repository: DataRepository
) {

    fun prepareAddTask(
        currentData: LunaCoinData,
        title: String,
        description: String,
        rewardCoins: Int,
        assignmentType: TaskAssignmentType,
        completionMode: TaskCompletionMode,
        repeatType: TaskRepeatType,
        assignedChildId: String?,
        startDate: String,
        dueDate: String?,
        weeklyDay: DayOfWeekName?,
        watchlist: Boolean
    ): TaskOperation {
        val newTask = TaskItem(
            id = uuid(),
            title = title,
            description = description,
            rewardCoins = rewardCoins,
            assignmentType = assignmentType,
            completionMode = completionMode,
            assignedChildId = if (assignmentType == TaskAssignmentType.ASSIGNED) assignedChildId else null,
            repeatType = repeatType,
            startDate = startDate,
            dueDate = dueDate?.takeIf { it.isNotBlank() },
            weeklyDay = weeklyDay,
            completions = emptyList(),
            watchlist = watchlist
        )

        return TaskOperation(
            originalData = currentData,
            optimisticData = currentData.copy(
                tasks = currentData.tasks + newTask
            ),
            task = newTask
        )
    }

    fun prepareUpdateTask(
        currentData: LunaCoinData,
        taskId: String,
        title: String,
        description: String,
        rewardCoins: Int,
        assignmentType: TaskAssignmentType,
        completionMode: TaskCompletionMode,
        repeatType: TaskRepeatType,
        assignedChildId: String?,
        startDate: String,
        dueDate: String?,
        weeklyDay: DayOfWeekName?,
        watchlist: Boolean
    ): TaskOperation? {
        var updatedTask: TaskItem? = null

        val updatedTasks = currentData.tasks.map { task ->
            if (task.id == taskId) {
                task.copy(
                    title = title,
                    description = description,
                    rewardCoins = rewardCoins,
                    assignmentType = assignmentType,
                    completionMode = completionMode,
                    assignedChildId = if (assignmentType == TaskAssignmentType.ASSIGNED) assignedChildId else null,
                    repeatType = repeatType,
                    startDate = startDate,
                    dueDate = dueDate?.takeIf { it.isNotBlank() },
                    weeklyDay = weeklyDay,
                    watchlist = watchlist
                ).also {
                    updatedTask = it
                }
            } else {
                task
            }
        }

        val safeUpdatedTask = updatedTask ?: return null

        return TaskOperation(
            originalData = currentData,
            optimisticData = currentData.copy(
                tasks = updatedTasks
            ),
            task = safeUpdatedTask
        )
    }

    fun prepareDeleteTask(
        currentData: LunaCoinData,
        taskId: String
    ): DeleteTaskOperation {
        return DeleteTaskOperation(
            originalData = currentData,
            optimisticData = currentData.copy(
                tasks = currentData.tasks.filterNot { it.id == taskId }
            ),
            taskId = taskId
        )
    }

    fun prepareCompleteTask(
        currentData: LunaCoinData,
        taskId: String,
        childId: String,
        selectedDate: LocalDate
    ): CompleteTaskOperation? {
        val selectedDateText = selectedDate.toString()
        val task = currentData.tasks.firstOrNull { it.id == taskId } ?: return null
        val child = currentData.children.firstOrNull { it.id == childId } ?: return null

        if (!isTaskVisibleForChildAndDate(task, childId, selectedDate)) return null
        if (!canTaskBeCompleted(task, childId, selectedDate)) return null

        val timestamp = nowText()

        val newCompletion = TaskCompletion(
            childId = childId,
            date = selectedDateText,
            timestamp = timestamp
        )

        val updatedTask = task.copy(
            completions = task.completions + newCompletion
        )

        val log = LogEntry(
            id = uuid(),
            timestamp = timestamp,
            childId = childId,
            type = LogType.TASK_DONE,
            text = "${child.name} hat Aufgabe erledigt: ${task.title}",
            coinChange = task.rewardCoins
        )

        val experienceDelta = task.rewardCoins.coerceAtLeast(0)

        val optimisticChild = ProgressManager.addTaskReward(
            child = child,
            rewardCoins = task.rewardCoins
        )

        return CompleteTaskOperation(
            originalData = currentData,
            optimisticData = sortChildrenInData(
                currentData.copy(
                    children = currentData.children.map { c ->
                        if (c.id == childId) optimisticChild else c
                    },
                    tasks = currentData.tasks.map { t ->
                        if (t.id == taskId) updatedTask else t
                    },
                    logs = addLogToList(log, currentData.logs)
                )
            ),
            updatedTask = updatedTask,
            log = log,
            childId = childId,
            coinDelta = task.rewardCoins,
            experienceDelta = experienceDelta,
            updatedChild = optimisticChild
        )
    }

    suspend fun persistCompleteTask(operation: CompleteTaskOperation): Child {
        val persistedChild = repository.changeChildCoinsAndExperience(
            childId = operation.childId,
            coinDelta = operation.coinDelta,
            experienceDelta = operation.experienceDelta
        )

        repository.saveTask(operation.updatedTask)
        repository.saveLog(operation.log)

        return persistedChild
    }

    fun applyPersistedChildProgress(
        currentData: LunaCoinData,
        persistedChild: Child
    ): LunaCoinData {
        return sortChildrenInData(
            currentData.copy(
                children = currentData.children.map { child ->
                    if (child.id == persistedChild.id) {
                        child.copy(
                            coins = persistedChild.coins,
                            level = persistedChild.level,
                            experience = persistedChild.experience,
                            availableSkillPoints = persistedChild.availableSkillPoints,
                            intelligence = persistedChild.intelligence,
                            strength = persistedChild.strength,
                            agility = persistedChild.agility
                        )
                    } else {
                        child
                    }
                }
            )
        )
    }


    fun isTaskVisibleForChildAndDate(task: TaskItem, childId: String, date: LocalDate): Boolean {
        if (task.assignmentType == TaskAssignmentType.ASSIGNED && task.assignedChildId != childId) return false
        if (!isTaskDueOnDate(task, date)) return false

        if (task.repeatType == TaskRepeatType.ONCE) {
            return when (task.completionMode) {
                TaskCompletionMode.EACH_PERSON -> task.completions.none { it.childId == childId }
                TaskCompletionMode.ONCE_TOTAL -> task.completions.isEmpty()
            }
        }

        if (task.completionMode == TaskCompletionMode.ONCE_TOTAL && task.completions.any { it.date == date.toString() }) return false

        return true
    }

    fun canTaskBeCompleted(task: TaskItem, childId: String, date: LocalDate): Boolean {
        if (task.repeatType == TaskRepeatType.DAILY && date != LocalDate.now()) return false
        if (task.assignmentType == TaskAssignmentType.ASSIGNED && task.assignedChildId != childId) return false

        return when (task.completionMode) {
            TaskCompletionMode.EACH_PERSON -> {
                if (task.repeatType == TaskRepeatType.ONCE) {
                    task.completions.none { it.childId == childId }
                } else {
                    task.completions.none { it.childId == childId && it.date == date.toString() }
                }
            }

            TaskCompletionMode.ONCE_TOTAL -> {
                if (task.repeatType == TaskRepeatType.ONCE) {
                    task.completions.isEmpty()
                } else {
                    task.completions.none { it.date == date.toString() }
                }
            }
        }
    }

    fun isTaskDueOnDate(task: TaskItem, date: LocalDate): Boolean {
        val start = task.startDate.toLocalDateOrNull() ?: return true

        if (date.isBefore(start)) return false

        val dueDate = task.dueDate?.toLocalDateOrNull()
        if (dueDate != null && date.isAfter(dueDate)) return false

        return when (task.repeatType) {
            TaskRepeatType.ONCE -> date == start
            TaskRepeatType.DAILY -> true
            TaskRepeatType.WEEKDAYS -> {
                date.dayOfWeek != DayOfWeek.SATURDAY &&
                        date.dayOfWeek != DayOfWeek.SUNDAY
            }
            TaskRepeatType.WEEKEND -> {
                date.dayOfWeek == DayOfWeek.SATURDAY ||
                        date.dayOfWeek == DayOfWeek.SUNDAY
            }
            TaskRepeatType.WEEKLY -> {
                val weeklyDay = task.weeklyDay ?: return true
                dayOfWeekNameToJavaDayOfWeek(weeklyDay) == date.dayOfWeek
            }
            TaskRepeatType.BIWEEKLY -> {
                val weeklyDay = task.weeklyDay ?: return true
                if (dayOfWeekNameToJavaDayOfWeek(weeklyDay) != date.dayOfWeek) return false

                val weeksBetween = java.time.temporal.ChronoUnit.WEEKS.between(start, date)
                weeksBetween % 2L == 0L
            }
            TaskRepeatType.MONTHLY -> date.dayOfMonth == start.dayOfMonth
            TaskRepeatType.YEARLY -> {
                date.dayOfMonth == start.dayOfMonth &&
                        date.monthValue == start.monthValue
            }
            TaskRepeatType.EVERY_TWO_YEARS -> {
                date.dayOfMonth == start.dayOfMonth &&
                        date.monthValue == start.monthValue &&
                        (date.year - start.year) % 2 == 0
            }
        }
    }

    fun dayOfWeekNameToJavaDayOfWeek(day: DayOfWeekName): DayOfWeek {
        return when (day) {
            DayOfWeekName.MONDAY -> DayOfWeek.MONDAY
            DayOfWeekName.TUESDAY -> DayOfWeek.TUESDAY
            DayOfWeekName.WEDNESDAY -> DayOfWeek.WEDNESDAY
            DayOfWeekName.THURSDAY -> DayOfWeek.THURSDAY
            DayOfWeekName.FRIDAY -> DayOfWeek.FRIDAY
            DayOfWeekName.SATURDAY -> DayOfWeek.SATURDAY
            DayOfWeekName.SUNDAY -> DayOfWeek.SUNDAY
        }
    }

    suspend fun persistAddTask(operation: TaskOperation) {
        repository.saveTask(operation.task)
    }

    suspend fun persistUpdateTask(operation: TaskOperation) {
        repository.saveTask(operation.task)
    }

    suspend fun persistDeleteTask(operation: DeleteTaskOperation) {
        repository.deleteTask(operation.taskId)
    }

    private fun String.toLocalDateOrNull(): LocalDate? {
        return try {
            LocalDate.parse(this)
        } catch (_: Exception) {
            null
        }
    }

}

data class TaskOperation(
    val originalData: LunaCoinData,
    val optimisticData: LunaCoinData,
    val task: TaskItem
)

data class CompleteTaskOperation(
    val originalData: LunaCoinData,
    val optimisticData: LunaCoinData,
    val updatedTask: TaskItem,
    val log: LogEntry,
    val childId: String,
    val coinDelta: Int,
    val experienceDelta: Int,
    val updatedChild: Child
)

data class DeleteTaskOperation(
    val originalData: LunaCoinData,
    val optimisticData: LunaCoinData,
    val taskId: String
)
