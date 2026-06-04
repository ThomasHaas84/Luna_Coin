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
import de.meson_labs.luna_coin.models.TaskItem
import de.meson_labs.luna_coin.storage.LunaCoinStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
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
        val task = currentData.tasks.firstOrNull { task ->
            task.id == taskId
        } ?: return

        if (task.done) return
        if (task.assignedChildId != null && task.assignedChildId != childId) return

        val timestamp = nowText()

        val updatedTasks = currentData.tasks.map { currentTask ->
            if (currentTask.id == taskId) {
                currentTask.copy(
                    done = true,
                    doneByChildId = childId,
                    doneTimestamp = timestamp
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

    fun addTask(
        title: String,
        description: String,
        rewardCoins: Int,
        date: String
    ) {
        val currentData = _data.value

        val newTask = TaskItem(
            id = uuid(),
            title = title,
            description = description,
            rewardCoins = rewardCoins,
            assignedChildId = null,
            date = date,
            done = false
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
        date: String
    ) {
        val currentData = _data.value

        val updatedTasks = currentData.tasks.map { task ->
            if (task.id == taskId) {
                task.copy(
                    title = title,
                    description = description,
                    rewardCoins = rewardCoins,
                    date = date
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
            savedData
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