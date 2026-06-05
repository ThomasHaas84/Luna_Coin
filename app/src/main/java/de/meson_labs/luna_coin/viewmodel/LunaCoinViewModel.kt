package de.meson_labs.luna_coin.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import de.meson_labs.luna_coin.data.DemoData
import de.meson_labs.luna_coin.models.DayOfWeekName
import de.meson_labs.luna_coin.models.DogScheduleItem
import de.meson_labs.luna_coin.models.LogEntry
import de.meson_labs.luna_coin.models.LogType
import de.meson_labs.luna_coin.models.LunaCoinData
import de.meson_labs.luna_coin.models.ShopItem
import de.meson_labs.luna_coin.models.TaskAssignmentType
import de.meson_labs.luna_coin.models.TaskCompletion
import de.meson_labs.luna_coin.models.TaskCompletionMode
import de.meson_labs.luna_coin.models.TaskItem
import de.meson_labs.luna_coin.models.TaskRepeatType
import de.meson_labs.luna_coin.storage.LunaCoinStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.UUID

class LunaCoinViewModel(
    private val storage: LunaCoinStorage
) : ViewModel() {

    private val _data = MutableStateFlow(loadInitialData())
    val data: StateFlow<LunaCoinData> = _data.asStateFlow()

    private val _selectedChildId = MutableStateFlow<String?>(null)
    val selectedChildId: StateFlow<String?> = _selectedChildId.asStateFlow()

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    fun selectChild(childId: String) {
        _selectedChildId.value = childId
    }

    fun logout() {
        _selectedChildId.value = null
    }

    fun previousDay() {
        _selectedDate.value = _selectedDate.value.minusDays(1)
    }

    fun nextDay() {
        _selectedDate.value = _selectedDate.value.plusDays(1)
    }

    fun today() {
        _selectedDate.value = LocalDate.now()
    }

    fun completeTask(taskId: String) {
        val childId = _selectedChildId.value ?: return
        val currentData = _data.value
        val selectedDate = _selectedDate.value
        val selectedDateText = selectedDate.toString()

        val task = currentData.tasks.firstOrNull { task ->
            task.id == taskId
        } ?: return

        if (!isTaskVisibleForChildAndDate(task, childId, selectedDate)) {
            return
        }

        if (!canTaskBeCompleted(task, childId, selectedDate)) {
            return
        }

        val timestamp = nowText()

        val newCompletion = TaskCompletion(
            childId = childId,
            date = selectedDateText,
            timestamp = timestamp
        )

        val updatedTasks = currentData.tasks.map { currentTask ->
            if (currentTask.id == taskId) {
                currentTask.copy(
                    completions = currentTask.completions + newCompletion
                )
            } else {
                currentTask
            }
        }

        val updatedChildren = currentData.children.map { child ->
            if (child.id == childId) {
                child.copy(
                    coins = child.coins + task.rewardCoins
                )
            } else {
                child
            }
        }

        val child = currentData.children.firstOrNull { currentChild ->
            currentChild.id == childId
        }

        val log = LogEntry(
            id = uuid(),
            timestamp = timestamp,
            childId = childId,
            type = LogType.TASK_DONE,
            text = "${child?.name ?: "Kind"} hat Aufgabe erledigt: ${task.title}",
            coinChange = task.rewardCoins
        )

        updateData(
            currentData.copy(
                children = updatedChildren,
                tasks = updatedTasks,
                logs = listOf(log) + currentData.logs
            )
        )
    }

    fun buyShopItem(shopItemId: String) {
        val childId = _selectedChildId.value ?: return
        val currentData = _data.value

        val child = currentData.children.firstOrNull { currentChild ->
            currentChild.id == childId
        } ?: return

        val item = currentData.shopItems.firstOrNull { shopItem ->
            shopItem.id == shopItemId
        } ?: return

        if (child.coins < item.priceCoins) return

        val timestamp = nowText()

        val updatedChildren = currentData.children.map { currentChild ->
            if (currentChild.id == childId) {
                currentChild.copy(
                    coins = currentChild.coins - item.priceCoins
                )
            } else {
                currentChild
            }
        }

        val log = LogEntry(
            id = uuid(),
            timestamp = timestamp,
            childId = childId,
            type = LogType.SHOP_BUY,
            text = "${child.name} hat gekauft: ${item.title}",
            coinChange = -item.priceCoins
        )

        updateData(
            currentData.copy(
                children = updatedChildren,
                logs = listOf(log) + currentData.logs
            )
        )
    }

    fun undoLogEntry(logId: String) {
        val currentData = _data.value

        val log = currentData.logs.firstOrNull { entry ->
            entry.id == logId
        } ?: return

        val updatedChildren = currentData.children.map { child ->
            if (child.id == log.childId) {
                child.copy(
                    coins = child.coins - log.coinChange
                )
            } else {
                child
            }
        }

        val updatedLogs = currentData.logs.filterNot { entry ->
            entry.id == logId
        }

        updateData(
            currentData.copy(
                children = updatedChildren,
                logs = updatedLogs
            )
        )
    }

    fun addTask(
        title: String,
        description: String,
        rewardCoins: Int,
        assignmentType: TaskAssignmentType,
        completionMode: TaskCompletionMode,
        repeatType: TaskRepeatType,
        assignedChildId: String?,
        startDate: String,
        dueDate: String?,
        weeklyDay: DayOfWeekName?
    ) {
        val currentData = _data.value

        val newTask = TaskItem(
            id = uuid(),
            title = title,
            description = description,
            rewardCoins = rewardCoins,
            assignmentType = assignmentType,
            completionMode = completionMode,
            assignedChildId = if (assignmentType == TaskAssignmentType.ASSIGNED) {
                assignedChildId
            } else {
                null
            },
            repeatType = repeatType,
            startDate = startDate,
            dueDate = dueDate?.takeIf { it.isNotBlank() },
            weeklyDay = weeklyDay,
            completions = emptyList()
        )

        updateData(
            currentData.copy(
                tasks = currentData.tasks + newTask
            )
        )
    }

    fun updateTask(
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
        weeklyDay: DayOfWeekName?
    ) {
        val currentData = _data.value

        val updatedTasks = currentData.tasks.map { task ->
            if (task.id == taskId) {
                task.copy(
                    title = title,
                    description = description,
                    rewardCoins = rewardCoins,
                    assignmentType = assignmentType,
                    completionMode = completionMode,
                    assignedChildId = if (assignmentType == TaskAssignmentType.ASSIGNED) {
                        assignedChildId
                    } else {
                        null
                    },
                    repeatType = repeatType,
                    startDate = startDate,
                    dueDate = dueDate?.takeIf { it.isNotBlank() },
                    weeklyDay = weeklyDay
                )
            } else {
                task
            }
        }

        updateData(
            currentData.copy(
                tasks = updatedTasks
            )
        )
    }

    fun deleteTask(taskId: String) {
        val currentData = _data.value

        updateData(
            currentData.copy(
                tasks = currentData.tasks.filterNot { task ->
                    task.id == taskId
                }
            )
        )
    }

    fun addShopItem(
        title: String,
        description: String,
        priceCoins: Int
    ) {
        val currentData = _data.value

        val newItem = ShopItem(
            id = uuid(),
            title = title,
            description = description,
            priceCoins = priceCoins
        )

        updateData(
            currentData.copy(
                shopItems = currentData.shopItems + newItem
            )
        )
    }

    fun updateShopItem(
        itemId: String,
        title: String,
        description: String,
        priceCoins: Int
    ) {
        val currentData = _data.value

        val updatedItems = currentData.shopItems.map { item ->
            if (item.id == itemId) {
                item.copy(
                    title = title,
                    description = description,
                    priceCoins = priceCoins
                )
            } else {
                item
            }
        }

        updateData(
            currentData.copy(
                shopItems = updatedItems
            )
        )
    }

    fun deleteShopItem(itemId: String) {
        val currentData = _data.value

        updateData(
            currentData.copy(
                shopItems = currentData.shopItems.filterNot { item ->
                    item.id == itemId
                }
            )
        )
    }

    fun addDogSchedule(
        childId: String,
        dayOfWeek: DayOfWeekName,
        careStartTime: String,
        careEndTime: String,
        feedingTime: String,
        walkTime: String
    ) {
        val currentData = _data.value

        val newEntry = DogScheduleItem(
            id = uuid(),
            childId = childId,
            dayOfWeek = dayOfWeek,
            careStartTime = careStartTime,
            careEndTime = careEndTime,
            feedingTime = feedingTime,
            walkTime = walkTime
        )

        updateData(
            currentData.copy(
                dogSchedule = currentData.dogSchedule + newEntry
            )
        )
    }

    fun updateDogSchedule(
        scheduleId: String,
        childId: String,
        dayOfWeek: DayOfWeekName,
        careStartTime: String,
        careEndTime: String,
        feedingTime: String,
        walkTime: String
    ) {
        val currentData = _data.value

        val updatedSchedule = currentData.dogSchedule.map { entry ->
            if (entry.id == scheduleId) {
                entry.copy(
                    childId = childId,
                    dayOfWeek = dayOfWeek,
                    careStartTime = careStartTime,
                    careEndTime = careEndTime,
                    feedingTime = feedingTime,
                    walkTime = walkTime
                )
            } else {
                entry
            }
        }

        updateData(
            currentData.copy(
                dogSchedule = updatedSchedule
            )
        )
    }

    fun deleteDogSchedule(scheduleId: String) {
        val currentData = _data.value

        updateData(
            currentData.copy(
                dogSchedule = currentData.dogSchedule.filterNot { entry ->
                    entry.id == scheduleId
                }
            )
        )
    }

    fun resetDemoData() {
        updateData(DemoData.create())
        _selectedChildId.value = null
        _selectedDate.value = LocalDate.now()
    }

    fun getJsonText(): String {
        return storage.getJsonText()
    }

    private fun isTaskVisibleForChildAndDate(
        task: TaskItem,
        childId: String,
        date: LocalDate
    ): Boolean {
        if (task.assignmentType == TaskAssignmentType.ASSIGNED &&
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
        if (task.repeatType == TaskRepeatType.DAILY &&
            date != LocalDate.now()
        ) {
            return false
        }

        if (task.assignmentType == TaskAssignmentType.ASSIGNED &&
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

            TaskRepeatType.WEEKLY -> {
                task.weeklyDay == date.toDayOfWeekName()
            }

            TaskRepeatType.BIWEEKLY -> {
                task.weeklyDay == date.toDayOfWeekName() &&
                        ChronoUnit.WEEKS.between(startDate, date) % 2L == 0L
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

    private fun updateData(newData: LunaCoinData) {
        _data.value = newData
        storage.saveData(newData)
    }

    private fun loadInitialData(): LunaCoinData {
        val savedData = storage.loadData()

        return if (savedData == null || savedData.children.isEmpty()) {
            val demoData = DemoData.create()
            storage.saveData(demoData)
            demoData
        } else {
            val fixedData = savedData.copy(
                children = savedData.children.map { child ->
                    when (child.id) {
                        "parent_lisa" -> child.copy(
                            password = "6511"
                        )

                        "admin_thomas" -> child.copy(
                            password = "5761"
                        )

                        else -> child
                    }
                }
            )

            storage.saveData(fixedData)
            fixedData
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

    private fun String.toLocalDateOrNull(): LocalDate? {
        return try {
            LocalDate.parse(this)
        } catch (_: Exception) {
            null
        }
    }

    private fun nowText(): String {
        return LocalDateTime.now().format(
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")
        )
    }

    private fun uuid(): String {
        return UUID.randomUUID().toString()
    }
}

class LunaCoinViewModelFactory(
    private val storage: LunaCoinStorage
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {
        return LunaCoinViewModel(storage) as T
    }
}