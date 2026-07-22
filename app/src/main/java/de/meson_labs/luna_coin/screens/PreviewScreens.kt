// screens/PreviewScreens.kt
package de.meson_labs.luna_coin.screens

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import de.meson_labs.luna_coin.data.DemoData
import de.meson_labs.luna_coin.data.repository.DataRepository
import de.meson_labs.luna_coin.models.Child
import de.meson_labs.luna_coin.models.DogScheduleItem
import de.meson_labs.luna_coin.models.DogPlanData
import de.meson_labs.luna_coin.models.DogPlanShift
import de.meson_labs.luna_coin.models.DogPlanTaskCompletion
import de.meson_labs.luna_coin.models.DogPlanTaskTemplate
import de.meson_labs.luna_coin.models.GameDailyReward
import de.meson_labs.luna_coin.models.GameHighscore
import de.meson_labs.luna_coin.models.GameSettings
import de.meson_labs.luna_coin.models.LogEntry
import de.meson_labs.luna_coin.models.LuckyWheelUsage
import de.meson_labs.luna_coin.models.LunaCoinData
import de.meson_labs.luna_coin.models.LunaGameType
import de.meson_labs.luna_coin.models.LunaInventoryItem
import de.meson_labs.luna_coin.models.ShopItem
import de.meson_labs.luna_coin.models.TaskItem
import de.meson_labs.luna_coin.models.UserRole
import de.meson_labs.luna_coin.screens.settings.SettingsScreen
import de.meson_labs.luna_coin.viewmodel.LunaCoinViewModel
import java.time.LocalDate

@SuppressLint("ViewModelConstructorInComposable")
@Preview(
    name = "User Selection",
    showBackground = true,
    widthDp = 1280,
    heightDp = 800
)
@Composable
fun UserSelectionScreenPreview() {
    val demoData = DemoData.create()
    val fakeRepository = FakeDataRepository(demoData)

    val viewModel = LunaCoinViewModel(repository = fakeRepository)

    UserSelectionScreen(
        viewModel = viewModel,
        onChildSelected = {}
    )
}

@Preview(
    name = "Tasks Screen",
    showBackground = true,
    widthDp = 1280,
    heightDp = 800
)
@Composable
fun TasksScreenPreview() {
    val demoData = DemoData.create()

    TasksScreen(
        data = demoData,
        selectedChild = demoData.children.firstOrNull(),
        selectedDate = LocalDate.now(),
        onPreviousDay = {},
        onNextDay = {},
        onToday = {},
        onCompleteTask = {},
        onLogout = {},
        canGoToPreviousDay = true,
        canGoToNextDay = true
    )
}

@Preview(
    name = "Shop Screen",
    showBackground = true,
    widthDp = 1280,
    heightDp = 800
)
@Composable
fun ShopScreenPreview() {
    val demoData = DemoData.create()

    ShopScreen(
        data = demoData,
        selectedChild = demoData.children.firstOrNull(),
        onBuyItem = {},
        onLuckyWheelResult = { _, _, result -> result },
        onLogout = {}
    )
}

@SuppressLint("ViewModelConstructorInComposable")
@Preview(
    name = "Settings Screen - Child",
    showBackground = true,
    widthDp = 1280,
    heightDp = 800
)
@Composable
fun SettingsScreenChildPreview() {
    val demoData = DemoData.create()
    val fakeRepository = FakeDataRepository(demoData)
    val viewModel = LunaCoinViewModel(repository = fakeRepository)

    SettingsScreen(
        data = demoData,
        selectedChild = demoData.children.firstOrNull(),
        selectedDate = LocalDate.now(),
        jsonText = "",
        viewModel = viewModel,

        onAddTask = { _, _, _, _, _, _, _, _, _, _, _ -> },
        onUpdateTask = { _, _, _, _, _, _, _, _, _, _, _, _ -> },
        onDeleteTask = {},

        onAddShopItem = { _, _, _, _ -> },
        onUpdateShopItem = { _, _, _, _, _ -> },
        onDeleteShopItem = {},

        onAddDogSchedule = { _, _, _, _, _, _ -> },
        onUpdateDogSchedule = { _, _, _, _, _, _, _ -> },
        onDeleteDogSchedule = {},

        onUpdateChildCoins = { _, _, _ -> },

        onUndoLogEntry = {},

        onResetDemoData = {},
        onCreateCloudBackup = {},
        onRestoreFromBackup = {},
        onImportFromJson = {},

        onLogout = {}
    )
}

