// games/LunaMultiplicationGameScreen.kt
package de.meson_labs.luna_coin.games

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import de.meson_labs.luna_coin.components.LunaScreenHeader
import de.meson_labs.luna_coin.components.dialogs.ConfirmationDialog
import de.meson_labs.luna_coin.models.Child
import de.meson_labs.luna_coin.models.LunaGameLevel
import de.meson_labs.luna_coin.models.LunaGameScoreType
import de.meson_labs.luna_coin.models.LunaGameType
import de.meson_labs.luna_coin.viewmodel.LunaCoinViewModel
import kotlinx.coroutines.delay
import kotlin.math.min

@Composable
fun LunaMultiplicationGameScreen(
    modifier: Modifier = Modifier,
    selectedChild: Child?,
    viewModel: LunaCoinViewModel,
    onLogout: () -> Unit,
    onBack: () -> Unit
) {
    val data by viewModel.data.collectAsState()
    val highscores = data.gameHighscores
    val children = data.children

    var missingFields by remember { mutableStateOf(setOf<Pair<Int, Int>>()) }
    val answers = remember { mutableStateMapOf<Pair<Int, Int>, String>() }

    var elapsedSeconds by remember { mutableLongStateOf(0L) }
    var timerStarted by remember { mutableStateOf(false) }
    var highscoreSaved by remember { mutableStateOf(false) }

    var showRestartConfirmation by remember { mutableStateOf(false) }
    var showExitConfirmation by remember { mutableStateOf(false) }

    fun createNewGame() {
        missingFields = (1..10)
            .flatMap { left ->
                (1..10).map { right ->
                    left to right
                }
            }
            .shuffled()
            .take(10)
            .toSet()

        answers.clear()

        missingFields.forEach { field ->
            answers[field] = ""
        }

        elapsedSeconds = 0L
        timerStarted = false
        highscoreSaved = false
    }

    fun finishGame() {
        val child = selectedChild ?: return

        viewModel.finishMultiplicationGame(
            childId = child.id,
            timeSeconds = elapsedSeconds.toInt()
        )
    }

    LaunchedEffect(Unit) {
        createNewGame()
    }

    val allCorrect = missingFields.isNotEmpty() && missingFields.all { field ->
        answers[field]?.toIntOrNull() == field.first * field.second
    }

    val gameIsRunning = timerStarted && !allCorrect

    LaunchedEffect(gameIsRunning) {
        while (gameIsRunning) {
            delay(1000)
            elapsedSeconds++
        }
    }

    LaunchedEffect(allCorrect) {
        if (allCorrect && !highscoreSaved && elapsedSeconds > 0L) {
            finishGame()
            highscoreSaved = true
        }
    }

    val personalHighscore = highscores.bestEntry(
        childId = selectedChild?.id,
        game = LunaGameType.MULTIPLICATION,
        scoreType = LunaGameScoreType.TIME_SECONDS,
        level = LunaGameLevel.DEFAULT
    )

    val globalHighscore = highscores.bestEntry(
        childId = null,
        game = LunaGameType.MULTIPLICATION,
        scoreType = LunaGameScoreType.TIME_SECONDS,
        level = LunaGameLevel.DEFAULT
    )

    val configuration = LocalConfiguration.current
    val isTabletLayout = configuration.smallestScreenWidthDp >= 600
    val isPhoneLandscape = !isTabletLayout && configuration.screenWidthDp > configuration.screenHeightDp
    val screenPadding = if (isTabletLayout) 24.dp else 12.dp

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(screenPadding)
    ) {
        LunaScreenHeader(
            title = "1 x 1",
            selectedChild = selectedChild,
            onLogout = onLogout
        )

        Spacer(modifier = Modifier.height(if (isTabletLayout) 8.dp else 6.dp))

        if (isTabletLayout) {
            TabletMultiplicationLayout(
                elapsedSeconds = elapsedSeconds,
                personalHighscoreText = personalHighscore?.let {
                    formatMultiplicationTime(it.value.toLong())
                } ?: "-",
                globalHighscoreText = globalHighscore?.let {
                    "${formatMultiplicationTime(it.value.toLong())} (${childName(it.childId, children)})"
                } ?: "-",
                allCorrect = allCorrect,
                missingFields = missingFields,
                answers = answers,
                onExitClick = { showExitConfirmation = true },
                onRestartClick = { showRestartConfirmation = true },
                onAnswerChanged = { field, value ->
                    if (!timerStarted) {
                        timerStarted = true
                    }

                    answers[field] = value.filter { it.isDigit() }.take(3)
                }
            )
        } else {
            PhoneMultiplicationLayout(
                isLandscape = isPhoneLandscape,
                elapsedSeconds = elapsedSeconds,
                personalHighscoreText = personalHighscore?.let {
                    formatMultiplicationTime(it.value.toLong())
                } ?: "-",
                globalHighscoreText = globalHighscore?.let {
                    "${formatMultiplicationTime(it.value.toLong())} (${childName(it.childId, children)})"
                } ?: "-",
                allCorrect = allCorrect,
                missingFields = missingFields,
                answers = answers,
                onExitClick = { showExitConfirmation = true },
                onRestartClick = { showRestartConfirmation = true },
                onAnswerChanged = { field, value ->
                    if (!timerStarted) {
                        timerStarted = true
                    }

                    answers[field] = value.filter { it.isDigit() }.take(3)
                }
            )
        }
    }

    if (showRestartConfirmation) {
        ConfirmationDialog(
            title = "Neues Spiel starten?",
            message = "Möchtest du wirklich ein neues Spiel starten? Der aktuelle Fortschritt geht verloren.",
            confirmText = "Neu starten",
            dismissText = "Abbrechen",
            onConfirm = {
                createNewGame()
                showRestartConfirmation = false
            },
            onDismiss = { showRestartConfirmation = false }
        )
    }

    if (showExitConfirmation) {
        ConfirmationDialog(
            title = "Spiel verlassen?",
            message = "Möchtest du wirklich zum Menü zurückkehren? Der aktuelle Fortschritt geht verloren.",
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
private fun TabletMultiplicationLayout(
    elapsedSeconds: Long,
    personalHighscoreText: String,
    globalHighscoreText: String,
    allCorrect: Boolean,
    missingFields: Set<Pair<Int, Int>>,
    answers: MutableMap<Pair<Int, Int>, String>,
    onExitClick: () -> Unit,
    onRestartClick: () -> Unit,
    onAnswerChanged: (Pair<Int, Int>, String) -> Unit
) {
    BoxWithConstraints(
        modifier = Modifier.fillMaxSize()
    ) {
        val spacing = 4.dp
        val leftPanelWidth = 210.dp
        val safetySpace = 16.dp

        val cellSizeByWidth = (maxWidth - spacing * 9) / 10
        val cellSizeByHeight = (maxHeight - spacing * 9 - safetySpace) / 10
        val cellSize = min(cellSizeByWidth.value, cellSizeByHeight.value).dp

        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            MultiplicationInfoPanel(
                modifier = Modifier
                    .width(leftPanelWidth)
                    .align(Alignment.CenterStart),
                elapsedSeconds = elapsedSeconds,
                personalHighscoreText = personalHighscoreText,
                globalHighscoreText = globalHighscoreText,
                allCorrect = allCorrect,
                compact = false,
                onExitClick = onExitClick,
                onRestartClick = onRestartClick
            )

            MultiplicationGrid(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = (-4).dp),
                spacing = spacing,
                cellSize = cellSize,
                missingFields = missingFields,
                answers = answers,
                compact = false,
                onAnswerChanged = onAnswerChanged
            )
        }
    }
}

