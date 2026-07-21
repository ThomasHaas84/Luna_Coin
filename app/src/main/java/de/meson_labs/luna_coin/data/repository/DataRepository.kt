package de.meson_labs.luna_coin.data.repository

import de.meson_labs.luna_coin.models.*

interface DataRepository {
    suspend fun loadData(): LunaCoinData?
    suspend fun saveData(data: LunaCoinData)
    suspend fun createCloudBackup(data: LunaCoinData)
    suspend fun loadCloudBackup(): LunaCoinData?
    fun startRealtimeSync(onDataChanged: (LunaCoinData) -> Unit, onError: (Exception) -> Unit)
    fun stopRealtimeSync()
    suspend fun loadChildren(): List<Child>
    suspend fun loadTasks(): List<TaskItem>
    suspend fun loadShopItems(): List<ShopItem>
    suspend fun loadDogSchedule(): List<DogScheduleItem>
    suspend fun loadDogPlan(): DogPlanData
    suspend fun loadDogPlanTemplates(): List<DogPlanTaskTemplate>
    suspend fun loadDogPlanCompletions(): List<DogPlanTaskCompletion>
    suspend fun loadDogPlanShifts(): List<DogPlanShift>
    suspend fun loadLogs(limit: Long = 2000): List<LogEntry>
    suspend fun loadLuckyWheelUsage(): List<LuckyWheelUsage>
    suspend fun loadGameHighscores(): List<GameHighscore>
    suspend fun loadGameDailyRewards(): List<GameDailyReward>
    suspend fun loadGameSettings(): GameSettings
    suspend fun saveChild(child: Child)
    suspend fun saveTask(task: TaskItem)
    suspend fun saveShopItem(shopItem: ShopItem)
    suspend fun saveDogScheduleItem(item: DogScheduleItem)
    suspend fun saveDogPlan(data: DogPlanData)
    suspend fun saveDogPlanTemplate(template: DogPlanTaskTemplate)
    suspend fun saveDogPlanCompletion(completion: DogPlanTaskCompletion)
    suspend fun saveDogPlanShift(shift: DogPlanShift)
    suspend fun saveLog(log: LogEntry)
    suspend fun saveLuckyWheelUsage(usage: LuckyWheelUsage)
    suspend fun saveGameHighscore(highscore: GameHighscore)
    suspend fun saveGameDailyReward(reward: GameDailyReward)
    suspend fun saveGameSettings(settings: GameSettings)
    suspend fun updateChildInventory(childId: String, inventory: List<LunaInventoryItem>, equippedItem: LunaInventoryItem?, profileImageItem: LunaInventoryItem?, hasProfileImage: Boolean)
    suspend fun updateChildProfile(childId: String, name: String, role: UserRole, password: String, age: Int, passwordRequired: Boolean, allowRememberLogin: Boolean, isBuiltInAdmin: Boolean)
    suspend fun updateChildProgress(childId: String, level: Int, experience: Int, availableSkillPoints: Int, intelligence: Int, strength: Int, agility: Int, endurance: Int, perception: Int, charisma: Int, luck: Int) {}
    suspend fun changeChildCoinsAndExperience(childId: String, coinDelta: Int, experienceDelta: Int): Child {
        val currentChild = loadChildren().firstOrNull { it.id == childId } ?: error("Benutzer nicht gefunden")
        return de.meson_labs.luna_coin.manager.ProgressManager.addCoinsAndExperience(currentChild, coinDelta, experienceDelta).also { saveChild(it) }
    }
    suspend fun deleteChild(childId: String)
    suspend fun deleteTask(taskId: String)
    suspend fun deleteShopItem(shopItemId: String)
    suspend fun deleteDogScheduleItem(itemId: String)
    suspend fun deleteDogPlanTemplate(templateId: String)
    suspend fun deleteDogPlanCompletion(completionId: String)
    suspend fun deleteDogPlanShift(shiftId: String)
    suspend fun deleteLog(logId: String)
    suspend fun deleteLuckyWheelUsage(usageId: String)
    suspend fun deleteGameHighscore(highscoreId: String)
    suspend fun deleteGameDailyReward(rewardId: String)
    suspend fun deleteGameHighscoresByGame(game: LunaGameType)
    suspend fun deleteAllGameHighscores()
    suspend fun setChildCoins(childId: String, coins: Int): Int
    suspend fun changeChildCoins(childId: String, coinDelta: Int): Int
    suspend fun setChildSilver(childId: String, silver: Long): Long
    suspend fun changeChildSilver(childId: String, silverDelta: Long): Long
    suspend fun convertCoinsToSilver(childId: String, coinAmount: Int, log: LogEntry): Pair<Int, Long>
    suspend fun transferCurrency(senderId: String, recipientId: String, amount: Long, currency: CurrencyType, senderLog: LogEntry, recipientLog: LogEntry): Pair<Long, Long> {
        throw UnsupportedOperationException("Währungsübertragungen werden von diesem Repository nicht unterstützt")
    }
    suspend fun transferCoins(senderId: String, recipientId: String, amount: Int, senderLog: LogEntry, recipientLog: LogEntry): Pair<Int, Int> {
        val result = transferCurrency(senderId, recipientId, amount.toLong(), CurrencyType.LUNA_COIN, senderLog, recipientLog)
        return result.first.toInt() to result.second.toInt()
    }
}
