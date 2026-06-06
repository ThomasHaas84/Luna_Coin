package de.meson_labs.luna_coin.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
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
import de.meson_labs.luna_coin.screens.settings.SettingsScreen
import de.meson_labs.luna_coin.viewmodel.LunaCoinViewModel
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.SportsEsports

@Composable
fun MainScreen(
    viewModel: LunaCoinViewModel
) {
    val data by viewModel.data.collectAsState()
    val selectedChildId by viewModel.selectedChildId.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()

    Surface {
        if (selectedChildId == null) {
            UserSelectionScreen(
                children = data.children,
                onChildSelected = viewModel::selectChild
            )
        } else {
            val selectedChild = data.children.firstOrNull { child ->
                child.id == selectedChildId
            }

            var selectedTab by remember {
                mutableIntStateOf(0)
            }

            Scaffold(
                bottomBar = {
                    NavigationBar {
                        NavigationBarItem(
                            selected = selectedTab == 0,
                            onClick = {
                                selectedTab = 0
                            },
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Aufgaben"
                                )
                            },
                            label = {
                                Text("Aufgaben")
                            }
                        )

                        NavigationBarItem(
                            selected = selectedTab == 1,
                            onClick = {
                                selectedTab = 1
                            },
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.ShoppingCart,
                                    contentDescription = "Shop"
                                )
                            },
                            label = {
                                Text("Shop")
                            }
                        )

                        NavigationBarItem(
                            selected = selectedTab == 2,
                            onClick = {
                                selectedTab = 2
                            },
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.SportsEsports,
                                    contentDescription = "Luna-Games"
                                )
                            },
                            label = {
                                Text("Luna-Games")
                            }
                        )

                        NavigationBarItem(
                            selected = selectedTab == 3,
                            onClick = {
                                selectedTab = 3
                            },
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.Pets,
                                    contentDescription = "LunaME"
                                )
                            },
                            label = {
                                Text("LunaME")
                            }
                        )

                        NavigationBarItem(
                            selected = selectedTab == 4,
                            onClick = {
                                selectedTab = 4
                            },
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Einstellungen"
                                )
                            },
                            label = {
                                Text("Einstellungen")
                            }
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
                        onLuckyWheelResult = viewModel::applyLuckyWheelResult,
                        onLogout = viewModel::logout
                    )

                    2 -> Text(
                        text = "Luna-Games",
                        modifier = Modifier.padding(innerPadding)
                    )

                    3 -> Text(
                        text = "LunaME",
                        modifier = Modifier.padding(innerPadding)
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

                        onUndoLogEntry = viewModel::undoLogEntry,

                        onResetDemoData = viewModel::resetDemoData,
                        onLogout = viewModel::logout
                    )
                }
            }
        }
    }
}