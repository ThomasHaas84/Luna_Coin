// screens/MainScreen.kt
package de.meson_labs.luna_coin.screens

import android.content.res.Configuration
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import de.meson_labs.luna_coin.models.Child
import de.meson_labs.luna_coin.screens.settings.SettingsScreen
import de.meson_labs.luna_coin.viewmodel.LunaCoinViewModel

private data class LunaBottomNavItem(
    val title: String,
    val compactTitle: String,
    val icon: ImageVector
)

@Composable
fun MainScreen(
    viewModel: LunaCoinViewModel
) {
    val data by viewModel.data.collectAsState()
    val selectedChildId by viewModel.selectedChildId.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()

    val view = LocalView.current
    val configuration = LocalConfiguration.current

    val screenWidthDp = configuration.screenWidthDp
    val isPortrait = configuration.orientation == Configuration.ORIENTATION_PORTRAIT
    val isPhone = screenWidthDp < 600
    val isCompactPhone = screenWidthDp < 420

    val bottomNavFontSize: TextUnit = when {
        isPhone && isPortrait && screenWidthDp < 360 -> 7.sp
        isPhone && isPortrait && screenWidthDp < 390 -> 8.sp
        isPhone && isPortrait -> 9.sp
        isPhone -> 10.sp
        else -> 12.sp
    }

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

            var selectedTab by remember {
                mutableIntStateOf(0)
            }

            fun selectTab(newTab: Int) {
                if (selectedTab != newTab) {
                    view.playSoundEffect(SoundEffectConstants.CLICK)
                    selectedTab = newTab
                }
            }

            val navItems = listOf(
                LunaBottomNavItem(
                    title = "Aufgaben",
                    compactTitle = "Aufgaben",
                    icon = Icons.Default.CheckCircle
                ),
                LunaBottomNavItem(
                    title = "Shop",
                    compactTitle = "Shop",
                    icon = Icons.Default.ShoppingCart
                ),
                LunaBottomNavItem(
                    title = "Luna-Games",
                    compactTitle = "Games",
                    icon = Icons.Default.SportsEsports
                ),
                LunaBottomNavItem(
                    title = "LunaME",
                    compactTitle = "LunaME",
                    icon = Icons.Default.Pets
                ),
                LunaBottomNavItem(
                    title = "Einstellungen",
                    compactTitle = "Einstellung",
                    icon = Icons.Default.Settings
                )
            )

            Scaffold(
                bottomBar = {
                    NavigationBar {
                        navItems.forEachIndexed { index, item ->
                            NavigationBarItem(
                                selected = selectedTab == index,
                                onClick = {
                                    selectTab(index)
                                },
                                icon = {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = item.title
                                    )
                                },
                                label = {
                                    Text(
                                        text = if (isCompactPhone) {
                                            item.compactTitle
                                        } else {
                                            item.title
                                        },
                                        maxLines = 1,
                                        softWrap = false,
                                        overflow = TextOverflow.Visible,
                                        fontSize = bottomNavFontSize
                                    )
                                },
                                alwaysShowLabel = true
                            )
                        }
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
                        onLogout = viewModel::logout,
                        canGoToPreviousDay = viewModel.canGoToPreviousDay,
                        canGoToNextDay = viewModel.canGoToNextDay
                    )

                    1 -> ShopScreen(
                        modifier = Modifier.padding(innerPadding),
                        data = data,
                        selectedChild = selectedChild,
                        onBuyItem = viewModel::buyShopItem,
                        onLuckyWheelResult = { childId, costCoins, result ->
                            viewModel.applyLuckyWheelResult(
                                childId = childId,
                                costCoins = costCoins,
                                result = result
                            )
                        },
                        onLogout = viewModel::logout
                    )

                    2 -> LunaGamesScreen(
                        modifier = Modifier.padding(innerPadding),
                        selectedChild = selectedChild,
                        viewModel = viewModel,
                        onLogout = viewModel::logout
                    )

                    3 -> LunaMeScreen(
                        modifier = Modifier.padding(innerPadding),
                        data = data,
                        selectedChild = selectedChild,
                        onBuyItem = viewModel::buyLunaMeItem,
                        onLogout = viewModel::logout,
                        onChildChanged = viewModel::updateChild
                    )

                    4 -> SettingsScreen(
                        modifier = Modifier.padding(innerPadding),
                        data = data,
                        selectedChild = selectedChild,
                        selectedDate = selectedDate,
                        jsonText = "",
                        viewModel = viewModel,
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
                            viewModel.updateChildCoins(
                                childId = childId,
                                newCoins = newCoins,
                                comment = comment
                            )
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