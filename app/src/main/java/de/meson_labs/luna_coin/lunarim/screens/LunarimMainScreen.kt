package de.meson_labs.luna_coin.lunarim.screens

import android.media.MediaPlayer
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import de.meson_labs.luna_coin.R
import de.meson_labs.luna_coin.lunarim.data.LunarimGameStorage
import de.meson_labs.luna_coin.lunarim.models.LunarimGameState
import de.meson_labs.luna_coin.models.Child
import kotlinx.coroutines.delay

private const val RESET_COUNTDOWN_SECONDS = 5

@Composable
fun LunarimMainScreen(
    modifier: Modifier = Modifier,
    selectedChild: Child?,
    onExit: () -> Unit,
    onSave: () -> Unit = {}
) {
    val context = LocalContext.current
    val storage = remember(context) {
        LunarimGameStorage(context)
    }

    /*
     * Im aktuellen LunaGamesScreen wird bereits der echte ausgewählte
     * Child-Datensatz übergeben. Für den Spielstand wird ausschließlich
     * dessen stabile ID verwendet.
     */
    val childId = selectedChild?.id.orEmpty()

    var gameState by remember(childId) {
        mutableStateOf(
            if (childId.isBlank()) {
                null
            } else {
                storage.load(childId)
            }
        )
    }

    var showStartDialog by rememberSaveable(childId) {
        mutableStateOf(true)
    }

    var currentDestination by rememberSaveable(childId) {
        mutableStateOf(LunarimDestination.CAMP)
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

    val hasSavedCharacter = gameState?.hasStarted == true

    /*
     * Die Menü-Musik läuft ausschließlich auf dem Lunarim-Hauptbildschirm.
     * Sobald das Spiel begonnen/fortgesetzt oder Lunarim verlassen wird,
     * wird der MediaPlayer automatisch beendet und freigegeben.
     */
    DisposableEffect(showStartDialog) {
        val menuMusicPlayer = if (showStartDialog) {
            MediaPlayer.create(context, R.raw.lunarim)?.apply {
                isLooping = true
                setVolume(1f, 1f)
                start()
            }
        } else {
            null
        }

        onDispose {
            runCatching {
                menuMusicPlayer?.stop()
            }
            menuMusicPlayer?.release()
        }
    }

    LaunchedEffect(showResetDialog) {
        if (!showResetDialog) {
            resetCountdown = RESET_COUNTDOWN_SECONDS
            return@LaunchedEffect
        }

        resetCountdown = RESET_COUNTDOWN_SECONDS

        while (resetCountdown > 0 && showResetDialog) {
            delay(1_000L)
            resetCountdown--
        }
    }

    fun createCharacter() {
        if (childId.isBlank()) return

        /*
         * createNewGame() speichert synchron mit commit().
         * Erst danach wird die Oberfläche geöffnet.
         */
        val createdGame = storage.createNewGame(childId)
        gameState = createdGame

        currentDestination = LunarimDestination.CHARACTER
        showStartDialog = false
    }

    fun continueGame() {
        if (!hasSavedCharacter) return

        /*
         * Beim Fortsetzen immer im Lager starten.
         */
        currentDestination = LunarimDestination.CAMP
        showStartDialog = false
    }

    fun saveCurrentGame() {
        gameState?.let(storage::save)
        onSave()
    }

    fun saveAndExit() {
        saveCurrentGame()
        showExitDialog = false
        onExit()
    }

    fun deleteCharacter() {
        storage.delete(childId)
        gameState = null

        currentDestination = LunarimDestination.CAMP
        showFinalResetDialog = false
        showResetDialog = false
        showStartDialog = true
    }

    fun requestExit() {
        showExitDialog = true
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
            }

            showStartDialog -> {
                showExitDialog = true
            }

            else -> {
                requestExit()
            }
        }
    }

    /*
     * Der Lunarim-Startbildschirm ist ein eigener schwarzer Fullscreen-Screen.
     * Spielinhalt und BottomBar werden dahinter nicht aufgebaut.
     */
    if (showStartDialog) {
        LunarimStartScreen(
            modifier = modifier,
            hasSavedCharacter = hasSavedCharacter,
            canCreateCharacter = childId.isNotBlank(),
            onCreateCharacter = ::createCharacter,
            onContinueGame = ::continueGame,
            onResetCharacter = {
                showResetDialog = true
            },
            onExit = {
                showExitDialog = true
            }
        )
    } else {
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
                    LunarimCharacterScreen(
                        modifier = contentModifier,
                        selectedChild = selectedChild,
                        gameState = gameState
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
    }

    if (showResetDialog) {
        LunarimResetDialog(
            selectedChild = selectedChild,
            resetCountdown = resetCountdown,
            onConfirm = {
                showResetDialog = false
                showFinalResetDialog = true
            },
            onDismiss = {
                showResetDialog = false
            }
        )
    }

    if (showFinalResetDialog) {
        LunarimFinalResetDialog(
            selectedChild = selectedChild,
            onConfirm = ::deleteCharacter,
            onDismiss = {
                showFinalResetDialog = false
            }
        )
    }

    if (showExitDialog) {
        LunarimExitDialog(
            hasSavedCharacter = hasSavedCharacter,
            onConfirm = {
                if (hasSavedCharacter) {
                    saveAndExit()
                } else {
                    showExitDialog = false
                    onExit()
                }
            },
            onDismiss = {
                showExitDialog = false
            }
        )
    }

}

