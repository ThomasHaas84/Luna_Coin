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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import de.meson_labs.luna_coin.components.LunaScreenHeader
import de.meson_labs.luna_coin.models.Child
import de.meson_labs.luna_coin.models.GameHighscore
import de.meson_labs.luna_coin.models.LunaGameLevel
import de.meson_labs.luna_coin.models.LunaGameScoreType
import de.meson_labs.luna_coin.models.LunaGameType
import de.meson_labs.luna_coin.storage.LunaCoinStorage
import kotlinx.coroutines.delay
import kotlin.math.min

@Composable
fun LunaMultiplicationGameScreen(
    modifier: Modifier = Modifier,
    selectedChild: Child?,
    onLogout: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val storage = remember { LunaCoinStorage(context) }
    val loadedData = remember { storage.loadData() }

    var highscores by remember {
        mutableStateOf(loadedData?.gameHighscores ?: emptyList())
    }

    var children by remember {
        mutableStateOf(loadedData?.children ?: emptyList())
    }

    var missingFields by remember { mutableStateOf(setOf<Pair<Int, Int>>()) }
    val answers = remember { mutableStateMapOf<Pair<Int, Int>, String>() }

    var elapsedSeconds by remember { mutableLongStateOf(0L) }
    var timerStarted by remember { mutableStateOf(false) }
    var highscoreSaved by remember { mutableStateOf(false) }

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

    fun saveHighscore() {
        val child = selectedChild ?: return
        val data = storage.loadData() ?: return

        val newHighscore = GameHighscore(
            game = LunaGameType.MULTIPLICATION,
            childId = child.id,
            scoreType = LunaGameScoreType.TIME_SECONDS,
            level = LunaGameLevel.DEFAULT,
            value = elapsedSeconds.toInt(),
            timestamp = System.currentTimeMillis().toString()
        )

        val updatedHighscores = data.gameHighscores.upsertHighscore(newHighscore)

        storage.saveData(
            data.copy(
                gameHighscores = updatedHighscores
            )
        )

        highscores = updatedHighscores
        children = data.children
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
            saveHighscore()
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

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        LunaScreenHeader(
            title = "1 x 1",
            selectedChild = selectedChild,
            onLogout = onLogout
        )

        Spacer(modifier = Modifier.height(8.dp))

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
                Column(
                    modifier = Modifier
                        .width(leftPanelWidth)
                        .align(Alignment.CenterStart),
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
                        text = "Dein Highscore:\n${
                            personalHighscore?.let {
                                formatMultiplicationTime(it.value.toLong())
                            } ?: "-"
                        }",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "App-Highscore:\n${
                            globalHighscore?.let {
                                "${formatMultiplicationTime(it.value.toLong())} (${childName(it.childId, children)})"
                            } ?: "-"
                        }",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(onClick = onBack) {
                        Text(text = "Verlassen")
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(onClick = { createNewGame() }) {
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

                MultiplicationGrid(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset(y = (-4).dp),
                    spacing = spacing,
                    cellSize = cellSize,
                    missingFields = missingFields,
                    answers = answers,
                    onAnswerChanged = { field, value ->
                        if (!timerStarted) {
                            timerStarted = true
                        }

                        answers[field] = value.filter { it.isDigit() }.take(3)
                    }
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
    onAnswerChanged: (String) -> Unit
) {
    val correctGreen = Color(0xFF2E7D32)
    val normalCellBackground = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)

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
                    fontSize = MaterialTheme.typography.titleMedium.fontSize,
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
                            style = MaterialTheme.typography.titleMedium,
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
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}

private fun List<GameHighscore>.upsertHighscore(
    newHighscore: GameHighscore
): List<GameHighscore> {
    val existing = firstOrNull {
        it.game == newHighscore.game &&
                it.childId == newHighscore.childId &&
                it.scoreType == newHighscore.scoreType &&
                it.level == newHighscore.level
    }

    if (existing != null && existing.value <= newHighscore.value) {
        return this
    }

    return filterNot {
        it.game == newHighscore.game &&
                it.childId == newHighscore.childId &&
                it.scoreType == newHighscore.scoreType &&
                it.level == newHighscore.level
    } + newHighscore
}

private fun List<GameHighscore>.bestEntry(
    childId: String?,
    game: LunaGameType,
    scoreType: LunaGameScoreType,
    level: LunaGameLevel
): GameHighscore? {
    return filter {
        it.game == game &&
                it.scoreType == scoreType &&
                it.level == level &&
                (childId == null || it.childId == childId)
    }.minByOrNull { it.value }
}

private fun childName(
    childId: String,
    children: List<Child>
): String {
    return children.firstOrNull { it.id == childId }?.name ?: "Unbekannt"
}

private fun formatMultiplicationTime(
    seconds: Long
): String {
    val minutes = seconds / 60
    val restSeconds = seconds % 60

    return "%02d:%02d".format(minutes, restSeconds)
}