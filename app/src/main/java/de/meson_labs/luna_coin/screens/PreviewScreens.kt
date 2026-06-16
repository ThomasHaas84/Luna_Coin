// screens/PreviewScreens.kt
package de.meson_labs.luna_coin.screens

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import de.meson_labs.luna_coin.data.DemoData
import de.meson_labs.luna_coin.data.repository.DataRepository
import de.meson_labs.luna_coin.models.Child
import de.meson_labs.luna_coin.models.DogScheduleItem
import de.meson_labs.luna_coin.models.GameHighscore
import de.meson_labs.luna_coin.models.LogEntry
import de.meson_labs.luna_coin.models.LuckyWheelUsage
import de.meson_labs.luna_coin.models.LunaCoinData
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

        onAddShopItem = { _, _, _ -> },
        onUpdateShopItem = { _, _, _, _ -> },
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
        child.name.equals("Thomas", ignoreCase = true)
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

        onAddShopItem = { _, _, _ -> },
        onUpdateShopItem = { _, _, _, _ -> },
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
    private val demoData: LunaCoinData
) : DataRepository {

    override suspend fun loadData(): LunaCoinData? = demoData

    override suspend fun saveData(data: LunaCoinData) {}

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

    override suspend fun loadLogs(limit: Long): List<LogEntry> {
        return demoData.logs
    }

    override suspend fun loadLuckyWheelUsage(): List<LuckyWheelUsage> {
        return demoData.luckyWheelUsage
    }

    override suspend fun loadGameHighscores(): List<GameHighscore> {
        return demoData.gameHighscores
    }

    override suspend fun saveChild(child: Child) {}

    override suspend fun saveTask(task: TaskItem) {}

    override suspend fun saveShopItem(shopItem: ShopItem) {}

    override suspend fun saveDogScheduleItem(item: DogScheduleItem) {}

    override suspend fun saveLog(log: LogEntry) {}

    override suspend fun saveLuckyWheelUsage(usage: LuckyWheelUsage) {}

    override suspend fun saveGameHighscore(highscore: GameHighscore) {}

    override suspend fun updateChildInventory(
        childId: String,
        inventory: List<LunaInventoryItem>,
        equippedItem: LunaInventoryItem?,
        profileImageItem: LunaInventoryItem?,
        hasProfileImage: Boolean
    ) {}

    override suspend fun updateChildProfile(
        childId: String,
        name: String,
        role: UserRole,
        password: String,
        age: Int
    ) {}

    override suspend fun deleteChild(childId: String) {}

    override suspend fun deleteTask(taskId: String) {}

    override suspend fun deleteShopItem(shopItemId: String) {}

    override suspend fun deleteDogScheduleItem(itemId: String) {}

    override suspend fun deleteLog(logId: String) {}

    override suspend fun deleteLuckyWheelUsage(usageId: String) {}

    override suspend fun deleteGameHighscore(highscoreId: String) {}

    override suspend fun setChildCoins(
        childId: String,
        coins: Int
    ): Int {
        return coins
    }

    override suspend fun changeChildCoins(
        childId: String,
        coinDelta: Int
    ): Int {
        val child = demoData.children.firstOrNull { it.id == childId }
        return (child?.coins ?: 0) + coinDelta
    }
}