@Composable
private fun LunarimExitDialog(
    hasSavedCharacter: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    LunarimRpgDialogFrame(
        icon = Icons.Default.ExitToApp,
        title = "LUNARIM VERLASSEN",
        message = if (hasSavedCharacter) {
            "Deine Reise wird an dieser Stelle bewahrt. Beim nächsten Betreten von Lunarim erwachst du wieder in deinem Lager."
        } else {
            "Möchtest du Lunarim verlassen, bevor deine Reise begonnen hat?"
        }
    ) {
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = onConfirm,
            colors = ButtonDefaults.buttonColors(
                containerColor = LunarimDialogColors.runeGold,
                contentColor = LunarimDialogColors.buttonText
            )
        ) {
            Icon(
                imageVector = if (hasSavedCharacter) {
                    Icons.Default.Save
                } else {
                    Icons.Default.ExitToApp
                },
                contentDescription = null
            )

            Text(
                modifier = Modifier.padding(start = 8.dp),
                text = if (hasSavedCharacter) {
                    "Speichern und verlassen"
                } else {
                    "Spiel verlassen"
                },
                fontWeight = FontWeight.Bold
            )
        }

        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = onDismiss,
            border = BorderStroke(
                width = 1.dp,
                color = LunarimDialogColors.runeGoldMuted.copy(alpha = 0.65f)
            ),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = LunarimDialogColors.parchment
            )
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = null
            )

            Text(
                modifier = Modifier.padding(start = 8.dp),
                text = "Zurück nach Lunarim"
            )
        }
    }
}

