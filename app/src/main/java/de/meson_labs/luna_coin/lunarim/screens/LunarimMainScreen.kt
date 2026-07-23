package de.meson_labs.luna_coin.lunarim.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.meson_labs.luna_coin.models.Child
import kotlinx.coroutines.delay

private const val RESET_COUNTDOWN_SECONDS = 5

@Composable
fun LunarimMainScreen(
    modifier: Modifier = Modifier,
    selectedChild: Child?,
    onExit: () -> Unit,
    onSave: () -> Unit = {},
    onCreateCharacter: () -> Unit = {},
    onReset: () -> Unit = {},
    hasExistingGame: Boolean = false
) {
    var currentDestination by rememberSaveable {
        mutableStateOf(LunarimDestination.CAMP)
    }

    /*
     * Dieser lokale Zustand sorgt dafür, dass die Oberfläche direkt reagiert,
     * nachdem ein Charakter erstellt oder zurückgesetzt wurde.
     *
     * Später sollte hasExistingGame aus ViewModel/Repository kommen.
     */
    var sessionHasGame by rememberSaveable(hasExistingGame) {
        mutableStateOf(hasExistingGame)
    }

    var showStartDialog by rememberSaveable {
        mutableStateOf(true)
    }

    var showExitDialog by rememberSaveable {
        mutableStateOf(false)
    }

    var showResetDialog by rememberSaveable {
        mutableStateOf(false)
    }

    var showFinalResetDialog by rememberSaveable {
        mutableStateOf(false)
    }

    var resetCountdown by remember {
        mutableIntStateOf(RESET_COUNTDOWN_SECONDS)
    }

    var resetButtonEnabled by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(showResetDialog) {
        if (!showResetDialog) {
            resetCountdown = RESET_COUNTDOWN_SECONDS
            resetButtonEnabled = false
            return@LaunchedEffect
        }

        resetCountdown = RESET_COUNTDOWN_SECONDS
        resetButtonEnabled = false

        while (resetCountdown > 0 && showResetDialog) {
            delay(1_000L)
            resetCountdown--
        }

        if (showResetDialog) {
            resetButtonEnabled = true
        }
    }

    fun requestExit() {
        showStartDialog = false
        showExitDialog = true
    }

    fun saveAndExit() {
        if (sessionHasGame) {
            onSave()
        }

        showExitDialog = false
        onExit()
    }

    fun createCharacter() {
        onCreateCharacter()
        sessionHasGame = true
        showStartDialog = false
        currentDestination = LunarimDestination.CHARACTER
    }

    fun continueGame() {
        showStartDialog = false
        currentDestination = LunarimDestination.CAMP
    }

    fun requestReset() {
        showStartDialog = false
        showResetDialog = true
    }

    fun resetLunarimFinally() {
        onReset()

        sessionHasGame = false
        currentDestination = LunarimDestination.CAMP

        showFinalResetDialog = false
        showResetDialog = false
        showStartDialog = true
    }

    BackHandler {
        when {
            showFinalResetDialog -> {
                showFinalResetDialog = false
                showResetDialog = true
            }

            showResetDialog -> {
                showResetDialog = false
                showStartDialog = true
            }

            showExitDialog -> {
                showExitDialog = false
                showStartDialog = true
            }

            showStartDialog -> {
                requestExit()
            }

            else -> {
                requestExit()
            }
        }
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
                    selectedChild = selectedChild
                )
            }

            LunarimDestination.CHARACTER -> {
                /*
                 * Die drei Callbacks bleiben vorerst erhalten, damit dein
                 * aktueller LunarimCharacterScreen weiter kompiliert.
                 *
                 * Wenn das Drei-Punkte-Menü später dort entfernt wird,
                 * können diese Parameter ebenfalls entfernt werden.
                 */
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

    if (showStartDialog) {
        AlertDialog(
            modifier = Modifier.widthIn(max = 480.dp),
            onDismissRequest = {
                // Das Startmenü soll nicht durch Tippen außerhalb verschwinden.
            },
            title = {
                Text(
                    if (sessionHasGame) {
                        "Lunarim"
                    } else {
                        "Willkommen in Lunarim"
                    }
                )
            },
            text = {
                Column {
                    Text(
                        if (sessionHasGame) {
                            "Für ${selectedChild?.name ?: "diesen Benutzer"} ist bereits ein Lunarim-Spielstand vorhanden."
                        } else {
                            "Für ${selectedChild?.name ?: "diesen Benutzer"} wurde noch kein Lunarim-Charakter erstellt."
                        }
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp)
                    )

                    Text(
                        if (sessionHasGame) {
                            "Du kannst das Spiel fortsetzen, den Lunarim-Charakter zurücksetzen oder Lunarim verlassen."
                        } else {
                            "Erstelle jetzt einen Charakter oder verlasse Lunarim."
                        }
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (sessionHasGame) {
                            continueGame()
                        } else {
                            createCharacter()
                        }
                    }
                ) {
                    Text(
                        if (sessionHasGame) {
                            "Spiel fortsetzen"
                        } else {
                            "Charakter erstellen"
                        }
                    )
                }
            },
            dismissButton = {
                Column {
                    if (sessionHasGame) {
                        TextButton(
                            onClick = ::requestReset
                        ) {
                            Text("Charakter zurücksetzen")
                        }
                    }

                    TextButton(
                        onClick = ::requestExit
                    ) {
                        Text("Spiel verlassen")
                    }
                }
            }
        )
    }

    if (showResetDialog) {
        AlertDialog(
            modifier = Modifier.widthIn(max = 480.dp),
            onDismissRequest = {
                showResetDialog = false
                showStartDialog = true
            },
            title = {
                Text("Lunarim-Charakter zurücksetzen?")
            },
            text = {
                Text(
                    "Der gesamte Lunarim-Spielstand dieses Charakters wird gelöscht.\n\n" +
                            "Luna Coins, Luna Silver, Luna-Level und LunaME-Skills bleiben erhalten.\n\n" +
                            "Der Vorgang kann nach der endgültigen Bestätigung nicht rückgängig gemacht werden."
                )
            },
            confirmButton = {
                TextButton(
                    enabled = resetButtonEnabled,
                    onClick = {
                        showResetDialog = false
                        showFinalResetDialog = true
                    }
                ) {
                    Text(
                        if (resetButtonEnabled) {
                            "Zurücksetzen"
                        } else {
                            "Zurücksetzen ($resetCountdown)"
                        }
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showResetDialog = false
                        showStartDialog = true
                    }
                ) {
                    Text("Abbrechen")
                }
            }
        )
    }

    if (showFinalResetDialog) {
        AlertDialog(
            modifier = Modifier.widthIn(max = 480.dp),
            onDismissRequest = {
                showFinalResetDialog = false
                showStartDialog = true
            },
            title = {
                Text("Wirklich endgültig zurücksetzen?")
            },
            text = {
                Text(
                    "Dieser Vorgang kann nicht rückgängig gemacht werden.\n\n" +
                            "Der komplette Lunarim-Spielstand von " +
                            "${selectedChild?.name ?: "diesem Benutzer"} wird gelöscht."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = ::resetLunarimFinally
                ) {
                    Text("Ja, endgültig zurücksetzen")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showFinalResetDialog = false
                        showStartDialog = true
                    }
                ) {
                    Text("Abbrechen")
                }
            }
        )
    }

    if (showExitDialog) {
        AlertDialog(
            modifier = Modifier.widthIn(max = 480.dp),
            onDismissRequest = {
                showExitDialog = false
                showStartDialog = true
            },
            title = {
                Text("Lunarim verlassen?")
            },
            text = {
                Text(
                    if (sessionHasGame) {
                        "Möchtest du Lunarim wirklich verlassen? Dein aktueller Spielstand wird vorher gespeichert."
                    } else {
                        "Möchtest du Lunarim wirklich verlassen?"
                    }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = ::saveAndExit
                ) {
                    Text(
                        if (sessionHasGame) {
                            "Speichern und verlassen"
                        } else {
                            "Spiel verlassen"
                        }
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showExitDialog = false
                        showStartDialog = true
                    }
                ) {
                    Text("Zurück")
                }
            }
        )
    }
}