@SuppressLint("ViewModelConstructorInComposable")
@Preview(
    name = "Settings Screen - Admin",
    showBackground = true,
    widthDp = 1280,
    heightDp = 800
)
@Composable
fun SettingsScreenAdminPreview() {
    val demoData = DemoData.create()
    val fakeRepository = FakeDataRepository(demoData)
    val viewModel = LunaCoinViewModel(repository = fakeRepository)

    val admin = demoData.children.firstOrNull { child ->
        child.isBuiltInAdmin || child.role == UserRole.ADMIN
    }

    SettingsScreen(
        data = demoData,
        selectedChild = admin,
        selectedDate = LocalDate.now(),
        jsonText = "",
        viewModel = viewModel,

        onAddTask = { _, _, _, _, _, _, _, _, _, _, _ -> },
        onUpdateTask = { _, _, _, _, _, _, _, _, _, _, _, _ -> },
        onDeleteTask = {},

        onAddShopItem = { _, _, _, _ -> },
        onUpdateShopItem = { _, _, _, _, _ -> },
        onDeleteShopItem = {},

        onAddDogSchedule = { _, _, _, _, _, _ -> },
        onUpdateDogSchedule = { _, _, _, _, _, _, _ -> },
        onDeleteDogSchedule = {},

        onUpdateChildCoins = { _, _, _ -> },

        onUndoLogEntry = {},

        onResetDemoData = {},
        onCreateCloudBackup = {},
        onRestoreFromBackup = {},
        onImportFromJson = {},

        onLogout = {}
    )
}

