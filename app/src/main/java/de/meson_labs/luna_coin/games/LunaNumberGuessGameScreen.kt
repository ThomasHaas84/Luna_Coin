package de.meson_labs.luna_coin.games

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import de.meson_labs.luna_coin.components.LunaScreenHeader
import de.meson_labs.luna_coin.components.dialogs.ConfirmationDialog
import de.meson_labs.luna_coin.models.Child
import de.meson_labs.luna_coin.models.LunaGameLevel
import de.meson_labs.luna_coin.models.LunaGameScoreType
import de.meson_labs.luna_coin.models.LunaGameType
import de.meson_labs.luna_coin.viewmodel.LunaCoinViewModel
import kotlin.random.Random

@Composable
fun LunaNumberGuessGameScreen(
    modifier: Modifier = Modifier,
    selectedChild: Child?,
    viewModel: LunaCoinViewModel,
    onLogout: () -> Unit,
    onBack: () -> Unit,
    onFullscreenChanged: (Boolean) -> Unit = {}
) {
    val configuration = LocalConfiguration.current
    val isTabletLayout = configuration.smallestScreenWidthDp >= 600
    val isPhone = !isTabletLayout

    val screenPadding = if (isPhone) {
        14.dp
    } else {
        24.dp
    }

    val data by viewModel.data.collectAsState()
    val highscores = data.gameHighscores
    val children = data.children

    DisposableEffect(Unit) {
        onFullscreenChanged(true)

        onDispose {
            onFullscreenChanged(false)
        }
    }

    var targetNumber by remember { mutableIntStateOf(Random.nextInt(1, 101)) }
    var input by remember { mutableStateOf("") }
    var attempts by remember { mutableIntStateOf(0) }
    var lastGuess by remember { mutableStateOf<Int?>(null) }
    var message by remember { mutableStateOf("Ich denke an eine Zahl von 1 bis 100.") }
    var finished by remember { mutableStateOf(false) }
    var highscoreSaved by remember { mutableStateOf(false) }

    var showRestartConfirmation by remember { mutableStateOf(false) }
    var showExitConfirmation by remember { mutableStateOf(false) }

    val personalHighscore = highscores.bestEntry(
        childId = selectedChild?.id,
        game = LunaGameType.NUMBER_GUESS,
        scoreType = LunaGameScoreType.ATTEMPTS,
        level = LunaGameLevel.DEFAULT
    )

    val globalHighscore = highscores.bestEntry(
        childId = null,
        game = LunaGameType.NUMBER_GUESS,
        scoreType = LunaGameScoreType.ATTEMPTS,
        level = LunaGameLevel.DEFAULT
    )

    fun resetGame() {
        targetNumber = Random.nextInt(1, 101)
        input = ""
        attempts = 0
        lastGuess = null
        message = "Ich denke an eine Zahl von 1 bis 100."
        finished = false
        highscoreSaved = false
    }

    fun finishGame() {
        val child = selectedChild ?: return

        viewModel.finishNumberGuessGame(
            childId = child.id,
            attempts = attempts
        )
    }

    fun addDigit(digit: String) {
        if (finished) return
        if (input.length >= 3) return

        val newInput = (input + digit).trimStart('0')
        input = newInput.take(3)
    }

    fun removeLastDigit() {
        if (finished) return
        input = input.dropLast(1)
    }

    fun checkGuess() {
        val guess = input.toIntOrNull()

        if (guess == null || guess !in 1..100) {
            message = "Bitte gib eine Zahl von 1 bis 100 ein."
            return
        }

        attempts++
        lastGuess = guess

        when {
            guess < targetNumber -> {
                message = "Zu klein. Versuch es nochmal!"
            }

            guess > targetNumber -> {
                message = "Zu groß. Versuch es nochmal!"
            }

            else -> {
                message = "Richtig! Du hast $attempts Versuche gebraucht. 🎉"
                finished = true
            }
        }

        input = ""
    }

    LaunchedEffect(finished) {
        if (finished && !highscoreSaved && attempts > 0) {
            finishGame()
            highscoreSaved = true
        }
    }

    if (isPhone) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(screenPadding)
        ) {
            LunaScreenHeader(
                title = "Zahlenraten",
                selectedChild = selectedChild,
                onLogout = onLogout
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = { showExitConfirmation = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Zurück zum Menü")
            }

            Spacer(modifier = Modifier.height(12.dp))

            NumberGuessInfoCard(
                message = message,
                attempts = attempts,
                lastGuess = lastGuess,
                personalHighscoreText = personalHighscore?.let { "${it.value} Versuche" } ?: "-",
                globalHighscoreText = globalHighscore?.let {
                    "${it.value} Versuche (${childName(it.childId, children)})"
                } ?: "-",
                compact = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            NumberInputPanel(
                input = input,
                finished = finished,
                compact = true,
                buttonHeight = 44.dp,
                onDigitClick = { digit -> addDigit(digit) },
                onBackspaceClick = { removeLastDigit() },
                onClearClick = { input = "" },
                onCheckClick = { checkGuess() },
                modifier = Modifier.fillMaxWidth()
            )

            if (finished) {
                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { showRestartConfirmation = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Nochmal spielen")
                }
            }
        }
    } else {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(screenPadding)
        ) {
            LunaScreenHeader(
                title = "Zahlenraten",
                selectedChild = selectedChild,
                onLogout = onLogout
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxSize()
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState())
                ) {
                    Button(onClick = { showExitConfirmation = true }) {
                        Text(text = "Zurück")
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = message,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Versuche: $attempts",
                        style = MaterialTheme.typography.bodyLarge
                    )

                    if (lastGuess != null) {
                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Letzter Tipp: $lastGuess",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Bereich: 1 bis 100",
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    HighscoreCard(
                        personalHighscoreText = personalHighscore?.let { "${it.value} Versuche" } ?: "-",
                        globalHighscoreText = globalHighscore?.let {
                            "${it.value} Versuche (${childName(it.childId, children)})"
                        } ?: "-",
                        compact = false
                    )

                    if (finished) {
                        Spacer(modifier = Modifier.height(16.dp))

                        Button(onClick = { showRestartConfirmation = true }) {
                            Text(text = "Nochmal spielen")
                        }
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                NumberInputPanel(
                    input = input,
                    finished = finished,
                    compact = false,
                    buttonHeight = 40.dp,
                    onDigitClick = { digit -> addDigit(digit) },
                    onBackspaceClick = { removeLastDigit() },
                    onClearClick = { input = "" },
                    onCheckClick = { checkGuess() },
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(0.45f)
                )
            }
        }
    }

    if (showRestartConfirmation) {
        ConfirmationDialog(
            title = "Neues Spiel starten?",
            message = "Möchtest du wirklich ein neues Spiel starten?",
            confirmText = "Ja, neu starten",
            dismissText = "Abbrechen",
            onConfirm = {
                resetGame()
                showRestartConfirmation = false
            },
            onDismiss = { showRestartConfirmation = false }
        )
    }

    if (showExitConfirmation) {
        ConfirmationDialog(
            title = "Spiel verlassen?",
            message = "Möchtest du wirklich zum Menü zurückkehren?",
            confirmText = "Verlassen",
            dismissText = "Weiter spielen",
            onConfirm = {
                onBack()
                showExitConfirmation = false
            },
            onDismiss = { showExitConfirmation = false }
        )
    }
}

