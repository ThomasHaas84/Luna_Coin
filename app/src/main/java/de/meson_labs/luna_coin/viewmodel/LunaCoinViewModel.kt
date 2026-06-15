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
import de.meson_labs.luna_coin.storage.LunaCoinStorage
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
    private val repository: DataRepository,
    private val legacyStorage: LunaCoinStorage? = null
) : ViewModel() {

    private val _data = MutableStateFlow<LunaCoinData>(LunaCoinData())
    val data: StateFlow<LunaCoinData> = _data.asStateFlow()

    private val _selectedChildId = MutableStateFlow<String?>(null)
    val selectedChildId: StateFlow<String?> = _selectedChildId.asStateFlow()

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _isLoading.value = true
            println("🔄 Starte schnellen Ladevorgang...")

            val legacyData = legacyStorage?.loadData()
            if (legacyData != null && legacyData.children.isNotEmpty()) {
                _data.value = legacyData
                println("📱 Lokale Daten sofort geladen (${legacyData.children.size} Kinder)")
            } else {
                _data.value = DemoData.create()
                println("🌱 Demo-Daten geladen")
            }

            launch {
                try {
                    val firestoreData = repository.loadData()
                    if (firestoreData != null && firestoreData.children.isNotEmpty()) {
                        println("🔄 Firestore Daten geladen → UI aktualisieren")
                        _data.value = firestoreData
                    }
                } catch (e: Exception) {
                    println("⚠️ Firestore Sync fehlgeschlagen: ${e.message}")
                } finally {
                    _isLoading.value = false
                }
            }
        }
    }

    private fun saveData() {
        viewModelScope.launch {
            val totalCoins = _data.value.children.sumOf { it.coins }
            println("💾 Speichere Daten nach Firestore... (Gesamt-Coins: $totalCoins)")
            repository.saveData(_data.value)
            legacyStorage?.saveData(_data.value)
        }
    }

    private fun updateData(newData: LunaCoinData) {
        _data.value = newData
        saveData()
        println("📝 Daten im ViewModel aktualisiert (Coins: ${newData.children.sumOf { it.coins }})")
    }

    // ====================== UI INTERAKTION ======================
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

    companion object {
        private const val MAX_ACTIVE_LOGS = 2000
    }

    private fun addLogToList(log: LogEntry, currentLogs: List<LogEntry>): List<LogEntry> {
        return (listOf(log) + currentLogs).take(MAX_ACTIVE_LOGS)
    }

    // ====================== WICHTIGE FUNKTIONEN ======================
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

        val updatedTasks = currentData.tasks.map { currentTask ->
            if (currentTask.id == taskId) {
                currentTask.copy(completions = currentTask.completions + newCompletion)
            } else currentTask
        }

        val updatedChildren = currentData.children.map { child ->
            if (child.id == childId) child.copy(coins = child.coins + task.rewardCoins) else child
        }

        val child = updatedChildren.firstOrNull { it.id == childId }

        val log = LogEntry(
            id = uuid(),
            timestamp = timestamp,
            childId = childId,
            type = LogType.TASK_DONE,
            text = "${child?.name ?: "Kind"} hat Aufgabe erledigt: ${task.title}",
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

        val updatedChildren = currentData.children.map { currentChild ->
            if (currentChild.id == childId) currentChild.copy(coins = currentChild.coins - item.priceCoins) else currentChild
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

    fun applyLuckyWheelResult(
        childId: String,
        costCoins: Int,
        result: LuckyWheelResult
    ): LuckyWheelResult {
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

        val updatedChildren = currentData.children.map { currentChild ->
            if (currentChild.id == childId) {
                currentChild.copy(coins = currentChild.coins + coinChange, inventory = updatedInventory)
            } else currentChild
        }

        val log = LogEntry(
            id = uuid(),
            timestamp = nowText(),
            childId = childId,
            type = LogType.SYSTEM,
            text = if (costCoins == 0) "Glücksrad kostenlos gedreht: $finalMessage"
            else "Glücksrad für 1 Luna Coin gedreht: $finalMessage",
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
        val coinDifference = newCoins - oldCoins
        val cleanComment = comment?.trim().orEmpty()

        val updatedChildren = currentData.children.map {
            if (it.id == childId) it.copy(coins = newCoins) else it
        }

        val logText = buildString {
            append("Coins von ${child.name} wurden manuell von $oldCoins auf $newCoins geändert")
            if (cleanComment.isNotBlank()) append(" · Kommentar: $cleanComment")
        }

        val log = LogEntry(
            id = uuid(),
            timestamp = nowText(),
            childId = childId,
            type = LogType.SYSTEM,
            text = logText,
            coinChange = coinDifference
        )

        updateData(currentData.copy(children = updatedChildren, logs = addLogToList(log, currentData.logs)))
    }

    fun updateChild(updatedChild: Child) {
        val currentData = _data.value
        val updatedChildren = currentData.children.map { child ->
            if (child.id == updatedChild.id) updatedChild else child
        }
        updateData(currentData.copy(children = updatedChildren))
    }

    // ====================== CRUD ======================
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
        weeklyDay: DayOfWeekName?,
        isWatchlist: Boolean
    ) {
        val currentData = _data.value

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
            isWatchlist = isWatchlist
        )

        updateData(currentData.copy(tasks = currentData.tasks + newTask))
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
        weeklyDay: DayOfWeekName?,
        isWatchlist: Boolean
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
                    assignedChildId = if (assignmentType == TaskAssignmentType.ASSIGNED) assignedChildId else null,
                    repeatType = repeatType,
                    startDate = startDate,
                    dueDate = dueDate?.takeIf { it.isNotBlank() },
                    weeklyDay = weeklyDay,
                    isWatchlist = isWatchlist
                )
            } else task
        }

        updateData(currentData.copy(tasks = updatedTasks))
    }

    fun deleteTask(taskId: String) {
        val currentData = _data.value
        updateData(currentData.copy(tasks = currentData.tasks.filterNot { it.id == taskId }))
    }

    fun addShopItem(title: String, description: String, priceCoins: Int) {
        val currentData = _data.value
        val newItem = ShopItem(id = uuid(), title = title, description = description, priceCoins = priceCoins)
        updateData(currentData.copy(shopItems = currentData.shopItems + newItem))
    }

    fun updateShopItem(itemId: String, title: String, description: String, priceCoins: Int) {
        val currentData = _data.value
        val updatedItems = currentData.shopItems.map { item ->
            if (item.id == itemId) item.copy(title = title, description = description, priceCoins = priceCoins) else item
        }
        updateData(currentData.copy(shopItems = updatedItems))
    }

    fun deleteShopItem(itemId: String) {
        val currentData = _data.value
        updateData(currentData.copy(shopItems = currentData.shopItems.filterNot { it.id == itemId }))
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
        updateData(currentData.copy(dogSchedule = currentData.dogSchedule + newEntry))
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
            } else entry
        }
        updateData(currentData.copy(dogSchedule = updatedSchedule))
    }

    fun deleteDogSchedule(scheduleId: String) {
        val currentData = _data.value
        updateData(currentData.copy(dogSchedule = currentData.dogSchedule.filterNot { it.id == scheduleId }))
    }

    fun undoLogEntry(logId: String) {
        val currentData = _data.value
        val log = currentData.logs.firstOrNull { it.id == logId } ?: return

        val updatedChildren = currentData.children.map { child ->
            if (child.id == log.childId) child.copy(coins = child.coins - log.coinChange) else child
        }

        val updatedLogs = currentData.logs.filterNot { it.id == logId }

        updateData(currentData.copy(children = updatedChildren, logs = updatedLogs))
    }

    fun resetDemoData() {
        updateData(DemoData.create())
        _selectedChildId.value = null
        _selectedDate.value = LocalDate.now()
    }

    // ====================== CLOUD BACKUP ======================
    fun createCloudBackup() {
        viewModelScope.launch {
            try {
                repository.saveData(_data.value)
                println("✅ Cloud-Backup erfolgreich erstellt")
            } catch (e: Exception) {
                println("❌ Backup fehlgeschlagen: ${e.message}")
            }
        }
    }

    fun restoreFromBackup() {
        viewModelScope.launch {
            try {
                val backupData = repository.loadData()
                if (backupData != null && backupData.children.isNotEmpty()) {
                    _data.value = backupData
                    println("✅ Backup erfolgreich wiederhergestellt")
                } else {
                    println("⚠️ Kein Backup gefunden")
                }
            } catch (e: Exception) {
                println("❌ Restore fehlgeschlagen: ${e.message}")
            }
        }
    }

    fun importFromJson() {
        println("📂 JSON-Import gestartet (noch nicht implementiert)")
    }

    // ====================== LEGACY ======================
    fun saveBackup(): Boolean = legacyStorage?.saveBackup(_data.value) ?: false

    fun loadBackup(): Boolean {
        val backupData = legacyStorage?.loadBackup() ?: return false
        updateData(backupData)
        _selectedChildId.value = null
        _selectedDate.value = LocalDate.now()
        return true
    }

    fun getJsonText(): String = legacyStorage?.getJsonText() ?: ""

    // ====================== PRIVATE HELFER ======================
    private fun isTaskVisibleForChildAndDate(task: TaskItem, childId: String, date: LocalDate): Boolean {
        if (task.assignmentType == TaskAssignmentType.ASSIGNED && task.assignedChildId != childId) return false
        if (!isTaskDueOnDate(task, date)) return false

        if (task.completionMode == TaskCompletionMode.ONCE_TOTAL) {
            if (task.completions.any { it.date == date.toString() }) return false
        }
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
        val startDate = task.startDate.toLocalDateOrNull() ?: return true
        if (date.isBefore(startDate)) return false

        return when (task.repeatType) {
            TaskRepeatType.DAILY -> true
            TaskRepeatType.WEEKDAYS -> date.dayOfWeek.value in 1..5
            TaskRepeatType.WEEKEND -> date.dayOfWeek.value in 6..7
            TaskRepeatType.WEEKLY -> task.weeklyDay == date.toDayOfWeekName()
            TaskRepeatType.BIWEEKLY -> task.weeklyDay == date.toDayOfWeekName() && ChronoUnit.WEEKS.between(startDate, date) % 2L == 0L
            TaskRepeatType.MONTHLY -> date.dayOfMonth == startDate.dayOfMonth
            TaskRepeatType.YEARLY -> date.month == startDate.month && date.dayOfMonth == startDate.dayOfMonth
            TaskRepeatType.EVERY_TWO_YEARS -> date.month == startDate.month && date.dayOfMonth == startDate.dayOfMonth && ChronoUnit.YEARS.between(startDate, date) % 2L == 0L
        }
    }

    private fun nowText(): String = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"))
    private fun uuid(): String = UUID.randomUUID().toString()

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
        return try { LocalDate.parse(this) } catch (_: Exception) { null }
    }
}

// ====================== FACTORY ======================
class LunaCoinViewModelFactory(
    private val repository: DataRepository,
    private val legacyStorage: LunaCoinStorage? = null
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return LunaCoinViewModel(repository, legacyStorage) as T
    }
}