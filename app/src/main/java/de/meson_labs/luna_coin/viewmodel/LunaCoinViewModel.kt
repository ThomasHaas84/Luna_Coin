package de.meson_labs.luna_coin.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import de.meson_labs.luna_coin.data.DemoData
import de.meson_labs.luna_coin.data.DogPlanDefaultData
import de.meson_labs.luna_coin.data.repository.DataRepository
import de.meson_labs.luna_coin.manager.GameHighscoreManager
import de.meson_labs.luna_coin.manager.GameResultScore
import de.meson_labs.luna_coin.manager.DogScheduleManager
import de.meson_labs.luna_coin.manager.DogPlanManager
import de.meson_labs.luna_coin.manager.BackupManager
import de.meson_labs.luna_coin.manager.ShopManager
import de.meson_labs.luna_coin.manager.BuyShopItemPreparation
import de.meson_labs.luna_coin.manager.UserManager
import de.meson_labs.luna_coin.manager.CoinManager
import de.meson_labs.luna_coin.manager.CoinTransferManager
import de.meson_labs.luna_coin.manager.BuyLunaMeItemPreparation
import de.meson_labs.luna_coin.manager.InventoryManager
import de.meson_labs.luna_coin.manager.LuckyWheelManager
import de.meson_labs.luna_coin.manager.TaskManager
import de.meson_labs.luna_coin.manager.LogManager
import de.meson_labs.luna_coin.manager.ProgressManager
import de.meson_labs.luna_coin.manager.ProgressSkill
import de.meson_labs.luna_coin.manager.ensureBuiltInAdmin
import de.meson_labs.luna_coin.manager.sortChildrenInData
import de.meson_labs.luna_coin.models.Child
import de.meson_labs.luna_coin.models.DayOfWeekName
import de.meson_labs.luna_coin.models.DogPlanTaskTemplate
import de.meson_labs.luna_coin.models.DogPlanTaskType
import de.meson_labs.luna_coin.models.LogEntry
import de.meson_labs.luna_coin.models.LogType
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
import java.time.LocalDateTime
import java.util.UUID

