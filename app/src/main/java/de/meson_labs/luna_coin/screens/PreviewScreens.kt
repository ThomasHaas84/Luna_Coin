package de.meson_labs.luna_coin.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import de.meson_labs.luna_coin.data.DemoData
import de.meson_labs.luna_coin.screens.settings.SettingsScreen
import java.time.LocalDate

@Preview(
    name = "User Selection",
    showBackground = true,
    widthDp = 1280,
    heightDp = 800
)
@Composable
fun UserSelectionScreenPreview() {
    val demoData = DemoData.create()

    UserSelectionScreen(
        children = demoData.children,
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
        onLogout = {}
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
        onLuckyWheelResult = { _, _, result ->
            result
        },
        onLogout = {}
    )
}

@Preview(
    name = "Settings Screen - Child",
    showBackground = true,
    widthDp = 1280,
    heightDp = 800
)
@Composable
fun SettingsScreenChildPreview() {
    val demoData = DemoData.create()

    SettingsScreen(
        data = demoData,
        selectedChild = demoData.children.firstOrNull(),
        selectedDate = LocalDate.now(),
        jsonText = "",

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
        onSaveBackup = {},
        onLoadBackup = {},

        onLogout = {}
    )
}

@Preview(
    name = "Settings Screen - Admin",
    showBackground = true,
    widthDp = 1280,
    heightDp = 800
)
@Composable
fun SettingsScreenAdminPreview() {
    val demoData = DemoData.create()

    val admin = demoData.children.firstOrNull { child ->
        child.name == "Thomas"
    }

    SettingsScreen(
        data = demoData,
        selectedChild = admin,
        selectedDate = LocalDate.now(),
        jsonText = "",

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
        onSaveBackup = {},
        onLoadBackup = {},

        onLogout = {}
    )
}