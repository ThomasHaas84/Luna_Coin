package de.meson_labs.luna_coin.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import de.meson_labs.luna_coin.data.DemoData
import de.meson_labs.luna_coin.data.repository.DataRepository
import de.meson_labs.luna_coin.manager.GameHighscoreManager
import de.meson_labs.luna_coin.manager.DogScheduleManager
import de.meson_labs.luna_coin.manager.BackupManager
import de.meson_labs.luna_coin.manager.ShopManager
import de.meson_labs.luna_coin.manager.BuyShopItemPreparation
import de.meson_labs.luna_coin.manager.UserManager
import de.meson_labs.luna_coin.manager.CoinManager
import de.meson_labs.luna_coin.manager.BuyLunaMeItemPreparation
import de.meson_labs.luna_coin.manager.InventoryManager
import de.meson_labs.luna_coin.manager.LuckyWheelManager
import de.meson_labs.luna_coin.manager.TaskManager
import de.meson_labs.luna_coin.manager.LogManager
import de.meson_labs.luna_coin.manager.ensureBuiltInAdmin
import de.meson_labs.luna_coin.manager.sortChildrenInData
import de.meson_labs.luna_coin.models.Child
import de.meson_labs.luna_coin.models.DayOfWeekName
import de.meson_labs.luna_coin.models.LunaCoinData
import de.meson_labs.luna_coin.models.LunaGameLevel
import de.meson_labs.luna_coin.models.LunaGameScoreType
import de.meson_labs.luna_coin.models.LunaGameType
import de.meson_labs.luna_coin.models.TaskAssignmentType
import de.meson_labs.luna_coin.models.TaskCompletionMode
import de.meson_labs.luna_coin.models.TaskRepeatType
import de.meson_labs.luna_coin.models.UserRole
import de.meson_labs.luna_coin.screens.LuckyWheelResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