class LunaCoinViewModel(
    private val repository: DataRepository
) : ViewModel() {

    private val gameHighscoreManager = GameHighscoreManager(repository)
    private val dogScheduleManager = DogScheduleManager(repository)
    private val dogPlanManager = DogPlanManager()
    private val backupManager = BackupManager(repository)
    private val shopManager = ShopManager(repository)
    private val userManager = UserManager(repository)
    private val coinManager = CoinManager(repository)
    private val coinTransferManager = CoinTransferManager(repository)
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
    private var automaticHighscoreResetRunning = false

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
                    val safeDemoData = demoData.copy(
                        dogPlan = if (demoData.dogPlan.templates.isEmpty()) {
                            DogPlanDefaultData.create()
                        } else {
                            demoData.dogPlan
                        }
                    )
                    _data.value = sortChildrenInData(safeDemoData)
                    showMessage("⚠️ Keine Cloud-Daten gefunden")
                } else {
                    val loadedDogPlan = repository.loadDogPlan()
                    val safeDogPlan = if (loadedDogPlan.templates.isEmpty()) {
                        DogPlanDefaultData.create().also { defaultDogPlan ->
                            repository.saveDogPlan(defaultDogPlan)
                        }
                    } else {
                        loadedDogPlan
                    }

                    val loadedData = ensureBuiltInAdmin(
                        LunaCoinData(
                            children = children,
                            tasks = repository.loadTasks(),
                            shopItems = repository.loadShopItems(),
                            dogSchedule = repository.loadDogSchedule(),
                            dogPlan = safeDogPlan,
                            logs = repository.loadLogs(),
                            luckyWheelUsage = repository.loadLuckyWheelUsage(),
                            gameHighscores = repository.loadGameHighscores(),
                            gameDailyRewards = repository.loadGameDailyRewards(),
                            gameSettings = repository.loadGameSettings()
                        )
                    )

                    _data.value = sortChildrenInData(loadedData)
                    checkAutomaticWeeklyHighscoreReset()

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
                    val realtimeDataWithDogPlan = if (realtimeData.dogPlan.templates.isEmpty()) {
                        realtimeData.copy(
                            dogPlan = DogPlanDefaultData.create()
                        )
                    } else {
                        realtimeData
                    }
                    val safeData = sortChildrenInData(ensureBuiltInAdmin(realtimeDataWithDogPlan))

                    _data.value = safeData
                    checkAutomaticWeeklyHighscoreReset()

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

    fun transferCoins(
        senderId: String,
        recipientId: String,
        amount: Int,
        comment: String,
        onResult: (Boolean, String?) -> Unit = { _, _ -> }
    ) {
        val sender = _data.value.children.firstOrNull { it.id == senderId }
        val recipient = _data.value.children.firstOrNull { it.id == recipientId }

        if (sender == null || recipient == null) {
            val error = "Sender oder Empfänger wurde nicht gefunden."
            showMessage(error)
            onResult(false, error)
            return
        }

        viewModelScope.launch {
            try {
                val result = coinTransferManager.transfer(sender, recipient, amount, comment)
                _data.value = _data.value.copy(
                    children = _data.value.children.map { child ->
                        when (child.id) {
                            sender.id -> child.copy(coins = result.newSenderCoins)
                            recipient.id -> child.copy(coins = result.newRecipientCoins)
                            else -> child
                        }
                    },
                    logs = listOf(result.senderLog, result.recipientLog) + _data.value.logs
                )
                val text = "$amount Luna Coin${if (amount == 1) "" else "s"} wurden an ${recipient.name} gesendet."
                showMessage(text)
                onResult(true, null)
            } catch (error: Exception) {
                val text = error.message ?: "Coins konnten nicht gesendet werden."
                showMessage(text)
                onResult(false, text)
            }
        }
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

    fun setAutomaticWeeklyHighscoreReset(enabled: Boolean) {
        val originalData = _data.value
        val updatedSettings = originalData.gameSettings.copy(
            autoWeeklyHighscoreResetEnabled = enabled
        )

        _data.value = originalData.copy(
            gameSettings = updatedSettings
        )

        viewModelScope.launch {
            try {
                repository.saveGameSettings(updatedSettings)

                if (enabled) {
                    checkAutomaticWeeklyHighscoreReset()
                }

                showMessage(
                    if (enabled) {
                        "✅ Automatischer Wochen-Reset aktiviert"
                    } else {
                        "✅ Automatischer Wochen-Reset deaktiviert"
                    }
                )
            } catch (e: Exception) {
                _data.value = originalData
                println("❌ Fehler beim Speichern der Highscore-Einstellungen: ${e.message}")
                e.printStackTrace()
                showMessage("❌ Einstellung konnte nicht gespeichert werden")
            }
        }
    }

    fun resetHighscoresForGame(game: LunaGameType) {
        val originalData = _data.value
        val remainingHighscores = originalData.gameHighscores.filterNot { highscore ->
            highscore.game == game
        }

        _data.value = originalData.copy(
            gameHighscores = remainingHighscores
        )

        viewModelScope.launch {
            try {
                repository.deleteGameHighscoresByGame(game)
                showMessage("✅ ${getGameDisplayName(game)}-Highscores gelöscht")
            } catch (e: Exception) {
                _data.value = originalData
                println("❌ Fehler beim Löschen der ${game.name}-Highscores: ${e.message}")
                e.printStackTrace()
                showMessage("❌ Highscores konnten nicht gelöscht werden")
            }
        }
    }

    fun resetAllHighscores() {
        val originalData = _data.value

        _data.value = originalData.copy(
            gameHighscores = emptyList()
        )

        viewModelScope.launch {
            try {
                repository.deleteAllGameHighscores()
                showMessage("✅ Alle Highscores gelöscht")
            } catch (e: Exception) {
                _data.value = originalData
                println("❌ Fehler beim Löschen aller Highscores: ${e.message}")
                e.printStackTrace()
                showMessage("❌ Highscores konnten nicht gelöscht werden")
            }
        }
    }

    fun checkAutomaticWeeklyHighscoreReset() {
        if (automaticHighscoreResetRunning) return

        val currentData = _data.value
        val settings = currentData.gameSettings

        if (!settings.autoWeeklyHighscoreResetEnabled) return

        val today = LocalDate.now()
        val currentWeekMonday = today.minusDays((today.dayOfWeek.value - 1).toLong())
        val lastResetDate = settings.lastAutomaticHighscoreResetDate
            ?.let { storedDate ->
                runCatching { LocalDate.parse(storedDate) }.getOrNull()
            }

        if (lastResetDate != null && !lastResetDate.isBefore(currentWeekMonday)) {
            return
        }

        automaticHighscoreResetRunning = true

        val originalData = _data.value
        val updatedSettings = originalData.gameSettings.copy(
            lastAutomaticHighscoreResetDate = today.toString()
        )

        _data.value = originalData.copy(
            gameHighscores = emptyList(),
            gameSettings = updatedSettings
        )

        viewModelScope.launch {
            try {
                repository.deleteAllGameHighscores()
                repository.saveGameSettings(updatedSettings)
                showMessage("✅ Wöchentlicher Highscore-Reset durchgeführt")
            } catch (e: Exception) {
                _data.value = originalData
                println("❌ Automatischer Highscore-Reset fehlgeschlagen: ${e.message}")
                e.printStackTrace()
                showMessage("❌ Automatischer Highscore-Reset fehlgeschlagen")
            } finally {
                automaticHighscoreResetRunning = false
            }
        }
    }

    private fun getGameDisplayName(game: LunaGameType): String {
        return when (game) {
            LunaGameType.MEMORY -> "Memory"
            LunaGameType.NUMBER_GUESS -> "Zahlenraten"
            LunaGameType.MULTIPLICATION -> "Einmaleins"
            LunaGameType.WORD_GUESS -> "Wort-Raten"
        }
    }


    fun finishGame(
        game: LunaGameType,
        childId: String,
        level: LunaGameLevel,
        scores: List<GameResultScore>
    ) {
        val operation = gameHighscoreManager.prepareFinishGame(
            currentData = _data.value,
            game = game,
            childId = childId,
            level = level,
            scores = scores
        ) ?: return

        _data.value = operation.optimisticData

        viewModelScope.launch {
            try {
                val persistedChild = gameHighscoreManager.persistFinishGame(operation)

                _data.value = gameHighscoreManager.applyPersistedChildProgress(
                    currentData = _data.value,
                    persistedChild = persistedChild
                )

                showLevelUpIfNeeded(
                    originalData = operation.originalData,
                    persistedChild = persistedChild
                )
            } catch (e: Exception) {
                _data.value = operation.originalData
                println("❌ Fehler beim Speichern des Spielabschlusses: ${e.message}")
                e.printStackTrace()
                showMessage("❌ Spielabschluss konnte nicht gespeichert werden")
            }
        }
    }

    fun finishNumberGuessGame(
        childId: String,
        attempts: Int
    ) {
        finishGame(
            game = LunaGameType.NUMBER_GUESS,
            childId = childId,
            level = LunaGameLevel.DEFAULT,
            scores = listOf(
                GameResultScore(
                    scoreType = LunaGameScoreType.ATTEMPTS,
                    value = attempts
                )
            )
        )
    }

    fun finishMemoryGame(
        childId: String,
        level: LunaGameLevel,
        moves: Int,
        timeSeconds: Int
    ) {
        finishGame(
            game = LunaGameType.MEMORY,
            childId = childId,
            level = level,
            scores = listOf(
                GameResultScore(
                    scoreType = LunaGameScoreType.ATTEMPTS,
                    value = moves
                ),
                GameResultScore(
                    scoreType = LunaGameScoreType.TIME_SECONDS,
                    value = timeSeconds
                )
            )
        )
    }

    fun finishMultiplicationGame(
        childId: String,
        timeSeconds: Int
    ) {
        finishGame(
            game = LunaGameType.MULTIPLICATION,
            childId = childId,
            level = LunaGameLevel.DEFAULT,
            scores = listOf(
                GameResultScore(
                    scoreType = LunaGameScoreType.TIME_SECONDS,
                    value = timeSeconds
                )
            )
        )
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
                val persistedChild = gameHighscoreManager.persistHighscore(operation)

                _data.value = gameHighscoreManager.applyPersistedChildProgress(
                    currentData = _data.value,
                    persistedChild = persistedChild
                )

                showLevelUpIfNeeded(
                    originalData = operation.originalData,
                    persistedChild = persistedChild
                )
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
                val persistedChild = taskManager.persistCompleteTask(operation)

                _data.value = taskManager.applyPersistedChildProgress(
                    currentData = _data.value,
                    persistedChild = persistedChild
                )

                showLevelUpIfNeeded(
                    originalData = operation.originalData,
                    persistedChild = persistedChild
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


    fun saveDogPlanTaskTemplate(template: DogPlanTaskTemplate) {
        val currentUser = getSelectedChild() ?: return

        val operation = dogPlanManager.prepareSaveTemplate(
            currentDogPlanData = _data.value.dogPlan,
            template = template,
            currentUser = currentUser
        ) ?: run {
            showMessage("❌ Hundeplan-Aufgabe konnte nicht gespeichert werden")
            return
        }

        val originalData = _data.value
        _data.value = originalData.copy(
            dogPlan = operation.dogPlanData
        )

        viewModelScope.launch {
            try {
                repository.saveDogPlanTemplate(operation.template)
                showMessage("✅ Hundeplan-Aufgabe gespeichert")
            } catch (e: Exception) {
                _data.value = originalData
                println("❌ Fehler beim Speichern der Hundeplan-Aufgabe: ${e.message}")
                e.printStackTrace()
                showMessage("❌ Hundeplan-Aufgabe konnte nicht gespeichert werden")
            }
        }
    }

    fun deleteDogPlanTaskTemplate(templateId: String) {
        val currentUser = getSelectedChild() ?: return

        val updatedDogPlan = dogPlanManager.prepareDeleteTemplate(
            currentDogPlanData = _data.value.dogPlan,
            templateId = templateId,
            currentUser = currentUser
        ) ?: run {
            showMessage("❌ Hundeplan-Aufgabe konnte nicht gelöscht werden")
            return
        }

        val originalData = _data.value
        _data.value = originalData.copy(
            dogPlan = updatedDogPlan
        )

        viewModelScope.launch {
            try {
                repository.saveDogPlan(updatedDogPlan)
                showMessage("✅ Hundeplan-Aufgabe gelöscht")
            } catch (e: Exception) {
                _data.value = originalData
                println("❌ Fehler beim Löschen der Hundeplan-Aufgabe: ${e.message}")
                e.printStackTrace()
                showMessage("❌ Hundeplan-Aufgabe konnte nicht gelöscht werden")
            }
        }
    }

    fun completeDogPlanTask(
        templateId: String,
        date: String,
        peed: Boolean,
        pooped: Boolean,
        diarrhea: Boolean,
        comment: String
    ) {
        val currentUser = getSelectedChild() ?: return

        val operation = dogPlanManager.prepareCompleteTask(
            currentDogPlanData = _data.value.dogPlan,
            templateId = templateId,
            date = date,
            completedByChild = currentUser,
            peed = peed,
            pooped = pooped,
            diarrhea = diarrhea,
            comment = comment
        ) ?: run {
            showMessage("❌ Hundeplan-Aufgabe konnte nicht erledigt werden")
            return
        }

        val originalData = _data.value
        val optimisticChild = if (operation.isEditing) {
            currentUser
        } else {
            ProgressManager.addTaskReward(
                child = currentUser,
                rewardCoins = operation.rewardCoins
            )
        }

        _data.value = sortChildrenInData(
            originalData.copy(
                dogPlan = operation.dogPlanData,
                children = originalData.children.map { child ->
                    if (child.id == currentUser.id) {
                        optimisticChild
                    } else {
                        child
                    }
                }
            )
        )

        viewModelScope.launch {
            try {
                repository.saveDogPlanCompletion(operation.completion)

                if (!operation.isEditing && operation.rewardCoins > 0) {
                    val persistedChild = repository.changeChildCoinsAndExperience(
                        childId = currentUser.id,
                        coinDelta = operation.rewardCoins,
                        experienceDelta = operation.rewardCoins
                    )

                    _data.value = sortChildrenInData(
                        _data.value.copy(
                            children = _data.value.children.map { child ->
                                if (child.id == currentUser.id) {
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

                    showLevelUpIfNeeded(
                        originalData = originalData,
                        persistedChild = persistedChild
                    )
                }

                val template = _data.value.dogPlan.templates.firstOrNull { dogTemplate ->
                    dogTemplate.id == operation.completion.templateId
                }

                val log = LogEntry(
                    id = UUID.randomUUID().toString(),
                    timestamp = LocalDateTime.now().toString(),
                    childId = currentUser.id,
                    type = LogType.DOG_PLAN,
                    text = if (operation.isEditing) {
                        createDogPlanEditLogText(
                            editorName = currentUser.name,
                            templateTitle = template?.title ?: "Hundeplan-Aufgabe",
                            templateType = template?.type ?: DogPlanTaskType.OTHER,
                            peed = operation.completion.peed,
                            pooped = operation.completion.pooped,
                            diarrhea = operation.completion.diarrhea,
                            comment = operation.completion.comment
                        )
                    } else {
                        createDogPlanLogText(
                            childName = currentUser.name,
                            templateTitle = template?.title ?: "Hundeplan-Aufgabe",
                            templateType = template?.type ?: DogPlanTaskType.OTHER,
                            rewardCoins = operation.completion.rewardCoins,
                            peed = operation.completion.peed,
                            pooped = operation.completion.pooped,
                            diarrhea = operation.completion.diarrhea,
                            comment = operation.completion.comment
                        )
                    },
                    coinChange = if (operation.isEditing) 0 else operation.completion.rewardCoins
                )

                repository.saveLog(log)

                _data.value = _data.value.copy(
                    logs = listOf(log) + _data.value.logs
                )

                showMessage(
                    if (operation.isEditing) {
                        "✅ Hundeplan-Eintrag geändert"
                    } else {
                        "✅ Hundeplan-Aufgabe erledigt"
                    }
                )
            } catch (e: Exception) {
                _data.value = originalData
                println("❌ Fehler beim Erledigen der Hundeplan-Aufgabe: ${e.message}")
                e.printStackTrace()
                showMessage("❌ Hundeplan-Aufgabe konnte nicht gespeichert werden")
            }
        }
    }

    fun assignDogPlanEarlyShift(
        date: String,
        childId: String
    ) {
        val currentUser = getSelectedChild() ?: return

        val operation = dogPlanManager.prepareAssignEarlyShift(
            currentDogPlanData = _data.value.dogPlan,
            date = date,
            childId = childId,
            currentUser = currentUser
        ) ?: return

        saveDogPlanShiftOperation(
            updatedDogPlan = operation.dogPlanData,
            shift = operation.shift,
            successMessage = "✅ Frühschicht eingetragen",
            errorMessage = "❌ Frühschicht konnte nicht gespeichert werden"
        )
    }

    fun assignDogPlanLateShift(
        date: String,
        childId: String
    ) {
        val currentUser = getSelectedChild() ?: return

        val operation = dogPlanManager.prepareAssignLateShift(
            currentDogPlanData = _data.value.dogPlan,
            date = date,
            childId = childId,
            currentUser = currentUser
        ) ?: return

        saveDogPlanShiftOperation(
            updatedDogPlan = operation.dogPlanData,
            shift = operation.shift,
            successMessage = "✅ Spätschicht eingetragen",
            errorMessage = "❌ Spätschicht konnte nicht gespeichert werden"
        )
    }

    fun clearDogPlanEarlyShift(date: String) {
        val currentUser = getSelectedChild() ?: return

        val operation = dogPlanManager.prepareClearEarlyShift(
            currentDogPlanData = _data.value.dogPlan,
            date = date,
            currentUser = currentUser
        ) ?: return

        saveDogPlanShiftOperation(
            updatedDogPlan = operation.dogPlanData,
            shift = operation.shift,
            successMessage = "✅ Frühschicht entfernt",
            errorMessage = "❌ Frühschicht konnte nicht entfernt werden"
        )
    }

    fun clearDogPlanLateShift(date: String) {
        val currentUser = getSelectedChild() ?: return

        val operation = dogPlanManager.prepareClearLateShift(
            currentDogPlanData = _data.value.dogPlan,
            date = date,
            currentUser = currentUser
        ) ?: return

        saveDogPlanShiftOperation(
            updatedDogPlan = operation.dogPlanData,
            shift = operation.shift,
            successMessage = "✅ Spätschicht entfernt",
            errorMessage = "❌ Spätschicht konnte nicht entfernt werden"
        )
    }

    private fun saveDogPlanShiftOperation(
        updatedDogPlan: de.meson_labs.luna_coin.models.DogPlanData,
        shift: de.meson_labs.luna_coin.models.DogPlanShift,
        successMessage: String,
        errorMessage: String
    ) {
        val originalData = _data.value
        _data.value = originalData.copy(
            dogPlan = updatedDogPlan
        )

        viewModelScope.launch {
            try {
                repository.saveDogPlanShift(shift)
                showMessage(successMessage)
            } catch (e: Exception) {
                _data.value = originalData
                println("❌ Fehler beim Speichern der Hundeplan-Schicht: ${e.message}")
                e.printStackTrace()
                showMessage(errorMessage)
            }
        }
    }



    fun updateChildProgressAsAdmin(
        childId: String,
        coins: Int,
        experience: Int,
        availableSkillPoints: Int,
        intelligence: Int,
        strength: Int,
        agility: Int,
        comment: String?
    ) {
        val currentChild = _data.value.children.firstOrNull { child ->
            child.id == childId
        } ?: return

        val updatedChild = ProgressManager.setAdminProgress(
            child = currentChild,
            coins = coins,
            experience = experience,
            availableSkillPoints = availableSkillPoints,
            intelligence = intelligence,
            strength = strength,
            agility = agility
        )

        val originalData = _data.value

        val log = LogEntry(
            id = UUID.randomUUID().toString(),
            timestamp = LocalDateTime.now().toString(),
            childId = childId,
            type = LogType.SYSTEM,
            text = buildString {
                append("Admin hat den Fortschritt von ${currentChild.name} angepasst.")
                append("\nCoins: ${currentChild.coins} → ${updatedChild.coins}")
                append("\nEP: ${currentChild.experience} → ${updatedChild.experience}")
                append("\nLevel: ${currentChild.level} → ${updatedChild.level}")
                append("\nSkillpunkte: ${currentChild.availableSkillPoints} → ${updatedChild.availableSkillPoints}")
                append("\nIntelligenz: ${currentChild.intelligence} → ${updatedChild.intelligence}")
                append("\nStärke: ${currentChild.strength} → ${updatedChild.strength}")
                append("\nGeschicklichkeit: ${currentChild.agility} → ${updatedChild.agility}")

                val safeComment = comment?.trim().orEmpty()
                if (safeComment.isNotBlank()) {
                    append("\nKommentar: $safeComment")
                }
            },
            coinChange = updatedChild.coins - currentChild.coins
        )

        _data.value = sortChildrenInData(
            originalData.copy(
                children = originalData.children.map { child ->
                    if (child.id == childId) updatedChild else child
                },
                logs = listOf(log) + originalData.logs
            )
        )

        viewModelScope.launch {
            try {
                repository.setChildCoins(
                    childId = updatedChild.id,
                    coins = updatedChild.coins
                )

                repository.updateChildProgress(
                    childId = updatedChild.id,
                    level = updatedChild.level,
                    experience = updatedChild.experience,
                    availableSkillPoints = updatedChild.availableSkillPoints,
                    intelligence = updatedChild.intelligence,
                    strength = updatedChild.strength,
                    agility = updatedChild.agility
                )

                repository.saveLog(log)

                showMessage("✅ Fortschritt gespeichert")
            } catch (e: Exception) {
                _data.value = originalData
                println("❌ Fehler beim Speichern des Fortschritts: ${e.message}")
                e.printStackTrace()
                showMessage("❌ Fortschritt konnte nicht gespeichert werden")
            }
        }
    }

    fun increaseIntelligence() {
        increaseSkill(ProgressSkill.INTELLIGENCE)
    }

    fun increaseStrength() {
        increaseSkill(ProgressSkill.STRENGTH)
    }

    fun increaseAgility() {
        increaseSkill(ProgressSkill.AGILITY)
    }

    private fun increaseSkill(skillType: ProgressSkill) {
        val childId = _selectedChildId.value ?: return
        val currentChild = _data.value.children.firstOrNull { it.id == childId } ?: return
        val updatedChild = ProgressManager.increaseSkill(
            child = currentChild,
            skill = skillType
        )

        if (updatedChild == currentChild) return

        val originalData = _data.value

        _data.value = sortChildrenInData(
            originalData.copy(
                children = originalData.children.map { child ->
                    if (child.id == childId) updatedChild else child
                }
            )
        )

        viewModelScope.launch {
            try {
                repository.updateChildProgress(
                    childId = updatedChild.id,
                    level = updatedChild.level,
                    experience = updatedChild.experience,
                    availableSkillPoints = updatedChild.availableSkillPoints,
                    intelligence = updatedChild.intelligence,
                    strength = updatedChild.strength,
                    agility = updatedChild.agility
                )
            } catch (e: Exception) {
                _data.value = originalData
                println("❌ Fehler beim Speichern des Skills: ${e.message}")
                e.printStackTrace()
                showMessage("❌ Skill konnte nicht gespeichert werden")
            }
        }
    }


    private fun showLevelUpIfNeeded(
        originalData: LunaCoinData,
        persistedChild: Child
    ) {
        val previousChild = originalData.children.firstOrNull { child ->
            child.id == persistedChild.id
        } ?: return

        if (persistedChild.level <= previousChild.level) return

        val gainedLevels = persistedChild.level - previousChild.level
        val skillPointText = if (gainedLevels == 1) {
            "1 neuer Skillpunkt"
        } else {
            "$gainedLevels neue Skillpunkte"
        }

        val childName = persistedChild.name.ifBlank {
            previousChild.name.ifBlank {
                "Luna"
            }
        }

        showMessage(
            "🎉 Level-Up! $childName ist jetzt Level ${persistedChild.level}. $skillPointText verfügbar!"
        )
    }

    private fun getSelectedChild(): Child? {
        val childId = _selectedChildId.value ?: return null
        return _data.value.children.firstOrNull { it.id == childId }
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

    fun awardGameFinished(childId: String) {
        awardGameWin(childId)
    }

    fun awardGameWin(childId: String) {
        if (childId.isBlank()) return

        val child = _data.value.children.firstOrNull { it.id == childId } ?: return
        val updatedChild = ProgressManager.addGameFinishedExperience(child)

        if (updatedChild == child) return

        val originalData = _data.value

        _data.value = sortChildrenInData(
            originalData.copy(
                children = originalData.children.map { currentChild ->
                    if (currentChild.id == childId) updatedChild else currentChild
                }
            )
        )

        viewModelScope.launch {
            try {
                val persistedChild = repository.changeChildCoinsAndExperience(
                    childId = updatedChild.id,
                    coinDelta = 0,
                    experienceDelta = ProgressManager.EXPERIENCE_PER_GAME_FINISHED
                )

                _data.value = sortChildrenInData(
                    _data.value.copy(
                        children = _data.value.children.map { currentChild ->
                            if (currentChild.id == persistedChild.id) {
                                currentChild.copy(
                                    coins = persistedChild.coins,
                                    level = persistedChild.level,
                                    experience = persistedChild.experience,
                                    availableSkillPoints = persistedChild.availableSkillPoints,
                                    intelligence = persistedChild.intelligence,
                                    strength = persistedChild.strength,
                                    agility = persistedChild.agility
                                )
                            } else {
                                currentChild
                            }
                        }
                    )
                )
            } catch (e: Exception) {
                _data.value = originalData
                println("❌ Fehler beim Speichern der Spiel-EP: ${e.message}")
                e.printStackTrace()
                showMessage("❌ Spiel-EP konnten nicht gespeichert werden")
            }
        }
    }

    fun resetDemoData() {
        val demoData = backupManager.prepareResetDemoData()
        val safeDemoData = if (demoData.dogPlan.templates.isEmpty()) {
            demoData.copy(
                dogPlan = DogPlanDefaultData.create()
            )
        } else {
            demoData
        }

        _data.value = safeDemoData
        _selectedChildId.value = null
        _selectedDate.value = LocalDate.now()

        viewModelScope.launch {
            try {
                backupManager.persistResetDemoData(safeDemoData)
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


    private fun createDogPlanEditLogText(
        editorName: String,
        templateTitle: String,
        templateType: DogPlanTaskType,
        peed: Boolean,
        pooped: Boolean,
        diarrhea: Boolean,
        comment: String
    ): String {
        val icon = when (templateType) {
            DogPlanTaskType.WALK -> "✏️🐶"
            DogPlanTaskType.FEEDING_EARLY -> "✏️🍖"
            DogPlanTaskType.FEEDING_LATE -> "✏️🍖"
            DogPlanTaskType.OTHER -> "✏️🐾"
        }

        val detailLines = mutableListOf<String>()

        if (templateType == DogPlanTaskType.WALK) {
            detailLines += if (peed) "✔ Gepinkelt" else "✖ Nicht gepinkelt"
            detailLines += if (pooped) "✔ Gekackt" else "✖ Nicht gekackt"
            detailLines += if (diarrhea) "⚠ Durchfall" else "✔ Kein Durchfall"
        }

        if (comment.isNotBlank()) {
            detailLines += "Kommentar: ${comment.trim()}"
        }

        val detailsText = if (detailLines.isNotEmpty()) {
            "\n" + detailLines.joinToString("\n")
        } else {
            ""
        }

        return "$icon $editorName hat den Eintrag „$templateTitle“ nachträglich bearbeitet.$detailsText"
    }

    private fun createDogPlanLogText(
        childName: String,
        templateTitle: String,
        templateType: DogPlanTaskType,
        rewardCoins: Int,
        peed: Boolean,
        pooped: Boolean,
        diarrhea: Boolean,
        comment: String
    ): String {
        val icon = when (templateType) {
            DogPlanTaskType.WALK -> "🐶"
            DogPlanTaskType.FEEDING_EARLY -> "🍖"
            DogPlanTaskType.FEEDING_LATE -> "🍖"
            DogPlanTaskType.OTHER -> "🐾"
        }

        val detailLines = mutableListOf<String>()

        if (templateType == DogPlanTaskType.WALK) {
            if (peed) detailLines += "✔ Gepinkelt"
            if (pooped) detailLines += "✔ Gekackt"
            if (diarrhea) detailLines += "⚠ Durchfall"
        }

        if (comment.isNotBlank()) {
            detailLines += "Kommentar: ${comment.trim()}"
        }

        val coinText = if (rewardCoins > 0) {
            "\n+${rewardCoins} Luna Coin${if (rewardCoins == 1) "" else "s"}"
        } else {
            ""
        }

        val detailsText = if (detailLines.isNotEmpty()) {
            "\n" + detailLines.joinToString("\n")
        } else {
            ""
        }

        return "$icon $childName hat $templateTitle erledigt.$coinText$detailsText"
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