@Composable
private fun NumberGuessInfoCard(
    message: String,
    attempts: Int,
    lastGuess: Int?,
    personalHighscoreText: String,
    globalHighscoreText: String,
    compact: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(if (compact) 18.dp else 22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (compact) 2.dp else 4.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(if (compact) 12.dp else 16.dp)
        ) {
            Text(
                text = message,
                style = if (compact) {
                    MaterialTheme.typography.titleMedium
                } else {
                    MaterialTheme.typography.titleLarge
                },
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(if (compact) 10.dp else 14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Versuche: $attempts",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = "Letzter Tipp: ${lastGuess ?: "-"}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(if (compact) 8.dp else 12.dp))

            Text(
                text = "Bereich: 1 bis 100",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(if (compact) 10.dp else 14.dp))

            HighscoreCard(
                personalHighscoreText = personalHighscoreText,
                globalHighscoreText = globalHighscoreText,
                compact = compact
            )
        }
    }
}

@Composable
private fun HighscoreCard(
    personalHighscoreText: String,
    globalHighscoreText: String,
    compact: Boolean
) {
    Card(
        shape = RoundedCornerShape(if (compact) 14.dp else 18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (compact) {
                MaterialTheme.colorScheme.surface
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(if (compact) 10.dp else 14.dp)
        ) {
            Text(
                text = "Highscores",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Dein Highscore: $personalHighscoreText",
                style = if (compact) {
                    MaterialTheme.typography.bodyMedium
                } else {
                    MaterialTheme.typography.bodyLarge
                },
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = "App-Highscore: $globalHighscoreText",
                style = if (compact) {
                    MaterialTheme.typography.bodyMedium
                } else {
                    MaterialTheme.typography.bodyLarge
                },
                maxLines = if (compact) 2 else 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun NumberInputPanel(
    input: String,
    finished: Boolean,
    compact: Boolean,
    buttonHeight: Dp,
    onDigitClick: (String) -> Unit,
    onBackspaceClick: () -> Unit,
    onClearClick: () -> Unit,
    onCheckClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(if (compact) 18.dp else 22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (compact) 4.dp else 8.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = if (compact) 10.dp else 10.dp,
                    vertical = if (compact) 10.dp else 6.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = if (input.isBlank()) "?" else input,
                style = if (compact) {
                    MaterialTheme.typography.displaySmall
                } else {
                    MaterialTheme.typography.headlineSmall
                },
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(if (compact) 10.dp else 8.dp))

            NumberButtonRow(
                values = listOf("1", "2", "3"),
                finished = finished,
                buttonHeight = buttonHeight,
                onDigitClick = onDigitClick
            )
            Spacer(modifier = Modifier.height(6.dp))
            NumberButtonRow(
                values = listOf("4", "5", "6"),
                finished = finished,
                buttonHeight = buttonHeight,
                onDigitClick = onDigitClick
            )
            Spacer(modifier = Modifier.height(6.dp))
            NumberButtonRow(
                values = listOf("7", "8", "9"),
                finished = finished,
                buttonHeight = buttonHeight,
                onDigitClick = onDigitClick
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                OutlinedButton(
                    onClick = onClearClick,
                    enabled = !finished && input.isNotEmpty(),
                    modifier = Modifier
                        .weight(1f)
                        .height(buttonHeight)
                ) {
                    Text(text = "C")
                }

                Button(
                    onClick = { onDigitClick("0") },
                    enabled = !finished,
                    modifier = Modifier
                        .weight(1f)
                        .height(buttonHeight)
                ) {
                    Text(text = "0")
                }

                OutlinedButton(
                    onClick = onBackspaceClick,
                    enabled = !finished && input.isNotEmpty(),
                    modifier = Modifier
                        .weight(1f)
                        .height(buttonHeight)
                ) {
                    Text(text = "⌫")
                }
            }

            Spacer(modifier = Modifier.height(if (compact) 10.dp else 8.dp))

            Button(
                onClick = onCheckClick,
                enabled = !finished && input.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (compact) 48.dp else buttonHeight)
            ) {
                Text(text = "Prüfen")
            }
        }
    }
}

@Composable
private fun NumberButtonRow(
    values: List<String>,
    finished: Boolean,
    buttonHeight: Dp,
    onDigitClick: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        values.forEach { value ->
            Button(
                onClick = { onDigitClick(value) },
                enabled = !finished,
                modifier = Modifier
                    .weight(1f)
                    .height(buttonHeight)
            ) {
                Text(text = value)
            }
        }
    }
}