@Composable
private fun LunarimResetDialog(
    selectedChild: Child?,
    resetCountdown: Int,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    LunarimRpgDialogFrame(
        icon = Icons.Default.WarningAmber,
        title = "CHRONIK ZURÜCKSETZEN",
        message = buildString {
            append(
                "Der vollständige Lunarim-Spielstand von "
            )
            append(selectedChild?.name ?: "diesem Charakter")
            append(
                " wird zum Löschen vorbereitet.\n\n"
            )
            append(
                "Luna Coins, Luna Silver, LunaME-Level und LunaME-Skills bleiben erhalten."
            )
        },
        accentColor = LunarimDialogColors.warningGold
    ) {
        Button(
            modifier = Modifier.fillMaxWidth(),
            enabled = resetCountdown <= 0,
            onClick = onConfirm,
            colors = ButtonDefaults.buttonColors(
                containerColor = LunarimDialogColors.warningGold,
                contentColor = LunarimDialogColors.buttonText,
                disabledContainerColor =
                    LunarimDialogColors.warningGold.copy(alpha = 0.30f),
                disabledContentColor =
                    LunarimDialogColors.parchment.copy(alpha = 0.45f)
            )
        ) {
            Icon(
                imageVector = Icons.Default.DeleteOutline,
                contentDescription = null
            )

            Text(
                modifier = Modifier.padding(start = 8.dp),
                text = if (resetCountdown > 0) {
                    "Zurücksetzen ($resetCountdown)"
                } else {
                    "Weiter zur Bestätigung"
                },
                fontWeight = FontWeight.Bold
            )
        }

        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = onDismiss,
            border = BorderStroke(
                width = 1.dp,
                color = LunarimDialogColors.runeGoldMuted.copy(alpha = 0.65f)
            ),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = LunarimDialogColors.parchment
            )
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = null
            )

            Text(
                modifier = Modifier.padding(start = 8.dp),
                text = "Abbrechen"
            )
        }
    }
}

@Composable
private fun LunarimFinalResetDialog(
    selectedChild: Child?,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    LunarimRpgDialogFrame(
        icon = Icons.Default.WarningAmber,
        title = "ENDGÜLTIG LÖSCHEN?",
        message = buildString {
            append("Die Chronik von ")
            append(selectedChild?.name ?: "diesem Charakter")
            append(
                " wird unwiderruflich vernichtet.\n\nDieser Schritt kann nicht rückgängig gemacht werden."
            )
        },
        accentColor = LunarimDialogColors.dangerRed
    ) {
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = onConfirm,
            colors = ButtonDefaults.buttonColors(
                containerColor = LunarimDialogColors.dangerRed,
                contentColor = Color.White
            )
        ) {
            Icon(
                imageVector = Icons.Default.DeleteOutline,
                contentDescription = null
            )

            Text(
                modifier = Modifier.padding(start = 8.dp),
                text = "Chronik endgültig löschen",
                fontWeight = FontWeight.Bold
            )
        }

        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = onDismiss,
            border = BorderStroke(
                width = 1.dp,
                color = LunarimDialogColors.runeGoldMuted.copy(alpha = 0.65f)
            ),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = LunarimDialogColors.parchment
            )
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = null
            )

            Text(
                modifier = Modifier.padding(start = 8.dp),
                text = "Abbrechen"
            )
        }
    }
}

@Composable
private fun LunarimRpgDialogFrame(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    message: String,
    accentColor: Color = LunarimDialogColors.runeGold,
    buttons: @Composable ColumnScope.() -> Unit
) {
    Dialog(
        onDismissRequest = {
            // Wird ausschließlich über die sichtbaren Schaltflächen geschlossen.
        },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp)
                .widthIn(max = 460.dp),
            shape = MaterialTheme.shapes.extraLarge,
            border = BorderStroke(
                width = 1.dp,
                color = accentColor.copy(alpha = 0.78f)
            ),
            colors = CardDefaults.cardColors(
                containerColor = LunarimDialogColors.darkStone
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 18.dp
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                LunarimDialogColors.darkStoneRaised,
                                LunarimDialogColors.darkStone
                            )
                        )
                    )
                    .padding(
                        horizontal = 22.dp,
                        vertical = 20.dp
                    ),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = accentColor
                )

                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = LunarimDialogColors.parchment
                )

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 2.dp),
                    color = accentColor.copy(alpha = 0.48f)
                )

                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = LunarimDialogColors.secondaryText
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    content = buttons
                )
            }
        }
    }
}

