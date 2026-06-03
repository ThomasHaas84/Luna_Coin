package de.meson_labs.luna_coin.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import de.meson_labs.luna_coin.models.Child
import de.meson_labs.luna_coin.models.DayOfWeekName
import de.meson_labs.luna_coin.models.DogScheduleItem
import de.meson_labs.luna_coin.models.DogTaskType
import de.meson_labs.luna_coin.models.LogEntry
import de.meson_labs.luna_coin.models.LogType
import de.meson_labs.luna_coin.models.LunaCoinData
import de.meson_labs.luna_coin.models.ShopItem
import de.meson_labs.luna_coin.models.TaskItem
import de.meson_labs.luna_coin.models.UserRole
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
        val task = currentData.tasks.firstOrNull { it.id == taskId } ?: return

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

        val child = currentData.children.firstOrNull { it.id == childId }

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
        val child = currentData.children.firstOrNull { it.id == childId } ?: return
        val item = currentData.shopItems.firstOrNull { it.id == shopItemId } ?: return

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

    fun resetDemoData() {
        updateData(createDemoData())
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
            val demoData = createDemoData()
            storage.saveData(demoData)
            demoData
        } else {
            savedData
        }
    }

    private fun createDemoData(): LunaCoinData {
        val today = LocalDate.now()
        val dateText = today.toString()

        val clara = Child(id = "child_clara", name = "Clara", coins = 0, role = UserRole.CHILD)
        val jakob = Child(id = "child_jakob", name = "Jakob", coins = 0, role = UserRole.CHILD)
        val lukas = Child(id = "child_lukas", name = "Lukas", coins = 0, role = UserRole.CHILD)
        val noah = Child(id = "child_noah", name = "Noah", coins = 0, role = UserRole.CHILD)
        val max = Child(id = "child_max", name = "Max", coins = 0, role = UserRole.CHILD)
        val felix = Child(id = "child_felix", name = "Felix", coins = 0, role = UserRole.CHILD)
        val marie = Child(id = "child_marie", name = "Marie", coins = 0, role = UserRole.CHILD)

        val lisa = Child(id = "parent_lisa", name = "Lisa", coins = 0, role = UserRole.PARENT)
        val thomas = Child(id = "admin_thomas", name = "Thomas", coins = 0, role = UserRole.ADMIN)

        val children = listOf(
            clara,
            jakob,
            lukas,
            noah,
            max,
            felix,
            marie,
            lisa,
            thomas
        )

        val tasks = listOf(
            TaskItem(
                id = uuid(),
                title = "Tisch decken",
                description = "Vor dem Essen Teller und Besteck auf den Tisch legen.",
                rewardCoins = 2,
                assignedChildId = null,
                date = dateText
            ),
            TaskItem(
                id = uuid(),
                title = "Spülmaschine ausräumen",
                description = "Sauberes Geschirr einsortieren.",
                rewardCoins = 4,
                assignedChildId = null,
                date = dateText
            ),
            TaskItem(
                id = uuid(),
                title = "Zimmer aufräumen",
                description = "Boden frei räumen und Spielsachen einsortieren.",
                rewardCoins = 5,
                assignedChildId = null,
                date = dateText
            ),
            TaskItem(
                id = uuid(),
                title = "Müll rausbringen",
                description = "Mülleimer leeren.",
                rewardCoins = 3,
                assignedChildId = null,
                date = dateText
            )
        )

        val shopItems = listOf(
            ShopItem(
                id = uuid(),
                title = "30 Minuten Tablet-Zeit",
                description = "Zusätzliche Spielzeit am Tablet.",
                priceCoins = 10
            ),
            ShopItem(
                id = uuid(),
                title = "Filmabend aussuchen",
                description = "Du darfst den Film für den nächsten Filmabend auswählen.",
                priceCoins = 20
            ),
            ShopItem(
                id = uuid(),
                title = "Kleines Spielzeug",
                description = "Ein kleines Spielzeug oder eine kleine Überraschung.",
                priceCoins = 40
            )
        )

        val dogSchedule = listOf(
            DogScheduleItem(
                id = uuid(),
                childId = clara.id,
                dayOfWeek = DayOfWeekName.MONDAY,
                time = "07:30",
                type = DogTaskType.WALK
            ),
            DogScheduleItem(
                id = uuid(),
                childId = jakob.id,
                dayOfWeek = DayOfWeekName.MONDAY,
                time = "18:00",
                type = DogTaskType.FEED
            ),
            DogScheduleItem(
                id = uuid(),
                childId = lukas.id,
                dayOfWeek = DayOfWeekName.TUESDAY,
                time = "07:30",
                type = DogTaskType.WALK
            ),
            DogScheduleItem(
                id = uuid(),
                childId = noah.id,
                dayOfWeek = DayOfWeekName.TUESDAY,
                time = "18:00",
                type = DogTaskType.FEED
            )
        )

        return LunaCoinData(
            children = children,
            tasks = tasks,
            shopItems = shopItems,
            dogSchedule = dogSchedule,
            logs = emptyList()
        )
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