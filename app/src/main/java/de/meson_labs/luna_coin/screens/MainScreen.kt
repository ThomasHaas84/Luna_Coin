package de.meson_labs.luna_coin.screens

import android.view.SoundEffectConstants
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import de.meson_labs.luna_coin.models.Child
import de.meson_labs.luna_coin.screens.settings.SettingsScreen
import de.meson_labs.luna_coin.viewmodel.LunaCoinViewModel

@Composable
fun MainScreen(
    viewModel: LunaCoinViewModel
) {
    val data by viewModel.data.collectAsState()
    val selectedChildId by viewModel.selectedChildId.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()

    val view = LocalView.current

    Surface {
        if (selectedChildId == null) {
            UserSelectionScreen(
                viewModel = viewModel,
                onChildSelected = viewModel::selectChild
            )
        } else {
            val selectedChild: Child? = data.children.firstOrNull { child ->
                child.id == selectedChildId
            }

            var selectedTab by remember { mutableIntStateOf(0) }

            fun selectTab(newTab: Int) {
                if (selectedTab != newTab) {
                    view.playSoundEffect(SoundEffectConstants.CLICK)
                    selectedTab = newTab
                }
            }

            Scaffold(
                bottomBar = {
                    NavigationBar {
                        NavigationBarItem(
                            selected = selectedTab == 0,
                            onClick = { selectTab(0) },
                            icon = { Icon(Icons.Default.CheckCircle, contentDescription = "Aufgaben") },
                            label = { Text("Aufgaben") }
                        )
                        NavigationBarItem(
                            selected = selectedTab == 1,
                            onClick = { selectTab(1) },
                            icon = { Icon(Icons.Default.ShoppingCart, contentDescription = "Shop") },
                            label = { Text("Shop") }
                        )
                        NavigationBarItem(
                            selected = selectedTab == 2,
                            onClick = { selectTab(2) },
                            icon = { Icon(Icons.Default.SportsEsports, contentDescription = "Luna-Games") },
                            label = { Text("Luna-Games") }
                        )
                        NavigationBarItem(
                            selected = selectedTab == 3,
                            onClick = { selectTab(3) },
                            icon = { Icon(Icons.Default.Pets, contentDescription = "LunaME") },
                            label = { Text("LunaME") }
                        )
                        NavigationBarItem(
                            selected = selectedTab == 4,
                            onClick = { selectTab(4) },
                            icon = { Icon(Icons.Default.Settings, contentDescription = "Einstellungen") },
                            label = { Text("Einstellungen") }
                        )
                    }
                }
            ) { innerPadding ->
                when (selectedTab) {
                    0 -> TasksScreen(
                        modifier = Modifier.padding(innerPadding),
                        data = data,
                        selectedChild = selectedChild,
                        selectedDate = selectedDate,
                        onPreviousDay = viewModel::previousDay,
                        onNextDay = viewModel::nextDay,
                        onToday = viewModel::today,
                        onCompleteTask = viewModel::completeTask,
                        onLogout = viewModel::logout
                    )

                    1 -> ShopScreen(
                        modifier = Modifier.padding(innerPadding),
                        data = data,
                        selectedChild = selectedChild,
                        onBuyItem = viewModel::buyShopItem,
                        onLuckyWheelResult = { childId, costCoins, result ->
                            viewModel.applyLuckyWheelResult(childId, costCoins, result)
                        },
                        onLogout = viewModel::logout
                    )

                    2 -> LunaGamesScreen(
                        modifier = Modifier.padding(innerPadding),
                        selectedChild = selectedChild,
                        onLogout = viewModel::logout
                    )

                    3 -> LunaMeScreen(
                        modifier = Modifier.padding(innerPadding),
                        selectedChild = selectedChild,
                        onLogout = viewModel::logout,
                        onChildChanged = viewModel::updateChild
                    )

                    4 -> SettingsScreen(
                        modifier = Modifier.padding(innerPadding),
                        data = data,
                        selectedChild = selectedChild,
                        selectedDate = selectedDate,
                        jsonText = viewModel.getJsonText(),

                        onAddTask = viewModel::addTask,
                        onUpdateTask = viewModel::updateTask,
                        onDeleteTask = viewModel::deleteTask,

                        onAddShopItem = viewModel::addShopItem,
                        onUpdateShopItem = viewModel::updateShopItem,
                        onDeleteShopItem = viewModel::deleteShopItem,

                        onAddDogSchedule = viewModel::addDogSchedule,
                        onUpdateDogSchedule = viewModel::updateDogSchedule,
                        onDeleteDogSchedule = viewModel::deleteDogSchedule,

                        onUpdateChildCoins = { childId, newCoins, comment ->
                            viewModel.updateChildCoins(childId, newCoins, comment)
                        },

                        onUndoLogEntry = viewModel::undoLogEntry,

                        onResetDemoData = viewModel::resetDemoData,
                        onCreateCloudBackup = viewModel::createCloudBackup,
                        onRestoreFromBackup = viewModel::restoreFromBackup,
                        onImportFromJson = viewModel::importFromJson,

                        onLogout = viewModel::logout
                    )
                }
            }
        }
    }
}