private object LunarimDialogColors {
    val darkStone = Color(0xFF17191C)
    val darkStoneRaised = Color(0xFF22262A)
    val runeGold = Color(0xFFD1AC62)
    val runeGoldMuted = Color(0xFF9F824C)
    val warningGold = Color(0xFFC58B45)
    val dangerRed = Color(0xFF9F4742)
    val parchment = Color(0xFFE8E0CF)
    val secondaryText = Color(0xFFB8B1A4)
    val dangerText = Color(0xFFD9A09A)
    val buttonText = Color(0xFF1A1712)
}

@Composable
private fun LunarimStartScreen(
    modifier: Modifier = Modifier,
    hasSavedCharacter: Boolean,
    canCreateCharacter: Boolean,
    onCreateCharacter: () -> Unit,
    onContinueGame: () -> Unit,
    onResetCharacter: () -> Unit,
    onExit: () -> Unit
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = Color.Black
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(
                    horizontal = 18.dp,
                    vertical = 22.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 470.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Image(
                    painter = painterResource(R.drawable.lunarim_logo),
                    contentDescription = "Lunarim-Wappen",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(190.dp),
                    contentScale = ContentScale.Fit
                )

                Text(
                    text = "LUNARIM",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = LunarimDialogColors.parchment
                )

                LunarimMenuButton(
                    title = if (hasSavedCharacter) {
                        "SPIEL FORTSETZEN"
                    } else {
                        "SPIEL BEGINNEN"
                    },
                    subtitle = if (hasSavedCharacter) {
                        "Im Lager erwachen und die Reise fortsetzen."
                    } else {
                        "Deine Reise durch Lunarim beginnt."
                    },
                    icon = Icons.Default.PlayArrow,
                    enabled = hasSavedCharacter || canCreateCharacter,
                    emphasized = true,
                    onClick = {
                        if (hasSavedCharacter) {
                            onContinueGame()
                        } else {
                            onCreateCharacter()
                        }
                    }
                )

                if (hasSavedCharacter) {
                    LunarimMenuButton(
                        title = "CHARAKTER ZURÜCKSETZEN",
                        subtitle = "Die aktuelle Chronik löschen und neu beginnen.",
                        icon = Icons.Default.DeleteOutline,
                        onClick = onResetCharacter
                    )
                }

                LunarimMenuButton(
                    title = "SPIEL VERLASSEN",
                    subtitle = "Zurück nach Luna Coin.",
                    icon = Icons.Default.ExitToApp,
                    danger = true,
                    onClick = onExit
                )
            }
        }
    }
}

@Composable
private fun LunarimMenuButton(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    enabled: Boolean = true,
    emphasized: Boolean = false,
    danger: Boolean = false
) {
    val borderColor = when {
        danger -> LunarimDialogColors.dangerRed.copy(alpha = 0.78f)
        emphasized -> LunarimDialogColors.runeGold.copy(alpha = 0.90f)
        else -> LunarimDialogColors.runeGoldMuted.copy(alpha = 0.62f)
    }

    val containerColor = when {
        danger -> LunarimDialogColors.dangerRed.copy(alpha = 0.15f)
        emphasized -> Color(0xFF243746)
        else -> Color(0xFF1C2024)
    }

    val contentColor = when {
        danger -> LunarimDialogColors.dangerText
        else -> LunarimDialogColors.parchment
    }

    OutlinedButton(
        modifier = Modifier.fillMaxWidth(),
        enabled = enabled,
        onClick = onClick,
        border = BorderStroke(
            width = if (emphasized) 1.5.dp else 1.dp,
            color = borderColor
        ),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = containerColor.copy(alpha = 0.35f),
            disabledContentColor = contentColor.copy(alpha = 0.40f)
        ),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            horizontal = 14.dp,
            vertical = 12.dp
        )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(30.dp),
            tint = if (danger) {
                LunarimDialogColors.dangerText
            } else {
                LunarimDialogColors.runeGold
            }
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = contentColor.copy(alpha = 0.74f)
            )
        }
    }
}
