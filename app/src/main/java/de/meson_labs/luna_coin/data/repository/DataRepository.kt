package de.meson_labs.luna_coin.data.repository

import de.meson_labs.luna_coin.models.Child
import de.meson_labs.luna_coin.models.DogPlanData
import de.meson_labs.luna_coin.models.DogPlanShift
import de.meson_labs.luna_coin.models.DogPlanTaskCompletion
import de.meson_labs.luna_coin.models.DogPlanTaskTemplate
import de.meson_labs.luna_coin.models.DogScheduleItem
import de.meson_labs.luna_coin.models.GameHighscore
import de.meson_labs.luna_coin.models.LogEntry
import de.meson_labs.luna_coin.models.LuckyWheelUsage
import de.meson_labs.luna_coin.models.LunaCoinData
import de.meson_labs.luna_coin.models.LunaInventoryItem
import de.meson_labs.luna_coin.models.ShopItem
import de.meson_labs.luna_coin.models.TaskItem
import de.meson_labs.luna_coin.models.UserRole

interface DataRepository {

    suspend fun loadData(): LunaCoinData?
    suspend fun saveData(data: LunaCoinData)

    suspend fun createCloudBackup(data: LunaCoinData)
    suspend fun loadCloudBackup(): LunaCoinData?

    fun startRealtimeSync(
        onDataChanged: (LunaCoinData) -> Unit,
        onError: (Exception) -> Unit
    )

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

    suspend fun updateChildInventory(
        childId: String,
        inventory: List<LunaInventoryItem>,
        equippedItem: LunaInventoryItem?,
        profileImageItem: LunaInventoryItem?,
        hasProfileImage: Boolean
    )

    suspend fun updateChildProfile(
        childId: String,
        name: String,
        role: UserRole,
        password: String,
        age: Int,
        passwordRequired: Boolean,
        allowRememberLogin: Boolean,
        isBuiltInAdmin: Boolean
    )

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

    suspend fun setChildCoins(
        childId: String,
        coins: Int
    ): Int

    suspend fun changeChildCoins(
        childId: String,
        coinDelta: Int
    ): Int
}
