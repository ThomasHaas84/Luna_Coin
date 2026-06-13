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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.meson_labs.luna_coin.components.LunaScreenHeader
import de.meson_labs.luna_coin.models.Child
import de.meson_labs.luna_coin.models.GameHighscore
import de.meson_labs.luna_coin.models.LunaGameLevel
import de.meson_labs.luna_coin.models.LunaGameScoreType
import de.meson_labs.luna_coin.models.LunaGameType
import de.meson_labs.luna_coin.storage.LunaCoinStorage
import kotlin.random.Random

@Composable
fun LunaNumberGuessGameScreen(
    modifier: Modifier = Modifier,
    selectedChild: Child?,
    onLogout: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val storage = remember { LunaCoinStorage(context) }
    var highscores by remember {
        mutableStateOf(storage.loadData()?.gameHighscores ?: emptyList())
    }

    var children by remember {
        mutableStateOf(storage.loadData()?.children ?: emptyList())
    }

    LaunchedEffect(selectedChild?.id) {
        val data = storage.loadData()
        highscores = data?.gameHighscores ?: emptyList()
        children = data?.children ?: emptyList()
    }

    var targetNumber by remember { mutableIntStateOf(Random.nextInt(1, 101)) }
    var input by remember { mutableStateOf("") }
    var attempts by remember { mutableIntStateOf(0) }
    var lastGuess by remember { mutableStateOf<Int?>(null) }
    var message by remember { mutableStateOf("Ich denke an eine Zahl von 1 bis 100.") }
    var finished by remember { mutableStateOf(false) }
    var highscoreSaved by remember { mutableStateOf(false) }

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

    fun saveHighscore() {
        val child = selectedChild ?: return
        val data = storage.loadData() ?: return

        val newHighscore = GameHighscore(
            game = LunaGameType.NUMBER_GUESS,
            childId = child.id,
            scoreType = LunaGameScoreType.ATTEMPTS,
            level = LunaGameLevel.DEFAULT,
            value = attempts,
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
            saveHighscore()
            highscoreSaved = true
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
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
                Button(onClick = onBack) {
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

                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp)
                    ) {
                        Text(
                            text = "Highscores",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Dein Highscore: ${
                                personalHighscore?.let { "${it.value} Versuche" } ?: "-"
                            }",
                            style = MaterialTheme.typography.bodyLarge
                        )

                        Text(
                            text = "App-Highscore: ${
                                globalHighscore?.let {
                                    "${it.value} Versuche (${childName(it.childId, children)})"
                                } ?: "-"
                            }",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }

                if (finished) {
                    Spacer(modifier = Modifier.height(16.dp))

                    Button(onClick = { resetGame() }) {
                        Text(text = "Nochmal spielen")
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            NumberInputPanel(
                input = input,
                finished = finished,
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

@Composable
private fun NumberInputPanel(
    input: String,
    finished: Boolean,
    onDigitClick: (String) -> Unit,
    onBackspaceClick: () -> Unit,
    onClearClick: () -> Unit,
    onCheckClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = if (input.isBlank()) "?" else input,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            NumberButtonRow(listOf("1", "2", "3"), finished, onDigitClick)
            Spacer(modifier = Modifier.height(6.dp))
            NumberButtonRow(listOf("4", "5", "6"), finished, onDigitClick)
            Spacer(modifier = Modifier.height(6.dp))
            NumberButtonRow(listOf("7", "8", "9"), finished, onDigitClick)

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                OutlinedButton(
                    onClick = onClearClick,
                    enabled = !finished && input.isNotEmpty(),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = "C")
                }

                Button(
                    onClick = { onDigitClick("0") },
                    enabled = !finished,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = "0")
                }

                OutlinedButton(
                    onClick = onBackspaceClick,
                    enabled = !finished && input.isNotEmpty(),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = "⌫")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onCheckClick,
                enabled = !finished && input.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
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
                modifier = Modifier.weight(1f)
            ) {
                Text(text = value)
            }
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