class LunaCoinViewModel(
    private val repository: DataRepository
) : ViewModel() {

    private val gameHighscoreManager = GameHighscoreManager(repository)
    private val dogScheduleManager = DogScheduleManager(repository)
    private val backupManager = BackupManager(repository)
    private val shopManager = ShopManager(repository)
    private val userManager = UserManager(repository)
    private val coinManager = CoinManager(repository)
    private val inventoryManager = InventoryManager(repository)
    private val luckyWheelManager = LuckyWheelManager(repository)
    private val taskManager = TaskManager(repository)
    private val logManager = LogManager(repository)

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
                    val demoData = ensureBuiltInAdmin(DemoData.create())
                    _data.value = sortChildrenInData(demoData)
                    showMessage("⚠️ Keine Cloud-Daten gefunden")
                } else {
                    val loadedData = ensureBuiltInAdmin(
                        LunaCoinData(
                            children = children,
                            tasks = repository.loadTasks(),
                            shopItems = repository.loadShopItems(),
                            dogSchedule = repository.loadDogSchedule(),
                            logs = repository.loadLogs(),
                            luckyWheelUsage = repository.loadLuckyWheelUsage(),
                            gameHighscores = repository.loadGameHighscores()
                        )
                    )

                    _data.value = sortChildrenInData(loadedData)

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
                        println("⚠️ Realtime-Update ohne Kinder ignoriert")
                        return@launch
                    }

                    val currentSelectedChildId = _selectedChildId.value
                    val safeData = sortChildrenInData(ensureBuiltInAdmin(realtimeData))

                    _data.value = safeData

                    if (
                        currentSelectedChildId != null &&
                        safeData.children.none { it.id == currentSelectedChildId }
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

    fun saveGameHighscore(
        game: LunaGameType,
        childId: String,
        scoreType: LunaGameScoreType,
        level: LunaGameLevel,
        value: Int
    ) {
        val operation = gameHighscoreManager.prepareSaveHighscore(
            currentData = _data.value,
            game = game,
            childId = childId,
            scoreType = scoreType,
            level = level,
            value = value
        ) ?: return

        _data.value = operation.optimisticData

        viewModelScope.launch {
            try {
                gameHighscoreManager.persistHighscore(operation)
            } catch (e: Exception) {
                _data.value = operation.originalData
                println("❌ Fehler beim Speichern des Highscores: ${e.message}")
                e.printStackTrace()
                showMessage("❌ Highscore konnte nicht gespeichert werden")
            }
        }
    }

    fun completeTask(taskId: String) {
        val childId = _selectedChildId.value ?: return

        val operation = taskManager.prepareCompleteTask(
            currentData = _data.value,
            taskId = taskId,
            childId = childId,
            selectedDate = _selectedDate.value
        ) ?: return

        _data.value = operation.optimisticData

        viewModelScope.launch {
            try {
                val realCoinValue = taskManager.persistCompleteTask(operation)

                _data.value = taskManager.applyRealCoinValue(
                    currentData = _data.value,
                    childId = operation.childId,
                    realCoinValue = realCoinValue
                )
            } catch (e: Exception) {
                _data.value = operation.originalData
                println("❌ Fehler beim Abschließen der Aufgabe: ${e.message}")
                e.printStackTrace()
                showMessage("❌ Aufgabe konnte nicht gespeichert werden")
            }
        }
    }

    fun buyShopItem(shopItemId: String) {
        val childId = _selectedChildId.value ?: return

        when (val preparation = shopManager.prepareBuyShopItem(
            currentData = _data.value,
            childId = childId,
            shopItemId = shopItemId
        )) {
            is BuyShopItemPreparation.Error -> {
                showMessage(preparation.message)
                return
            }

            is BuyShopItemPreparation.Success -> {
                val operation = preparation.operation

                _data.value = operation.optimisticData

                viewModelScope.launch {
                    try {
                        val realCoinValue = shopManager.persistBuyShopItem(operation)

                        _data.value = shopManager.applyRealCoinValue(
                            currentData = _data.value,
                            childId = operation.childId,
                            realCoinValue = realCoinValue
                        )
                    } catch (e: Exception) {
                        _data.value = operation.originalData
                        println("❌ Fehler beim Kaufen des Shop-Items: ${e.message}")
                        e.printStackTrace()
                        showMessage(e.message ?: "❌ Kauf konnte nicht gespeichert werden")
                    }
                }
            }
        }
    }

    fun buyLunaMeItem(itemName: String) {
        val childId = _selectedChildId.value ?: return
        val currentData = _data.value

        when (val preparation = inventoryManager.prepareBuyLunaMeItem(currentData, childId, itemName)) {
            is BuyLunaMeItemPreparation.Error -> {
                showMessage(preparation.message)
                return
            }

            is BuyLunaMeItemPreparation.Success -> {
                val operation = preparation.operation

                _data.value = operation.optimisticData

                viewModelScope.launch {
                    try {
                        val childWithRealCoins = inventoryManager.persistBuyLunaMeItem(operation)

                        _data.value = inventoryManager.applyPersistedInventoryChild(
                            currentData = _data.value,
                            childWithRealCoins = childWithRealCoins
                        )

                        showMessage(operation.successMessage)
                    } catch (e: Exception) {
                        _data.value = operation.originalData

                        println("❌ Fehler beim Kaufen des LunaME-Items: ${e.message}")
                        e.printStackTrace()

                        showMessage(e.message ?: "❌ LunaME-Item konnte nicht gekauft werden")
                    }
                }
            }
        }
    }

    fun applyLuckyWheelResult(childId: String, costCoins: Int, result: LuckyWheelResult): LuckyWheelResult {
        val operation = luckyWheelManager.prepareApplyLuckyWheelResult(
            currentData = _data.value,
            childId = childId,
            costCoins = costCoins,
            result = result
        ) ?: return result

        _data.value = operation.optimisticData

        viewModelScope.launch {
            try {
                val childWithRealCoins = luckyWheelManager.persistLuckyWheelResult(operation)

                _data.value = luckyWheelManager.applyPersistedLuckyWheelChild(
                    currentData = _data.value,
                    childWithRealCoins = childWithRealCoins
                )
            } catch (e: Exception) {
                _data.value = operation.originalData
                println("❌ Fehler beim Speichern des Glücksrad-Ergebnisses: ${e.message}")
                e.printStackTrace()
                showMessage("❌ Glücksrad-Ergebnis konnte nicht gespeichert werden")
            }
        }

        return operation.finalResult
    }

    fun updateChildCoins(childId: String, newCoins: Int, comment: String?) {
        val operation = coinManager.prepareSetChildCoins(
            currentData = _data.value,
            childId = childId,
            newCoins = newCoins,
            comment = comment
        ) ?: return

        _data.value = operation.optimisticData

        viewModelScope.launch {
            try {
                val realCoinValue = coinManager.persistSetChildCoins(operation)

                _data.value = coinManager.applyRealCoinValue(
                    currentData = _data.value,
                    childId = operation.childId,
                    realCoinValue = realCoinValue
                )
            } catch (e: Exception) {
                _data.value = operation.originalData
                println("❌ Fehler beim manuellen Anpassen der Coins: ${e.message}")
                e.printStackTrace()
                showMessage("❌ Coins konnten nicht gespeichert werden")
            }
        }
    }

    fun addChild(
        name: String,
        role: UserRole,
        password: String,
        age: Int,
        coins: Int,
        passwordRequired: Boolean,
        allowRememberLogin: Boolean
    ) {
        val result = userManager.prepareAddChild(
            currentData = _data.value,
            name = name,
            role = role,
            password = password,
            age = age,
            coins = coins,
            passwordRequired = passwordRequired,
            allowRememberLogin = allowRememberLogin
        )

        val operation = result.operation

        if (operation == null) {
            result.errorMessage?.let { showMessage(it) }
            return
        }

        _data.value = operation.optimisticData

        viewModelScope.launch {
            try {
                userManager.persistAddChild(operation)
                showMessage("✅ Benutzer angelegt")
            } catch (e: Exception) {
                _data.value = operation.originalData
                println("❌ Fehler beim Anlegen des Benutzers: ${e.message}")
                e.printStackTrace()
                showMessage("❌ Benutzer konnte nicht angelegt werden")
            }
        }
    }

    fun updateChild(updatedChild: Child) {
        val operation = userManager.prepareUpdateChild(
            currentData = _data.value,
            updatedChild = updatedChild
        ) ?: return

        operation.warningMessage?.let { showMessage(it) }

        _data.value = operation.optimisticData

        viewModelScope.launch {
            try {
                userManager.persistUpdateChild(operation)
                showMessage("✅ Benutzer gespeichert")
            } catch (e: Exception) {
                _data.value = operation.originalData
                println("❌ Fehler beim Speichern des Benutzers: ${e.message}")
                e.printStackTrace()
                showMessage("❌ Benutzer konnte nicht gespeichert werden")
            }
        }
    }

    fun deleteChild(childId: String) {
        val result = userManager.prepareDeleteChild(
            currentData = _data.value,
            childId = childId
        )

        val operation = result.operation

        if (operation == null) {
            result.errorMessage?.let { showMessage(it) }
            return
        }

        if (_selectedChildId.value == childId) {
            _selectedChildId.value = null
        }

        _data.value = operation.optimisticData

        viewModelScope.launch {
            try {
                userManager.persistDeleteChild(operation)
                showMessage("✅ Benutzer gelöscht")
            } catch (e: Exception) {
                _data.value = operation.originalData
                println("❌ Fehler beim Löschen des Benutzers: ${e.message}")
                e.printStackTrace()
                showMessage("❌ Benutzer konnte nicht gelöscht werden")
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
        watchlist: Boolean
    ) {
        val operation = taskManager.prepareAddTask(
            currentData = _data.value,
            title = title,
            description = description,
            rewardCoins = rewardCoins,
            assignmentType = assignmentType,
            completionMode = completionMode,
            repeatType = repeatType,
            assignedChildId = assignedChildId,
            startDate = startDate,
            dueDate = dueDate,
            weeklyDay = weeklyDay,
            watchlist = watchlist
        )

        _data.value = operation.optimisticData

        viewModelScope.launch {
            try {
                taskManager.persistAddTask(operation)
            } catch (e: Exception) {
                _data.value = operation.originalData
                println("❌ Fehler beim Speichern der Aufgabe: ${e.message}")
                e.printStackTrace()
            }
        }
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
        watchlist: Boolean
    ) {
        val operation = taskManager.prepareUpdateTask(
            currentData = _data.value,
            taskId = taskId,
            title = title,
            description = description,
            rewardCoins = rewardCoins,
            assignmentType = assignmentType,
            completionMode = completionMode,
            repeatType = repeatType,
            assignedChildId = assignedChildId,
            startDate = startDate,
            dueDate = dueDate,
            weeklyDay = weeklyDay,
            watchlist = watchlist
        ) ?: return

        _data.value = operation.optimisticData

        viewModelScope.launch {
            try {
                taskManager.persistUpdateTask(operation)
            } catch (e: Exception) {
                _data.value = operation.originalData
                println("❌ Fehler beim Speichern der Aufgabe: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    fun deleteTask(taskId: String) {
        val operation = taskManager.prepareDeleteTask(
            currentData = _data.value,
            taskId = taskId
        )

        _data.value = operation.optimisticData

        viewModelScope.launch {
            try {
                taskManager.persistDeleteTask(operation)
            } catch (e: Exception) {
                _data.value = operation.originalData
                println("❌ Fehler beim Löschen der Aufgabe: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    fun addShopItem(
        title: String,
        description: String,
        priceCoins: Int,
        maxPurchasesPerDay: Int
    ) {
        val operation = shopManager.prepareAddShopItem(
            currentData = _data.value,
            title = title,
            description = description,
            priceCoins = priceCoins,
            maxPurchasesPerDay = maxPurchasesPerDay
        )

        _data.value = operation.optimisticData

        viewModelScope.launch {
            try {
                shopManager.persistAddShopItem(operation)
            } catch (e: Exception) {
                println("❌ Fehler beim Speichern des Shop-Items: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    fun updateShopItem(
        itemId: String,
        title: String,
        description: String,
        priceCoins: Int,
        maxPurchasesPerDay: Int
    ) {
        val operation = shopManager.prepareUpdateShopItem(
            currentData = _data.value,
            itemId = itemId,
            title = title,
            description = description,
            priceCoins = priceCoins,
            maxPurchasesPerDay = maxPurchasesPerDay
        ) ?: return

        _data.value = operation.optimisticData

        viewModelScope.launch {
            try {
                shopManager.persistUpdateShopItem(operation)
            } catch (e: Exception) {
                println("❌ Fehler beim Speichern des Shop-Items: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    fun deleteShopItem(itemId: String) {
        val operation = shopManager.prepareDeleteShopItem(
            currentData = _data.value,
            itemId = itemId
        )

        _data.value = operation.optimisticData

        viewModelScope.launch {
            try {
                shopManager.persistDeleteShopItem(operation)
            } catch (e: Exception) {
                println("❌ Fehler beim Löschen des Shop-Items: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    fun addDogSchedule(
        childId: String,
        dayOfWeek: DayOfWeekName,
        careStartTime: String,
        careEndTime: String,
        feedingTime: String,
        walkTime: String
    ) {
        val operation = dogScheduleManager.prepareAddDogSchedule(
            currentData = _data.value,
            childId = childId,
            dayOfWeek = dayOfWeek,
            careStartTime = careStartTime,
            careEndTime = careEndTime,
            feedingTime = feedingTime,
            walkTime = walkTime
        )

        _data.value = operation.optimisticData

        viewModelScope.launch {
            try {
                dogScheduleManager.persistAddDogSchedule(operation)
            } catch (e: Exception) {
                println("❌ Fehler beim Speichern des Hundeplans: ${e.message}")
                e.printStackTrace()
            }
        }
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
        val operation = dogScheduleManager.prepareUpdateDogSchedule(
            currentData = _data.value,
            scheduleId = scheduleId,
            childId = childId,
            dayOfWeek = dayOfWeek,
            careStartTime = careStartTime,
            careEndTime = careEndTime,
            feedingTime = feedingTime,
            walkTime = walkTime
        ) ?: return

        _data.value = operation.optimisticData

        viewModelScope.launch {
            try {
                dogScheduleManager.persistUpdateDogSchedule(operation)
            } catch (e: Exception) {
                println("❌ Fehler beim Speichern des Hundeplans: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    fun deleteDogSchedule(scheduleId: String) {
        val operation = dogScheduleManager.prepareDeleteDogSchedule(
            currentData = _data.value,
            scheduleId = scheduleId
        )

        _data.value = operation.optimisticData

        viewModelScope.launch {
            try {
                dogScheduleManager.persistDeleteDogSchedule(operation)
            } catch (e: Exception) {
                println("❌ Fehler beim Löschen des Hundeplans: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    fun undoLogEntry(logId: String) {
        val operation = logManager.prepareUndoLog(
            currentData = _data.value,
            logId = logId
        ) ?: return

        _data.value = operation.optimisticData

        viewModelScope.launch {
            try {
                val realCoinValue = logManager.persistUndoLog(operation)

                _data.value = logManager.applyRealCoinValue(
                    currentData = _data.value,
                    childId = operation.childId,
                    realCoinValue = realCoinValue
                )
            } catch (e: Exception) {
                _data.value = operation.originalData
                println("❌ Fehler beim Rückgängig machen des Logs: ${e.message}")
                e.printStackTrace()
                showMessage("❌ Aktion konnte nicht rückgängig gemacht werden")
            }
        }
    }

    fun resetDemoData() {
        val demoData = backupManager.prepareResetDemoData()

        _data.value = demoData
        _selectedChildId.value = null
        _selectedDate.value = LocalDate.now()

        viewModelScope.launch {
            try {
                backupManager.persistResetDemoData(demoData)
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
                _isLoading.value = true

                backupManager.createCloudBackup(_data.value)

                showMessage("✅ Cloud-Backup erfolgreich erstellt")
            } catch (e: Exception) {
                println("❌ Backup fehlgeschlagen: ${e.message}")
                e.printStackTrace()
                showMessage("❌ Backup fehlgeschlagen")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun restoreFromBackup() {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                val safeBackup = backupManager.loadSafeCloudBackup()

                if (safeBackup != null) {
                    backupManager.persistRestoredBackup(safeBackup)
                    _data.value = safeBackup

                    showMessage("✅ Cloud-Backup erfolgreich wiederhergestellt")
                } else {
                    showMessage("⚠️ Kein Cloud-Backup gefunden")
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
        showMessage(backupManager.getImportFromJsonMessage())
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