@Composable
private fun PhoneMultiplicationLayout(
    isLandscape: Boolean,
    elapsedSeconds: Long,
    personalHighscoreText: String,
    globalHighscoreText: String,
    allCorrect: Boolean,
    missingFields: Set<Pair<Int, Int>>,
    answers: MutableMap<Pair<Int, Int>, String>,
    onExitClick: () -> Unit,
    onRestartClick: () -> Unit,
    onAnswerChanged: (Pair<Int, Int>, String) -> Unit
) {
    if (isLandscape) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MultiplicationInfoPanel(
                modifier = Modifier
                    .width(150.dp)
                    .fillMaxSize(),
                elapsedSeconds = elapsedSeconds,
                personalHighscoreText = personalHighscoreText,
                globalHighscoreText = globalHighscoreText,
                allCorrect = allCorrect,
                compact = true,
                onExitClick = onExitClick,
                onRestartClick = onRestartClick
            )

            BoxWithConstraints(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                val spacing = 2.dp
                val cellSizeByWidth = (maxWidth - spacing * 9) / 10
                val cellSizeByHeight = (maxHeight - spacing * 9) / 10
                val cellSize = min(cellSizeByWidth.value, cellSizeByHeight.value).dp

                MultiplicationGrid(
                    spacing = spacing,
                    cellSize = cellSize,
                    missingFields = missingFields,
                    answers = answers,
                    compact = true,
                    onAnswerChanged = onAnswerChanged
                )
            }
        }
    } else {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            MultiplicationInfoPanel(
                modifier = Modifier.fillMaxWidth(),
                elapsedSeconds = elapsedSeconds,
                personalHighscoreText = personalHighscoreText,
                globalHighscoreText = globalHighscoreText,
                allCorrect = allCorrect,
                compact = true,
                onExitClick = onExitClick,
                onRestartClick = onRestartClick
            )

            Spacer(modifier = Modifier.height(8.dp))

            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.TopCenter
            ) {
                val spacing = 2.dp
                val cellSizeByWidth = (maxWidth - spacing * 9) / 10
                val cellSizeByHeight = (maxHeight - spacing * 9) / 10
                val cellSize = min(cellSizeByWidth.value, cellSizeByHeight.value).dp

                MultiplicationGrid(
                    spacing = spacing,
                    cellSize = cellSize,
                    missingFields = missingFields,
                    answers = answers,
                    compact = true,
                    onAnswerChanged = onAnswerChanged
                )
            }
        }
    }
}