// ====================== FAKE REPOSITORY FÜR PREVIEWS ======================
class FakeDataRepository(
    private var demoData: LunaCoinData
) : DataRepository {

    override suspend fun loadData(): LunaCoinData? = demoData

    override suspend fun saveData(data: LunaCoinData) {
        demoData = data
    }

    override suspend fun createCloudBackup(data: LunaCoinData) {}

    override suspend fun loadCloudBackup(): LunaCoinData? {
        return demoData
    }

    override fun startRealtimeSync(
        onDataChanged: (LunaCoinData) -> Unit,
        onError: (Exception) -> Unit
    ) {
        onDataChanged(demoData)
    }

    override fun stopRealtimeSync() {}

    override suspend fun loadChildren(): List<Child> {
        return demoData.children
    }

    override suspend fun loadTasks(): List<TaskItem> {
        return demoData.tasks
    }

    override suspend fun loadShopItems(): List<ShopItem> {
        return demoData.shopItems
    }

    override suspend fun loadDogSchedule(): List<DogScheduleItem> {
        return demoData.dogSchedule
    }

    override suspend fun loadDogPlan(): DogPlanData {
        return demoData.dogPlan
    }

    override suspend fun loadDogPlanTemplates(): List<DogPlanTaskTemplate> {
        return demoData.dogPlan.templates
    }

    override suspend fun loadDogPlanCompletions(): List<DogPlanTaskCompletion> {
        return demoData.dogPlan.completions
    }

    override suspend fun loadDogPlanShifts(): List<DogPlanShift> {
        return demoData.dogPlan.shifts
    }

    override suspend fun loadLogs(limit: Long): List<LogEntry> {
        return demoData.logs
    }

    override suspend fun loadLuckyWheelUsage(): List<LuckyWheelUsage> {
        return demoData.luckyWheelUsage
    }

    override suspend fun loadGameHighscores(): List<GameHighscore> {
        return demoData.gameHighscores
    }

    override suspend fun loadGameDailyRewards(): List<GameDailyReward> {
        return demoData.gameDailyRewards
    }

    override suspend fun loadGameSettings(): GameSettings {
        return demoData.gameSettings
    }

    override suspend fun saveChild(child: Child) {
        demoData = demoData.copy(
            children = if (demoData.children.any { it.id == child.id }) {
                demoData.children.map {
                    if (it.id == child.id) child else it
                }
            } else {
                demoData.children + child
            }
        )
    }

    override suspend fun saveTask(task: TaskItem) {
        demoData = demoData.copy(
            tasks = if (demoData.tasks.any { it.id == task.id }) {
                demoData.tasks.map {
                    if (it.id == task.id) task else it
                }
            } else {
                demoData.tasks + task
            }
        )
    }

    override suspend fun saveShopItem(shopItem: ShopItem) {
        demoData = demoData.copy(
            shopItems = if (demoData.shopItems.any { it.id == shopItem.id }) {
                demoData.shopItems.map {
                    if (it.id == shopItem.id) shopItem else it
                }
            } else {
                demoData.shopItems + shopItem
            }
        )
    }

    override suspend fun saveDogScheduleItem(item: DogScheduleItem) {
        demoData = demoData.copy(
            dogSchedule = if (demoData.dogSchedule.any { it.id == item.id }) {
                demoData.dogSchedule.map {
                    if (it.id == item.id) item else it
                }
            } else {
                demoData.dogSchedule + item
            }
        )
    }

    override suspend fun saveDogPlan(data: DogPlanData) {
        demoData = demoData.copy(
            dogPlan = data
        )
    }

    override suspend fun saveDogPlanTemplate(template: DogPlanTaskTemplate) {
        val currentDogPlan = demoData.dogPlan

        demoData = demoData.copy(
            dogPlan = currentDogPlan.copy(
                templates = if (currentDogPlan.templates.any { it.id == template.id }) {
                    currentDogPlan.templates.map {
                        if (it.id == template.id) template else it
                    }
                } else {
                    currentDogPlan.templates + template
                }
            )
        )
    }

    override suspend fun saveDogPlanCompletion(completion: DogPlanTaskCompletion) {
        val currentDogPlan = demoData.dogPlan

        demoData = demoData.copy(
            dogPlan = currentDogPlan.copy(
                completions = if (currentDogPlan.completions.any { it.id == completion.id }) {
                    currentDogPlan.completions.map {
                        if (it.id == completion.id) completion else it
                    }
                } else {
                    currentDogPlan.completions + completion
                }
            )
        )
    }

    override suspend fun saveDogPlanShift(shift: DogPlanShift) {
        val currentDogPlan = demoData.dogPlan

        demoData = demoData.copy(
            dogPlan = currentDogPlan.copy(
                shifts = if (currentDogPlan.shifts.any { it.id == shift.id }) {
                    currentDogPlan.shifts.map {
                        if (it.id == shift.id) shift else it
                    }
                } else {
                    currentDogPlan.shifts + shift
                }
            )
        )
    }

    override suspend fun saveLog(log: LogEntry) {
        demoData = demoData.copy(
            logs = listOf(log) + demoData.logs
        )
    }

    override suspend fun saveLuckyWheelUsage(usage: LuckyWheelUsage) {
        demoData = demoData.copy(
            luckyWheelUsage = if (demoData.luckyWheelUsage.any { it.id == usage.id }) {
                demoData.luckyWheelUsage.map {
                    if (it.id == usage.id) usage else it
                }
            } else {
                demoData.luckyWheelUsage + usage
            }
        )
    }

    override suspend fun saveGameHighscore(highscore: GameHighscore) {
        demoData = demoData.copy(
            gameHighscores = if (demoData.gameHighscores.any { it.id == highscore.id }) {
                demoData.gameHighscores.map {
                    if (it.id == highscore.id) highscore else it
                }
            } else {
                demoData.gameHighscores + highscore
            }
        )
    }

    override suspend fun saveGameDailyReward(reward: GameDailyReward) {
        demoData = demoData.copy(
            gameDailyRewards = if (demoData.gameDailyRewards.any { it.id == reward.id }) {
                demoData.gameDailyRewards.map {
                    if (it.id == reward.id) reward else it
                }
            } else {
                demoData.gameDailyRewards + reward
            }
        )
    }

    override suspend fun saveGameSettings(settings: GameSettings) {
        demoData = demoData.copy(
            gameSettings = settings
        )
    }

    override suspend fun updateChildInventory(
        childId: String,
        inventory: List<LunaInventoryItem>,
        equippedItem: LunaInventoryItem?,
        profileImageItem: LunaInventoryItem?,
        hasProfileImage: Boolean
    ) {
        demoData = demoData.copy(
            children = demoData.children.map { child ->
                if (child.id == childId) {
                    child.copy(
                        inventory = inventory,
                        equippedItem = equippedItem,
                        profileImageItem = profileImageItem,
                        hasProfileImage = hasProfileImage
                    )
                } else {
                    child
                }
            }
        )
    }

    override suspend fun updateChildProfile(
        childId: String,
        name: String,
        role: UserRole,
        password: String,
        age: Int,
        passwordRequired: Boolean,
        allowRememberLogin: Boolean,
        isBuiltInAdmin: Boolean
    ) {
        demoData = demoData.copy(
            children = demoData.children.map { child ->
                if (child.id == childId) {
                    child.copy(
                        name = name,
                        role = role,
                        password = password,
                        age = age,
                        passwordRequired = passwordRequired,
                        allowRememberLogin = allowRememberLogin,
                        isBuiltInAdmin = isBuiltInAdmin
                    )
                } else {
                    child
                }
            }
        )
    }

    override suspend fun deleteChild(childId: String) {
        demoData = demoData.copy(
            children = demoData.children.filterNot { it.id == childId }
        )
    }

    override suspend fun deleteTask(taskId: String) {
        demoData = demoData.copy(
            tasks = demoData.tasks.filterNot { it.id == taskId }
        )
    }

    override suspend fun deleteShopItem(shopItemId: String) {
        demoData = demoData.copy(
            shopItems = demoData.shopItems.filterNot { it.id == shopItemId }
        )
    }

    override suspend fun deleteDogScheduleItem(itemId: String) {
        demoData = demoData.copy(
            dogSchedule = demoData.dogSchedule.filterNot { it.id == itemId }
        )
    }

    override suspend fun deleteDogPlanTemplate(templateId: String) {
        val currentDogPlan = demoData.dogPlan

        demoData = demoData.copy(
            dogPlan = currentDogPlan.copy(
                templates = currentDogPlan.templates.filterNot { it.id == templateId }
            )
        )
    }

    override suspend fun deleteDogPlanCompletion(completionId: String) {
        val currentDogPlan = demoData.dogPlan

        demoData = demoData.copy(
            dogPlan = currentDogPlan.copy(
                completions = currentDogPlan.completions.filterNot { it.id == completionId }
            )
        )
    }

    override suspend fun deleteDogPlanShift(shiftId: String) {
        val currentDogPlan = demoData.dogPlan

        demoData = demoData.copy(
            dogPlan = currentDogPlan.copy(
                shifts = currentDogPlan.shifts.filterNot { it.id == shiftId }
            )
        )
    }

    override suspend fun deleteLog(logId: String) {
        demoData = demoData.copy(
            logs = demoData.logs.filterNot { it.id == logId }
        )
    }

    override suspend fun deleteLuckyWheelUsage(usageId: String) {
        demoData = demoData.copy(
            luckyWheelUsage = demoData.luckyWheelUsage.filterNot { it.id == usageId }
        )
    }

    override suspend fun deleteGameHighscore(highscoreId: String) {
        demoData = demoData.copy(
            gameHighscores = demoData.gameHighscores.filterNot { it.id == highscoreId }
        )
    }

    override suspend fun deleteGameDailyReward(rewardId: String) {
        demoData = demoData.copy(
            gameDailyRewards = demoData.gameDailyRewards.filterNot { it.id == rewardId }
        )
    }

    override suspend fun deleteGameHighscoresByGame(game: LunaGameType) {
        demoData = demoData.copy(
            gameHighscores = demoData.gameHighscores.filterNot { highscore ->
                highscore.game == game
            }
        )
    }

    override suspend fun deleteAllGameHighscores() {
        demoData = demoData.copy(
            gameHighscores = emptyList()
        )
    }

    override suspend fun setChildCoins(
        childId: String,
        coins: Int
    ): Int {
        demoData = demoData.copy(
            children = demoData.children.map { child ->
                if (child.id == childId) {
                    child.copy(coins = coins)
                } else {
                    child
                }
            }
        )

        return coins
    }

    override suspend fun changeChildCoins(
        childId: String,
        coinDelta: Int
    ): Int {
        val child = demoData.children.firstOrNull { it.id == childId }
        val newCoins = (child?.coins ?: 0) + coinDelta

        demoData = demoData.copy(
            children = demoData.children.map {
                if (it.id == childId) {
                    it.copy(coins = newCoins)
                } else {
                    it
                }
            }
        )

        return newCoins
    }
    override suspend fun setChildSilver(childId:String,silver:Long):Long { demoData=demoData.copy(children=demoData.children.map{if(it.id==childId)it.copy(silver=silver)else it});return silver }
    override suspend fun changeChildSilver(childId:String,silverDelta:Long):Long { val old=demoData.children.firstOrNull{it.id==childId}?.silver?:0L;val value=old+silverDelta;demoData=demoData.copy(children=demoData.children.map{if(it.id==childId)it.copy(silver=value)else it});return value }
    override suspend fun convertCoinsToSilver(childId:String,coinAmount:Int,log:LogEntry):Pair<Int,Long>{val c=demoData.children.first{it.id==childId};val nc=c.coins-coinAmount;val ns=c.silver+coinAmount*100L;demoData=demoData.copy(children=demoData.children.map{if(it.id==childId)it.copy(coins=nc,silver=ns)else it},logs=listOf(log)+demoData.logs);return nc to ns}
    override suspend fun transferCurrency(senderId:String,recipientId:String,amount:Long,currency:de.meson_labs.luna_coin.models.CurrencyType,senderLog:LogEntry,recipientLog:LogEntry):Pair<Long,Long>{val s=demoData.children.first{it.id==senderId};val r=demoData.children.first{it.id==recipientId};val sb=if(currency==de.meson_labs.luna_coin.models.CurrencyType.LUNA_COIN)s.coins.toLong()else s.silver;val rb=if(currency==de.meson_labs.luna_coin.models.CurrencyType.LUNA_COIN)r.coins.toLong()else r.silver;val ns=sb-amount;val nr=rb+amount;demoData=demoData.copy(children=demoData.children.map{when(it.id){senderId->if(currency==de.meson_labs.luna_coin.models.CurrencyType.LUNA_COIN)it.copy(coins=ns.toInt())else it.copy(silver=ns);recipientId->if(currency==de.meson_labs.luna_coin.models.CurrencyType.LUNA_COIN)it.copy(coins=nr.toInt())else it.copy(silver=nr);else->it}},logs=listOf(senderLog,recipientLog)+demoData.logs);return ns to nr}

}
