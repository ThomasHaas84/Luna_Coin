package de.meson_labs.luna_coin.lunarim

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import de.meson_labs.luna_coin.models.Child

@Composable
fun LunarimMainScreen(
    modifier: Modifier = Modifier,
    selectedChild: Child?,
    onExit: () -> Unit,
    onSave: () -> Unit = {}
) {
    var currentDestination by remember {
        mutableStateOf(LunarimDestination.CAMP)
    }

    var showExitDialog by remember {
        mutableStateOf(false)
    }

    fun requestExit() {
        showExitDialog = true
    }

    fun saveAndExit() {
        onSave()
        showExitDialog = false
        onExit()
    }

    BackHandler {
        requestExit()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            LunarimBottomBar(
                currentDestination = currentDestination,
                onDestinationSelected = { destination ->
                    currentDestination = destination
                }
            )
        }
    ) { innerPadding ->
        val contentModifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)

        when (currentDestination) {
            LunarimDestination.CAMP -> {
                LunarimCampScreen(
                    modifier = contentModifier,
                    selectedChild = selectedChild,
                    onExit = ::requestExit
                )
            }

            LunarimDestination.CHARACTER -> {
                LunarimCharacterScreen(
                    modifier = contentModifier,
                    selectedChild = selectedChild
                )
            }

            LunarimDestination.SHOP -> {
                LunarimShopScreen(
                    modifier = contentModifier,
                    selectedChild = selectedChild
                )
            }

            LunarimDestination.MAP -> {
                LunarimMapScreen(
                    modifier = contentModifier,
                    selectedChild = selectedChild
                )
            }
        }
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = {
                showExitDialog = false
            },
            title = {
                Text("Lunarim verlassen?")
            },
            text = {
                Text(
                    "Möchtest du Lunarim wirklich verlassen? " +
                            "Dein aktueller Spielstand wird vorher gespeichert."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = ::saveAndExit
                ) {
                    Text("Speichern und verlassen")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showExitDialog = false
                    }
                ) {
                    Text("Weiterspielen")
                }
            }
        )
    }
}