@Composable
private fun MultiplicationInfoPanel(
    modifier: Modifier = Modifier,
    elapsedSeconds: Long,
    personalHighscoreText: String,
    globalHighscoreText: String,
    allCorrect: Boolean,
    compact: Boolean,
    onExitClick: () -> Unit,
    onRestartClick: () -> Unit
) {
    if (compact) {
        Card(
            modifier = modifier,
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
            )
        ) {
            Column(
                modifier = Modifier.padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Fülle 10 Felder aus",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Zeit: ${formatMultiplicationTime(elapsedSeconds)}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Dein Rekord: $personalHighscoreText",
                    style = MaterialTheme.typography.labelMedium,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "App: $globalHighscoreText",
                    style = MaterialTheme.typography.labelMedium,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Button(
                        onClick = onExitClick,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = "Zurück")
                    }

                    Button(
                        onClick = onRestartClick,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = "Neu")
                    }
                }

                if (allCorrect) {
                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Super! Alles richtig! 🎉",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    } else {
        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Fülle die 10 leeren Felder aus",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Zeit: ${formatMultiplicationTime(elapsedSeconds)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Dein Highscore:\n$personalHighscoreText",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "App-Highscore:\n$globalHighscoreText",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(onClick = onExitClick) {
                Text(text = "Verlassen")
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(onClick = onRestartClick) {
                Text(text = "Neu starten")
            }

            if (allCorrect) {
                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Super!\nAlles richtig! 🎉",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun MultiplicationGrid(
    modifier: Modifier = Modifier,
    spacing: Dp,
    cellSize: Dp,
    missingFields: Set<Pair<Int, Int>>,
    answers: MutableMap<Pair<Int, Int>, String>,
    compact: Boolean = false,
    onAnswerChanged: (Pair<Int, Int>, String) -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(spacing),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        (1..10).forEach { left ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(spacing),
                verticalAlignment = Alignment.CenterVertically
            ) {
                (1..10).forEach { right ->
                    val field = left to right
                    val isMissing = field in missingFields
                    val correctValue = left * right
                    val answer = answers[field].orEmpty()
                    val answerNumber = answer.toIntOrNull()
                    val hasAnswer = answer.isNotEmpty()
                    val isCorrect = answerNumber == correctValue

                    MultiplicationCell(
                        modifier = Modifier.requiredSize(cellSize),
                        text = correctValue.toString(),
                        isMissing = isMissing,
                        answer = answer,
                        hasAnswer = hasAnswer,
                        isCorrect = isCorrect,
                        compact = compact,
                        onAnswerChanged = { value ->
                            onAnswerChanged(field, value)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun MultiplicationCell(
    modifier: Modifier = Modifier,
    text: String,
    isMissing: Boolean,
    answer: String,
    hasAnswer: Boolean,
    isCorrect: Boolean,
    compact: Boolean = false,
    onAnswerChanged: (String) -> Unit
) {
    val correctGreen = Color(0xFF2E7D32)
    val normalCellBackground = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)

    val cellTextStyle = if (compact) {
        MaterialTheme.typography.labelMedium
    } else {
        MaterialTheme.typography.titleMedium
    }

    val backgroundColor = when {
        isMissing && hasAnswer && !isCorrect -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.85f)
        else -> normalCellBackground
    }

    val borderColor = when {
        isMissing && hasAnswer && isCorrect -> correctGreen
        isMissing && hasAnswer && !isCorrect -> MaterialTheme.colorScheme.error
        isMissing -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.outlineVariant
    }

    Column(
        modifier = modifier
            .clip(MaterialTheme.shapes.small)
            .background(backgroundColor)
            .border(
                width = if (isMissing && hasAnswer) 3.dp else 1.dp,
                color = borderColor,
                shape = MaterialTheme.shapes.small
            )
            .padding(1.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (isMissing) {
            BasicTextField(
                value = answer,
                onValueChange = onAnswerChanged,
                singleLine = true,
                textStyle = TextStyle(
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = cellTextStyle.fontSize,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                ),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                decorationBox = { innerTextField ->
                    if (answer.isEmpty()) {
                        Text(
                            text = "?",
                            style = cellTextStyle,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }

                    innerTextField()
                }
            )
        } else {
            Text(
                text = text,
                style = cellTextStyle,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}

private fun formatMultiplicationTime(
    seconds: Long
): String {
    val minutes = seconds / 60
    val restSeconds = seconds % 60

    return "%02d:%02d".format(minutes, restSeconds)
}