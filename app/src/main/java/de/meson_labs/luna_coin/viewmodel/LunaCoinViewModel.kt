package de.meson_labs.luna_coin.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import de.meson_labs.luna_coin.data.DemoData
import de.meson_labs.luna_coin.data.repository.DataRepository
import de.meson_labs.luna_coin.models.Child
import de.meson_labs.luna_coin.models.DayOfWeekName
import de.meson_labs.luna_coin.models.DogScheduleItem
import de.meson_labs.luna_coin.models.LogEntry
import de.meson_labs.luna_coin.models.LogType
import de.meson_labs.luna_coin.models.LuckyWheelUsage
import de.meson_labs.luna_coin.models.LunaCoinData
import de.meson_labs.luna_coin.models.LunaItemCatalog
import de.meson_labs.luna_coin.models.ShopItem
import de.meson_labs.luna_coin.models.TaskAssignmentType
import de.meson_labs.luna_coin.models.TaskCompletion
import de.meson_labs.luna_coin.models.TaskCompletionMode
import de.meson_labs.luna_coin.models.TaskItem
import de.meson_labs.luna_coin.models.TaskRepeatType
import de.meson_labs.luna_coin.screens.LuckyWheelResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.UUID

class LunaCoinViewModel(
    private val repository: DataRepository
) : ViewModel() {

    private val _data = MutableStateFlow<LunaCoinData>(LunaCoinData())
    val data: StateFlow<LunaCoinData> = _data.asStateFlow()

    private val _selectedChildId = MutableStateFlow<String?>(null)
    val selectedChildId: StateFlow<String?> = _selectedChildId.asStateFlow()

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _isLoading.value = true
            println("🔄 Lade Daten aus Firestore...")

            val firestoreData = repository.loadData()
            if (firestoreData != null && firestoreData.children.isNotEmpty()) {
                _data.value = firestoreData
                println("✅ Firestore Daten geladen (${firestoreData.children.size} Kinder)")
            } else {
                println("🌱 Keine Daten in Firestore gefunden → Demo-Daten laden")
                _data.value = DemoData.create()
                saveData()
            }

            _isLoading.value = false
        }
    }

    private fun saveData() {
        viewModelScope.launch {
            val totalCoins = _data.value.children.sumOf { it.coins }
            println("💾 Speichere Daten nach Firestore... (Gesamt-Coins: $totalCoins)")
            repository.saveData(_data.value)
        }
    }

    private fun updateData(newData: LunaCoinData) {
        _data.value = newData
        saveData()
    }

    fun showMessage(text: String) {
        _message.value = text
        viewModelScope.launch {
            kotlinx.coroutines.delay(2500)
            if (_message.value == text) _message.value = null
        }
    }

    // ====================== UI INTERAKTION ======================
    fun selectChild(childId: String) { _selectedChildId.value = childId }
    fun logout() { _selectedChildId.value = null }
    fun previousDay() { _selectedDate.value = _selectedDate.value.minusDays(1) }
    fun nextDay() { _selectedDate.value = _selectedDate.value.plusDays(1) }
    fun today() { _selectedDate.value = LocalDate.now() }

    companion object {
        private const val MAX_ACTIVE_LOGS = 2000
    }

    private fun addLogToList(log: LogEntry, currentLogs: List<LogEntry>): List<LogEntry> {
        return (listOf(log) + currentLogs).take(MAX_ACTIVE_LOGS)
    }

    // ====================== TASKS ======================
    fun completeTask(taskId: String) {
        val childId = _selectedChildId.value ?: return
        val currentData = _data.value
        val selectedDate = _selectedDate.value
        val selectedDateText = selectedDate.toString()

        val task = currentData.tasks.firstOrNull { it.id == taskId } ?: return

        if (!isTaskVisibleForChildAndDate(task, childId, selectedDate)) return
        if (!canTaskBeCompleted(task, childId, selectedDate)) return

        val timestamp = nowText()
        val newCompletion = TaskCompletion(childId = childId, date = selectedDateText, timestamp = timestamp)

        val updatedTasks = currentData.tasks.map { t ->
            if (t.id == taskId) t.copy(completions = t.completions + newCompletion) else t
        }

        val updatedChildren = currentData.children.map { c ->
            if (c.id == childId) c.copy(coins = c.coins + task.rewardCoins) else c
        }

        val childName = updatedChildren.firstOrNull { it.id == childId }?.name ?: "Kind"

        val log = LogEntry(
            id = uuid(),
            timestamp = timestamp,
            childId = childId,
            type = LogType.TASK_DONE,
            text = "$childName hat Aufgabe erledigt: ${task.title}",
            coinChange = task.rewardCoins
        )

        updateData(currentData.copy(
            children = updatedChildren,
            tasks = updatedTasks,
            logs = addLogToList(log, currentData.logs)
        ))
    }

    fun buyShopItem(shopItemId: String) {
        val childId = _selectedChildId.value ?: return
        val currentData = _data.value

        val child = currentData.children.firstOrNull { it.id == childId } ?: return
        val item = currentData.shopItems.firstOrNull { it.id == shopItemId } ?: return

        if (child.coins < item.priceCoins) return

        val timestamp = nowText()

        val updatedChildren = currentData.children.map { c ->
            if (c.id == childId) c.copy(coins = c.coins - item.priceCoins) else c
        }

        val log = LogEntry(
            id = uuid(),
            timestamp = timestamp,
            childId = childId,
            type = LogType.SHOP_BUY,
            text = "${child.name} hat gekauft: ${item.title}",
            coinChange = -item.priceCoins
        )

        updateData(currentData.copy(
            children = updatedChildren,
            logs = addLogToList(log, currentData.logs)
        ))
    }

    fun applyLuckyWheelResult(childId: String, costCoins: Int, result: LuckyWheelResult): LuckyWheelResult {
        val currentData = _data.value
        val todayText = LocalDate.now().toString()

        val child = currentData.children.firstOrNull { it.id == childId } ?: return result

        val currentUsage = currentData.luckyWheelUsage.firstOrNull { it.childId == childId && it.date == todayText }
        val baseUsage = currentUsage ?: LuckyWheelUsage(childId = childId, date = todayText)

        val coinChange = result.rewardCoins - costCoins
        var finalMessage = result.message
        var updatedInventory = child.inventory
        var skinWonToday = baseUsage.skinWon

        if (result.isSkinReward) {
            if (baseUsage.skinWon) {
                finalMessage = "Heute hast du bereits einen Skin gewonnen."
            } else {
                val nextSkin = LunaItemCatalog.allItems.firstOrNull { it.item !in child.inventory }
                if (nextSkin != null) {
                    updatedInventory = child.inventory + nextSkin.item
                    skinWonToday = true
                    finalMessage = "Du hast den Skin „${nextSkin.title}“ gewonnen!"
                } else {
                    finalMessage = "Du hast bereits alle Skins gesammelt."
                }
            }
        }

        val updatedUsage = baseUsage.copy(freeSpinUsed = true, skinWon = skinWonToday)

        val updatedUsageList = if (currentUsage == null) {
            currentData.luckyWheelUsage + updatedUsage
        } else {
            currentData.luckyWheelUsage.map { if (it.childId == childId && it.date == todayText) updatedUsage else it }
        }

        val updatedChildren = currentData.children.map { c ->
            if (c.id == childId) c.copy(coins = c.coins + coinChange, inventory = updatedInventory) else c
        }

        val log = LogEntry(
            id = uuid(),
            timestamp = nowText(),
            childId = childId,
            type = LogType.SYSTEM,
            text = if (costCoins == 0) "Glücksrad kostenlos gedreht: $finalMessage" else "Glücksrad für 1 Luna Coin gedreht: $finalMessage",
            coinChange = coinChange
        )

        updateData(currentData.copy(
            children = updatedChildren,
            luckyWheelUsage = updatedUsageList,
            logs = addLogToList(log, currentData.logs)
        ))

        return result.copy(message = finalMessage)
    }

    fun updateChildCoins(childId: String, newCoins: Int, comment: String?) {
        val currentData = _data.value
        val child = currentData.children.firstOrNull { it.id == childId } ?: return

        val oldCoins = child.coins
        val difference = newCoins - oldCoins

        val updatedChildren = currentData.children.map { c ->
            if (c.id == childId) c.copy(coins = newCoins) else c
        }

        val log = LogEntry(
            id = uuid(),
            timestamp = nowText(),
            childId = childId,
            type = LogType.SYSTEM,
            text = "Coins von ${child.name} manuell angepasst: $oldCoins → $newCoins",
            coinChange = difference
        )

        updateData(currentData.copy(children = updatedChildren, logs = addLogToList(log, currentData.logs)))
    }

    fun updateChild(updatedChild: Child) {
        val currentData = _data.value
        val updatedChildren = currentData.children.map { if (it.id == updatedChild.id) updatedChild else it }
        updateData(currentData.copy(children = updatedChildren))
    }

    // ====================== CRUD ======================
    fun addTask(
        title: String, description: String, rewardCoins: Int,
        assignmentType: TaskAssignmentType, completionMode: TaskCompletionMode,
        repeatType: TaskRepeatType, assignedChildId: String?,
        startDate: String, dueDate: String?, weeklyDay: DayOfWeekName?,
        isWatchlist: Boolean
    ) {
        val current = _data.value
        val newTask = TaskItem(
            id = uuid(), title = title, description = description, rewardCoins = rewardCoins,
            assignmentType = assignmentType, completionMode = completionMode,
            assignedChildId = if (assignmentType == TaskAssignmentType.ASSIGNED) assignedChildId else null,
            repeatType = repeatType, startDate = startDate,
            dueDate = dueDate?.takeIf { it.isNotBlank() }, weeklyDay = weeklyDay,
            completions = emptyList(), isWatchlist = isWatchlist
        )
        updateData(current.copy(tasks = current.tasks + newTask))
    }

    fun updateTask(
        taskId: String, title: String, description: String, rewardCoins: Int,
        assignmentType: TaskAssignmentType, completionMode: TaskCompletionMode,
        repeatType: TaskRepeatType, assignedChildId: String?,
        startDate: String, dueDate: String?, weeklyDay: DayOfWeekName?,
        isWatchlist: Boolean
    ) {
        val current = _data.value
        val updated = current.tasks.map { task ->
            if (task.id == taskId) {
                task.copy(
                    title = title, description = description, rewardCoins = rewardCoins,
                    assignmentType = assignmentType, completionMode = completionMode,
                    assignedChildId = if (assignmentType == TaskAssignmentType.ASSIGNED) assignedChildId else null,
                    repeatType = repeatType, startDate = startDate,
                    dueDate = dueDate?.takeIf { it.isNotBlank() }, weeklyDay = weeklyDay,
                    isWatchlist = isWatchlist
                )
            } else task
        }
        updateData(current.copy(tasks = updated))
    }

    fun deleteTask(taskId: String) {
        val current = _data.value
        updateData(current.copy(tasks = current.tasks.filterNot { it.id == taskId }))
    }

    fun addShopItem(title: String, description: String, priceCoins: Int) {
        val current = _data.value
        val newItem = ShopItem(id = uuid(), title = title, description = description, priceCoins = priceCoins)
        updateData(current.copy(shopItems = current.shopItems + newItem))
    }

    fun updateShopItem(itemId: String, title: String, description: String, priceCoins: Int) {
        val current = _data.value
        val updated = current.shopItems.map { item ->
            if (item.id == itemId) item.copy(title = title, description = description, priceCoins = priceCoins) else item
        }
        updateData(current.copy(shopItems = updated))
    }

    fun deleteShopItem(itemId: String) {
        val current = _data.value
        updateData(current.copy(shopItems = current.shopItems.filterNot { it.id == itemId }))
    }

    fun addDogSchedule(
        childId: String, dayOfWeek: DayOfWeekName,
        careStartTime: String, careEndTime: String,
        feedingTime: String, walkTime: String
    ) {
        val current = _data.value
        val newItem = DogScheduleItem(
            id = uuid(),
            childId = childId,
            dayOfWeek = dayOfWeek,
            careStartTime = careStartTime,
            careEndTime = careEndTime,
            feedingTime = feedingTime,
            walkTime = walkTime
        )
        updateData(current.copy(dogSchedule = current.dogSchedule + newItem))
    }

    fun updateDogSchedule(
        scheduleId: String, childId: String, dayOfWeek: DayOfWeekName,
        careStartTime: String, careEndTime: String,
        feedingTime: String, walkTime: String
    ) {
        val current = _data.value
        val updated = current.dogSchedule.map { item ->
            if (item.id == scheduleId) {
                item.copy(
                    childId = childId,
                    dayOfWeek = dayOfWeek,
                    careStartTime = careStartTime,
                    careEndTime = careEndTime,
                    feedingTime = feedingTime,
                    walkTime = walkTime
                )
            } else item
        }
        updateData(current.copy(dogSchedule = updated))
    }

    fun deleteDogSchedule(scheduleId: String) {
        val current = _data.value
        updateData(current.copy(dogSchedule = current.dogSchedule.filterNot { it.id == scheduleId }))
    }

    fun undoLogEntry(logId: String) {
        val current = _data.value
        val log = current.logs.firstOrNull { it.id == logId } ?: return
        val updatedChildren = current.children.map { c ->
            if (c.id == log.childId) c.copy(coins = c.coins - log.coinChange) else c
        }
        updateData(current.copy(children = updatedChildren, logs = current.logs.filterNot { it.id == logId }))
    }

    fun resetDemoData() {
        updateData(DemoData.create())
        _selectedChildId.value = null
        _selectedDate.value = LocalDate.now()
        showMessage("Demo-Daten wurden zurückgesetzt")
    }

    // ====================== CLOUD BACKUP ======================
    fun createCloudBackup() {
        viewModelScope.launch {
            try {
                repository.saveData(_data.value)
                showMessage("✅ Cloud-Backup erfolgreich erstellt")
            } catch (e: Exception) {
                showMessage("❌ Backup fehlgeschlagen")
            }
        }
    }

    fun restoreFromBackup() {
        viewModelScope.launch {
            try {
                val backup = repository.loadData()
                if (backup != null && backup.children.isNotEmpty()) {
                    _data.value = backup
                    showMessage("✅ Backup erfolgreich wiederhergestellt")
                } else {
                    showMessage("⚠️ Kein Backup gefunden")
                }
            } catch (e: Exception) {
                showMessage("❌ Wiederherstellen fehlgeschlagen")
            }
        }
    }

    fun importFromJson() {
        showMessage("📂 JSON-Import wird vorbereitet...")
    }

    // ====================== PRIVATE HELFER ======================
    private fun isTaskVisibleForChildAndDate(task: TaskItem, childId: String, date: LocalDate): Boolean {
        if (task.assignmentType == TaskAssignmentType.ASSIGNED && task.assignedChildId != childId) return false
        if (!isTaskDueOnDate(task, date)) return false
        if (task.completionMode == TaskCompletionMode.ONCE_TOTAL && task.completions.any { it.date == date.toString() }) return false
        return true
    }

    private fun canTaskBeCompleted(task: TaskItem, childId: String, date: LocalDate): Boolean {
        if (task.repeatType == TaskRepeatType.DAILY && date != LocalDate.now()) return false
        if (task.assignmentType == TaskAssignmentType.ASSIGNED && task.assignedChildId != childId) return false

        return when (task.completionMode) {
            TaskCompletionMode.EACH_PERSON -> task.completions.none { it.childId == childId && it.date == date.toString() }
            TaskCompletionMode.ONCE_TOTAL -> task.completions.none { it.date == date.toString() }
        }
    }

    private fun isTaskDueOnDate(task: TaskItem, date: LocalDate): Boolean {
        val start = task.startDate.toLocalDateOrNull() ?: return true
        if (date.isBefore(start)) return false
        return true
    }

    private fun nowText(): String = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"))
    private fun uuid(): String = UUID.randomUUID().toString()

    private fun LocalDate.toDayOfWeekName(): DayOfWeekName {
        return when (dayOfWeek.value) {
            1 -> DayOfWeekName.MONDAY
            2 -> DayOfWeekName.TUESDAY
            3 -> DayOfWeekName.WEDNESDAY
            4 -> DayOfWeekName.THURSDAY
            5 -> DayOfWeekName.FRIDAY
            6 -> DayOfWeekName.SATURDAY
            else -> DayOfWeekName.SUNDAY
        }
    }

    private fun String.toLocalDateOrNull(): LocalDate? = try { LocalDate.parse(this) } catch (_: Exception) { null }
}

// ====================== FACTORY ======================
class LunaCoinViewModelFactory(
    private val repository: DataRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return LunaCoinViewModel(repository) as T
    }
}