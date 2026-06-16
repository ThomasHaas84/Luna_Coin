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

    private var realtimeSyncStarted = false

    private val minSelectableDate = LocalDate.now().minusDays(14)
    private val maxSelectableDate = LocalDate.now().plusDays(14)

    val canGoToPreviousDay: Boolean
        get() = _selectedDate.value.isAfter(minSelectableDate)

    val canGoToNextDay: Boolean
        get() = _selectedDate.value.isBefore(maxSelectableDate)

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _isLoading.value = true
            println("🔄 Lade Firestore Collections...")

            try {
                val children = repository.loadChildren()

                if (children.isEmpty()) {
                    println("⚠️ Keine Kinder in Firestore gefunden → Demo-Daten nur lokal anzeigen, NICHT speichern")
                    val demoData = DemoData.create()
                    _data.value = demoData
                    showMessage("⚠️ Keine Cloud-Daten gefunden")
                } else {
                    val loadedData = LunaCoinData(
                        children = children,
                        tasks = repository.loadTasks(),
                        shopItems = repository.loadShopItems(),
                        dogSchedule = repository.loadDogSchedule(),
                        logs = repository.loadLogs(),
                        luckyWheelUsage = repository.loadLuckyWheelUsage(),
                        gameHighscores = repository.loadGameHighscores()
                    )

                    _data.value = loadedData

                    println(
                        "✅ Firestore Collections geladen: " +
                                "${loadedData.children.size} Kinder, " +
                                "${loadedData.tasks.size} Aufgaben, " +
                                "${loadedData.shopItems.size} Shop-Items, " +
                                "${loadedData.logs.size} Logs"
                    )
                }

                startRealtimeSync()
            } catch (e: Exception) {
                println("❌ Fehler beim Laden der Firestore Collections: ${e.message}")
                e.printStackTrace()
                showMessage("❌ Daten konnten nicht geladen werden")
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun startRealtimeSync() {
        if (realtimeSyncStarted) return

        realtimeSyncStarted = true

        repository.startRealtimeSync(
            onDataChanged = { realtimeData ->
                viewModelScope.launch {
                    if (realtimeData.children.isEmpty()) {
                        println("⚠️ Realtime-Update ohne Kinder ignoriert, um lokale Anzeige nicht mit leerem Cloud-Zustand zu überschreiben")
                        return@launch
                    }

                    val currentSelectedChildId = _selectedChildId.value

                    _data.value = realtimeData

                    if (
                        currentSelectedChildId != null &&
                        realtimeData.children.none { it.id == currentSelectedChildId }
                    ) {
                        _selectedChildId.value = null
                    }
                }
            },
            onError = { error ->
                println("❌ Firestore Live-Sync Fehler: ${error.message}")
                error.printStackTrace()

                viewModelScope.launch {
                    showMessage("❌ Live-Synchronisation fehlgeschlagen")
                }
            }
        )
    }

    override fun onCleared() {
        repository.stopRealtimeSync()
        super.onCleared()
    }

    fun showMessage(text: String) {
        _message.value = text
        viewModelScope.launch {
            kotlinx.coroutines.delay(2500)
            if (_message.value == text) {
                _message.value = null
            }
        }
    }

    fun selectChild(childId: String) {
        _selectedChildId.value = childId
    }

    fun logout() {
        _selectedChildId.value = null
    }

    fun previousDay() {
        if (canGoToPreviousDay) {
            _selectedDate.value = _selectedDate.value.minusDays(1)
        }
    }

    fun nextDay() {
        if (canGoToNextDay) {
            _selectedDate.value = _selectedDate.value.plusDays(1)
        }
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

    fun completeTask(taskId: String) {
        val childId = _selectedChildId.value ?: return
        val currentData = _data.value
        val selectedDate = _selectedDate.value
        val selectedDateText = selectedDate.toString()

        val task = currentData.tasks.firstOrNull { it.id == taskId } ?: return
        val child = currentData.children.firstOrNull { it.id == childId } ?: return

        if (!isTaskVisibleForChildAndDate(task, childId, selectedDate)) return
        if (!canTaskBeCompleted(task, childId, selectedDate)) return

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

        val optimisticChild = child.copy(
            coins = child.coins + task.rewardCoins
        )

        _data.value = currentData.copy(
            children = currentData.children.map { c ->
                if (c.id == childId) optimisticChild else c
            },
            tasks = currentData.tasks.map { t ->
                if (t.id == taskId) updatedTask else t
            },
            logs = addLogToList(log, currentData.logs)
        )

        viewModelScope.launch {
            try {
                val realCoinValue = repository.changeChildCoins(
                    childId = childId,
                    coinDelta = task.rewardCoins
                )

                val latestData = _data.value
                _data.value = latestData.copy(
                    children = latestData.children.map { c ->
                        if (c.id == childId) c.copy(coins = realCoinValue) else c
                    }
                )

                repository.saveTask(updatedTask)
                repository.saveLog(log)
            } catch (e: Exception) {
                println("❌ Fehler beim Abschließen der Aufgabe: ${e.message}")
                e.printStackTrace()
                showMessage("❌ Aufgabe konnte nicht gespeichert werden")
            }
        }
    }

    fun buyShopItem(shopItemId: String) {
        val childId = _selectedChildId.value ?: return
        val currentData = _data.value

        val child = currentData.children.firstOrNull { it.id == childId } ?: return
        val item = currentData.shopItems.firstOrNull { it.id == shopItemId } ?: return

        if (child.coins < item.priceCoins) {
            showMessage("Nicht genug Luna Coins")
            return
        }

        val coinDelta = -item.priceCoins
        val timestamp = nowText()

        val log = LogEntry(
            id = uuid(),
            timestamp = timestamp,
            childId = childId,
            type = LogType.SHOP_BUY,
            text = "${child.name} hat gekauft: ${item.title}",
            coinChange = coinDelta
        )

        val optimisticChild = child.copy(
            coins = child.coins + coinDelta
        )

        _data.value = currentData.copy(
            children = currentData.children.map { c ->
                if (c.id == childId) optimisticChild else c
            },
            logs = addLogToList(log, currentData.logs)
        )

        viewModelScope.launch {
            try {
                val realCoinValue = repository.changeChildCoins(
                    childId = childId,
                    coinDelta = coinDelta
                )

                val latestData = _data.value
                _data.value = latestData.copy(
                    children = latestData.children.map { c ->
                        if (c.id == childId) c.copy(coins = realCoinValue) else c
                    }
                )

                repository.saveLog(log)
            } catch (e: Exception) {
                println("❌ Fehler beim Kaufen des Shop-Items: ${e.message}")
                e.printStackTrace()
                showMessage("❌ Kauf konnte nicht gespeichert werden")
            }
        }
    }

    fun applyLuckyWheelResult(childId: String, costCoins: Int, result: LuckyWheelResult): LuckyWheelResult {
        val currentData = _data.value
        val todayText = LocalDate.now().toString()

        val child = currentData.children.firstOrNull { it.id == childId } ?: return result

        val currentUsage = currentData.luckyWheelUsage.firstOrNull {
            it.childId == childId && it.date == todayText
        }

        val baseUsage = currentUsage ?: LuckyWheelUsage(
            id = "${childId}_${todayText}",
            childId = childId,
            date = todayText
        )

        val coinDelta = result.rewardCoins - costCoins

        var finalMessage = result.message
        var updatedInventory = child.inventory
        var skinWonToday = baseUsage.skinWon

        if (result.isSkinReward) {
            if (baseUsage.skinWon) {
                finalMessage = "Heute hast du bereits einen Skin gewonnen."
            } else {
                val nextSkin = LunaItemCatalog.allItems.firstOrNull {
                    it.item !in child.inventory
                }

                if (nextSkin != null) {
                    updatedInventory = child.inventory + nextSkin.item
                    skinWonToday = true
                    finalMessage = "Du hast den Skin „${nextSkin.title}“ gewonnen!"
                } else {
                    finalMessage = "Du hast bereits alle Skins gesammelt."
                }
            }
        }

        val updatedUsage = baseUsage.copy(
            freeSpinUsed = true,
            skinWon = skinWonToday
        )

        val updatedUsageList = if (currentUsage == null) {
            currentData.luckyWheelUsage + updatedUsage
        } else {
            currentData.luckyWheelUsage.map { usage ->
                if (usage.childId == childId && usage.date == todayText) updatedUsage else usage
            }
        }

        val optimisticChild = child.copy(
            coins = child.coins + coinDelta,
            inventory = updatedInventory
        )

        val log = LogEntry(
            id = uuid(),
            timestamp = nowText(),
            childId = childId,
            type = LogType.SYSTEM,
            text = if (costCoins == 0) {
                "Glücksrad kostenlos gedreht: $finalMessage"
            } else {
                "Glücksrad für $costCoins Luna Coin gedreht: $finalMessage"
            },
            coinChange = coinDelta
        )

        _data.value = currentData.copy(
            children = currentData.children.map { c ->
                if (c.id == childId) optimisticChild else c
            },
            luckyWheelUsage = updatedUsageList,
            logs = addLogToList(log, currentData.logs)
        )

        viewModelScope.launch {
            try {
                val realCoinValue = repository.changeChildCoins(
                    childId = childId,
                    coinDelta = coinDelta
                )

                val childWithRealCoins = optimisticChild.copy(
                    coins = realCoinValue
                )

                val latestData = _data.value
                _data.value = latestData.copy(
                    children = latestData.children.map { c ->
                        if (c.id == childId) childWithRealCoins else c
                    }
                )

                repository.updateChildInventory(
                    childId = childId,
                    inventory = childWithRealCoins.inventory,
                    equippedItem = childWithRealCoins.equippedItem,
                    profileImageItem = childWithRealCoins.profileImageItem,
                    hasProfileImage = childWithRealCoins.hasProfileImage
                )

                repository.saveLuckyWheelUsage(updatedUsage)
                repository.saveLog(log)
            } catch (e: Exception) {
                println("❌ Fehler beim Speichern des Glücksrad-Ergebnisses: ${e.message}")
                e.printStackTrace()
                showMessage("❌ Glücksrad-Ergebnis konnte nicht gespeichert werden")
            }
        }

        return result.copy(message = finalMessage)
    }

    fun updateChildCoins(childId: String, newCoins: Int, comment: String?) {
        val currentData = _data.value
        val child = currentData.children.firstOrNull { it.id == childId } ?: return

        val oldCoins = child.coins
        val coinDelta = newCoins - oldCoins
        val timestamp = nowText()

        val logText = if (comment.isNullOrBlank()) {
            "Coins von ${child.name} manuell angepasst: $oldCoins → $newCoins"
        } else {
            "Coins von ${child.name} manuell angepasst: $oldCoins → $newCoins ($comment)"
        }

        val log = LogEntry(
            id = uuid(),
            timestamp = timestamp,
            childId = childId,
            type = LogType.SYSTEM,
            text = logText,
            coinChange = coinDelta
        )

        val optimisticChild = child.copy(
            coins = newCoins
        )

        _data.value = currentData.copy(
            children = currentData.children.map { c ->
                if (c.id == childId) optimisticChild else c
            },
            logs = addLogToList(log, currentData.logs)
        )

        viewModelScope.launch {
            try {
                val realCoinValue = repository.setChildCoins(
                    childId = childId,
                    coins = newCoins
                )

                val latestData = _data.value
                _data.value = latestData.copy(
                    children = latestData.children.map { c ->
                        if (c.id == childId) c.copy(coins = realCoinValue) else c
                    }
                )

                repository.saveLog(log)

            } catch (e: Exception) {
                println("❌ Fehler beim manuellen Anpassen der Coins: ${e.message}")
                e.printStackTrace()
                showMessage("❌ Coins konnten nicht gespeichert werden")
            }
        }
    }

    fun updateChild(updatedChild: Child) {
        val currentData = _data.value
        val existingChild = currentData.children.firstOrNull { it.id == updatedChild.id } ?: return

        val safeUpdatedChild = updatedChild.copy(
            coins = existingChild.coins
        )

        _data.value = currentData.copy(
            children = currentData.children.map { child ->
                if (child.id == safeUpdatedChild.id) safeUpdatedChild else child
            }
        )

        viewModelScope.launch {
            try {
                repository.updateChildProfile(
                    childId = safeUpdatedChild.id,
                    name = safeUpdatedChild.name,
                    role = safeUpdatedChild.role,
                    password = safeUpdatedChild.password,
                    age = safeUpdatedChild.age
                )

                repository.updateChildInventory(
                    childId = safeUpdatedChild.id,
                    inventory = safeUpdatedChild.inventory,
                    equippedItem = safeUpdatedChild.equippedItem,
                    profileImageItem = safeUpdatedChild.profileImageItem,
                    hasProfileImage = safeUpdatedChild.hasProfileImage
                )
            } catch (e: Exception) {
                println("❌ Fehler beim Speichern des Kindes: ${e.message}")
                e.printStackTrace()
                showMessage("❌ Kind konnte nicht gespeichert werden")
            }
        }
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
        weeklyDay: DayOfWeekName?,
        isWatchlist: Boolean
    ) {
        val current = _data.value

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

        _data.value = current.copy(
            tasks = current.tasks + newTask
        )

        saveTaskToFirestore(newTask)
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
        val current = _data.value
        var updatedTask: TaskItem? = null

        val updated = current.tasks.map { task ->
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
                ).also {
                    updatedTask = it
                }
            } else {
                task
            }
        }

        _data.value = current.copy(tasks = updated)

        updatedTask?.let {
            saveTaskToFirestore(it)
        }
    }

    fun deleteTask(taskId: String) {
        val current = _data.value

        _data.value = current.copy(
            tasks = current.tasks.filterNot { it.id == taskId }
        )

        deleteTaskFromFirestore(taskId)
    }

    fun addShopItem(title: String, description: String, priceCoins: Int) {
        val current = _data.value

        val newItem = ShopItem(
            id = uuid(),
            title = title,
            description = description,
            priceCoins = priceCoins
        )

        _data.value = current.copy(
            shopItems = current.shopItems + newItem
        )

        saveShopItemToFirestore(newItem)
    }

    fun updateShopItem(itemId: String, title: String, description: String, priceCoins: Int) {
        val current = _data.value
        var updatedItem: ShopItem? = null

        val updated = current.shopItems.map { item ->
            if (item.id == itemId) {
                item.copy(
                    title = title,
                    description = description,
                    priceCoins = priceCoins
                ).also {
                    updatedItem = it
                }
            } else {
                item
            }
        }

        _data.value = current.copy(shopItems = updated)

        updatedItem?.let {
            saveShopItemToFirestore(it)
        }
    }

    fun deleteShopItem(itemId: String) {
        val current = _data.value

        _data.value = current.copy(
            shopItems = current.shopItems.filterNot { it.id == itemId }
        )

        deleteShopItemFromFirestore(itemId)
    }

    fun addDogSchedule(
        childId: String,
        dayOfWeek: DayOfWeekName,
        careStartTime: String,
        careEndTime: String,
        feedingTime: String,
        walkTime: String
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

        _data.value = current.copy(
            dogSchedule = current.dogSchedule + newItem
        )

        saveDogScheduleItemToFirestore(newItem)
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
        val current = _data.value
        var updatedItem: DogScheduleItem? = null

        val updated = current.dogSchedule.map { item ->
            if (item.id == scheduleId) {
                item.copy(
                    childId = childId,
                    dayOfWeek = dayOfWeek,
                    careStartTime = careStartTime,
                    careEndTime = careEndTime,
                    feedingTime = feedingTime,
                    walkTime = walkTime
                ).also {
                    updatedItem = it
                }
            } else {
                item
            }
        }

        _data.value = current.copy(dogSchedule = updated)

        updatedItem?.let {
            saveDogScheduleItemToFirestore(it)
        }
    }

    fun deleteDogSchedule(scheduleId: String) {
        val current = _data.value

        _data.value = current.copy(
            dogSchedule = current.dogSchedule.filterNot { it.id == scheduleId }
        )

        deleteDogScheduleItemFromFirestore(scheduleId)
    }

    fun undoLogEntry(logId: String) {
        val current = _data.value
        val log = current.logs.firstOrNull { it.id == logId } ?: return
        val child = current.children.firstOrNull { it.id == log.childId } ?: return

        val coinDelta = -log.coinChange

        val optimisticChild = child.copy(
            coins = child.coins + coinDelta
        )

        _data.value = current.copy(
            children = current.children.map { c ->
                if (c.id == log.childId) optimisticChild else c
            },
            logs = current.logs.filterNot { it.id == logId }
        )

        viewModelScope.launch {
            try {
                val realCoinValue = repository.changeChildCoins(
                    childId = log.childId,
                    coinDelta = coinDelta
                )

                val latestData = _data.value
                _data.value = latestData.copy(
                    children = latestData.children.map { c ->
                        if (c.id == log.childId) c.copy(coins = realCoinValue) else c
                    }
                )

                repository.deleteLog(logId)
            } catch (e: Exception) {
                println("❌ Fehler beim Rückgängig machen des Logs: ${e.message}")
                e.printStackTrace()
                showMessage("❌ Aktion konnte nicht rückgängig gemacht werden")
            }
        }
    }

    fun resetDemoData() {
        val demoData = DemoData.create()

        _data.value = demoData
        _selectedChildId.value = null
        _selectedDate.value = LocalDate.now()

        viewModelScope.launch {
            try {
                repository.saveData(demoData)
                showMessage("Demo-Daten wurden zurückgesetzt")
            } catch (e: Exception) {
                println("❌ Fehler beim Zurücksetzen der Demo-Daten: ${e.message}")
                e.printStackTrace()
                showMessage("❌ Demo-Daten konnten nicht zurückgesetzt werden")
            }
        }
    }

    fun createCloudBackup() {
        viewModelScope.launch {
            try {
                repository.saveData(_data.value)
                showMessage("✅ Cloud-Backup erfolgreich erstellt")
            } catch (e: Exception) {
                println("❌ Backup fehlgeschlagen: ${e.message}")
                e.printStackTrace()
                showMessage("❌ Backup fehlgeschlagen")
            }
        }
    }

    fun restoreFromBackup() {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                val backup = repository.loadData()

                if (backup != null && backup.children.isNotEmpty()) {
                    _data.value = backup
                    showMessage("✅ Backup erfolgreich wiederhergestellt")
                } else {
                    showMessage("⚠️ Kein Backup gefunden")
                }
            } catch (e: Exception) {
                println("❌ Wiederherstellen fehlgeschlagen: ${e.message}")
                e.printStackTrace()
                showMessage("❌ Wiederherstellen fehlgeschlagen")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun importFromJson() {
        showMessage("📂 JSON-Import wird vorbereitet...")
    }

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

    private fun saveTaskToFirestore(task: TaskItem) {
        viewModelScope.launch {
            try {
                repository.saveTask(task)
            } catch (e: Exception) {
                println("❌ Fehler beim Speichern der Aufgabe: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    private fun deleteTaskFromFirestore(taskId: String) {
        viewModelScope.launch {
            try {
                repository.deleteTask(taskId)
            } catch (e: Exception) {
                println("❌ Fehler beim Löschen der Aufgabe: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    private fun saveShopItemToFirestore(shopItem: ShopItem) {
        viewModelScope.launch {
            try {
                repository.saveShopItem(shopItem)
            } catch (e: Exception) {
                println("❌ Fehler beim Speichern des Shop-Items: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    private fun deleteShopItemFromFirestore(shopItemId: String) {
        viewModelScope.launch {
            try {
                repository.deleteShopItem(shopItemId)
            } catch (e: Exception) {
                println("❌ Fehler beim Löschen des Shop-Items: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    private fun saveDogScheduleItemToFirestore(item: DogScheduleItem) {
        viewModelScope.launch {
            try {
                repository.saveDogScheduleItem(item)
            } catch (e: Exception) {
                println("❌ Fehler beim Speichern des Hundeplans: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    private fun deleteDogScheduleItemFromFirestore(itemId: String) {
        viewModelScope.launch {
            try {
                repository.deleteDogScheduleItem(itemId)
            } catch (e: Exception) {
                println("❌ Fehler beim Löschen des Hundeplans: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    private fun nowText(): String {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"))
    }

    private fun uuid(): String {
        return UUID.randomUUID().toString()
    }

    private fun String.toLocalDateOrNull(): LocalDate? {
        return try {
            LocalDate.parse(this)
        } catch (_: Exception) {
            null
        }
    }
}

class LunaCoinViewModelFactory(
    private val repository: DataRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return LunaCoinViewModel(repository) as T
    }
}