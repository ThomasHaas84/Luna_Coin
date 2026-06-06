package de.meson_labs.luna_coin.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.meson_labs.luna_coin.components.CoinDisplay
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

                    2 -> EmptyLunaTabScreen(
                        modifier = Modifier.padding(innerPadding),
                        title = "Luna-Games",
                        selectedChild = selectedChild,
                        onLogout = viewModel::logout
                    )

                    3 -> EmptyLunaTabScreen(
                        modifier = Modifier.padding(innerPadding),
                        title = "LunaME",
                        selectedChild = selectedChild,
                        onLogout = viewModel::logout
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

@Composable
private fun EmptyLunaTabScreen(
    modifier: Modifier = Modifier,
    title: String,
    selectedChild: Child?,
    onLogout: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        LunaHeaderRow(
            title = title,
            selectedChild = selectedChild,
            onLogout = onLogout
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Coming soon...",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun LunaHeaderRow(
    title: String,
    selectedChild: Child?,
    onLogout: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.displaySmall
            )

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${selectedChild?.name ?: ""}  ",
                    style = MaterialTheme.typography.headlineSmall
                )

                CoinDisplay(
                    amount = selectedChild?.coins ?: 0
                )
            }
        }

        OutlinedButton(
            onClick = onLogout
        ) {
            Text("Benutzer wechseln")
        }
    }
}