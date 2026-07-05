package de.meson_labs.luna_coin.games

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.meson_labs.luna_coin.components.LunaScreenHeader
import de.meson_labs.luna_coin.components.dialogs.ConfirmationDialog
import de.meson_labs.luna_coin.models.Child
import de.meson_labs.luna_coin.models.LunaGameLevel
import de.meson_labs.luna_coin.models.LunaGameScoreType
import de.meson_labs.luna_coin.models.LunaGameType
import de.meson_labs.luna_coin.viewmodel.LunaCoinViewModel
import kotlin.random.Random

private const val WORD_GUESS_MAX_LIVES = 9

private data class WordGuessWord(
    val word: String,
    val hint: String
)

private val wordGuessWords = listOf(
    WordGuessWord("ZWOELF", "Eine Zahl"),
    WordGuessWord("PILZE", "Wächst im Wald"),
    WordGuessWord("BRAUT", "Hochzeit"),
    WordGuessWord("TANTE", "Familie"),
    WordGuessWord("SORGE", "Gefühl"),
    WordGuessWord("OTTER", "Tier am Wasser"),
    WordGuessWord("PFERD", "Tier"),
    WordGuessWord("ZWANG", "Man muss etwas tun"),
    WordGuessWord("QUARZ", "Stein / Mineral"),
    WordGuessWord("SPECK", "Essen"),
    WordGuessWord("ECKEN", "Davon hat ein Quadrat vier"),
    WordGuessWord("AUTOS", "Fahrzeuge"),
    WordGuessWord("SAEULE", "Gebäude-Teil"),
    WordGuessWord("ANGEL", "Damit fängt man Fische"),
    WordGuessWord("JETZT", "Nicht später"),
    WordGuessWord("KAMPF", "Auseinandersetzung"),
    WordGuessWord("MUENZE", "Geldstück"),
    WordGuessWord("WALZE", "Runde Rolle"),
    WordGuessWord("JOGGEN", "Sport"),
    WordGuessWord("BAUEN", "Etwas herstellen"),
    WordGuessWord("LEBEN", "Nicht tot sein"),
    WordGuessWord("KAUEN", "Mit den Zähnen"),
    WordGuessWord("PIZZA", "Essen"),
    WordGuessWord("BUSCH", "Pflanze")
)

@Composable
fun LunaWordGuessGameScreen(
    modifier: Modifier = Modifier,
    selectedChild: Child?,
    viewModel: LunaCoinViewModel,
    onLogout: () -> Unit,
    onBack: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val isPhone = configuration.smallestScreenWidthDp < 600
    val screenPadding = if (isPhone) 14.dp else 24.dp

    val data by viewModel.data.collectAsState()
    val highscores = data.gameHighscores
    val children = data.children

    var currentWord by remember { mutableStateOf(wordGuessWords.random()) }
    var lives by remember { mutableIntStateOf(WORD_GUESS_MAX_LIVES) }
    var guessedLetters by remember { mutableStateOf(setOf<Char>()) }
    var wrongLetters by remember { mutableStateOf(setOf<Char>()) }
    var wordGuessInput by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("Rate Buchstaben oder löse direkt das Wort.") }
    var finished by remember { mutableStateOf(false) }
    var won by remember { mutableStateOf(false) }
    var highscoreSaved by remember { mutableStateOf(false) }
    var showRestartConfirmation by remember { mutableStateOf(false) }
    var showExitConfirmation by remember { mutableStateOf(false) }

    val visibleWord = currentWord.word.map { letter ->
        if (letter in guessedLetters) letter else '_'
    }.joinToString(" ")

    val mistakes = WORD_GUESS_MAX_LIVES - lives

    val personalHighscore = highscores.bestEntry(
        childId = selectedChild?.id,
        game = LunaGameType.WORD_GUESS,
        scoreType = LunaGameScoreType.ATTEMPTS,
        level = LunaGameLevel.DEFAULT
    )
    val globalHighscore = highscores.bestEntry(
        childId = null,
        game = LunaGameType.WORD_GUESS,
        scoreType = LunaGameScoreType.ATTEMPTS,
        level = LunaGameLevel.DEFAULT
    )

    fun resetGame() {
        currentWord = wordGuessWords.random()
        lives = WORD_GUESS_MAX_LIVES
        guessedLetters = emptySet()
        wrongLetters = emptySet()
        wordGuessInput = ""
        message = "Rate Buchstaben oder löse direkt das Wort."
        finished = false
        won = false
        highscoreSaved = false
    }

    fun finishGame(wasWon: Boolean) {
        finished = true
        won = wasWon
        message = if (wasWon) {
            "Gewonnen! Das Wort war ${currentWord.word}."
        } else {
            "Leider verloren. Das Wort war ${currentWord.word}."
        }
    }

    fun guessLetter(letter: Char) {
        if (finished) return
        if (letter in guessedLetters || letter in wrongLetters) return

        if (letter in currentWord.word) {
            val updatedLetters = guessedLetters + letter
            guessedLetters = updatedLetters
            message = "Richtig! $letter kommt vor."
            if (currentWord.word.all { it in updatedLetters }) {
                finishGame(true)
            }
        } else {
            wrongLetters = wrongLetters + letter
            lives--
            message = "Falsch. Ein Leben weniger."
            if (lives <= 0) {
                finishGame(false)
            }
        }
    }

    fun guessWord() {
        if (finished) return
        val normalizedGuess = normalizeWordGuess(wordGuessInput)
        if (normalizedGuess.isBlank()) {
            message = "Bitte gib erst ein Wort ein."
            return
        }
        wordGuessInput = ""
        if (normalizedGuess == currentWord.word) {
            guessedLetters = currentWord.word.toSet()
            finishGame(true)
        } else {
            lives--
            message = "Das war nicht das Wort. Ein Leben weniger."
            if (lives <= 0) {
                finishGame(false)
            }
        }
    }

    LaunchedEffect(finished, won, highscoreSaved) {
        val child = selectedChild
        if (finished && won && !highscoreSaved && child != null) {
            viewModel.finishGame(
                game = LunaGameType.WORD_GUESS,
                childId = child.id,
                level = LunaGameLevel.DEFAULT,
                scores = listOf(
                    de.meson_labs.luna_coin.manager.GameResultScore(
                        scoreType = LunaGameScoreType.ATTEMPTS,
                        value = mistakes
                    )
                )
            )
            highscoreSaved = true
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(screenPadding)
    ) {
        LunaScreenHeader(
            title = "Neun Leben",
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

        WordGuessInfoCard(
            visibleWord = visibleWord,
            lives = lives,
            hint = currentWord.hint,
            message = message,
            wrongLetters = wrongLetters,
            personalHighscoreText = personalHighscore?.let { "${it.value} Fehler" } ?: "-",
            globalHighscoreText = globalHighscore?.let { "${it.value} Fehler (${childName(it.childId, children)})" } ?: "-",
            compact = isPhone
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = wordGuessInput,
            onValueChange = { newValue ->
                wordGuessInput = newValue.take(16)
            },
            enabled = !finished,
            singleLine = true,
            label = { Text(text = "Wort lösen") },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Characters
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { guessWord() },
            enabled = !finished && wordGuessInput.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Wort prüfen")
        }

        Spacer(modifier = Modifier.height(14.dp))

        LetterKeyboard(
            guessedLetters = guessedLetters,
            wrongLetters = wrongLetters,
            finished = finished,
            onLetterClick = { letter -> guessLetter(letter) }
        )

        if (finished) {
            Spacer(modifier = Modifier.height(14.dp))
            Button(
                onClick = { showRestartConfirmation = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Nochmal spielen")
            }
        }
    }

    if (showRestartConfirmation) {
        ConfirmationDialog(
            title = "Neues Spiel starten?",
            message = "Möchtest du wirklich ein neues Neun-Leben-Spiel starten?",
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
private fun WordGuessInfoCard(
    visibleWord: String,
    lives: Int,
    hint: String,
    message: String,
    wrongLetters: Set<Char>,
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
        elevation = CardDefaults.cardElevation(defaultElevation = if (compact) 2.dp else 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(if (compact) 12.dp else 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = visibleWord,
                style = if (compact) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "❤".repeat(lives),
                fontSize = if (compact) 26.sp else 34.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Tipp: $hint",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Falsche Buchstaben: ${wrongLetters.sorted().joinToString(" ").ifBlank { "-" }}",
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = "Highscores",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "Dein Highscore: $personalHighscoreText")
                    Text(text = "App-Highscore: $globalHighscoreText")
                }
            }
        }
    }
}

@Composable
private fun LetterKeyboard(
    guessedLetters: Set<Char>,
    wrongLetters: Set<Char>,
    finished: Boolean,
    onLetterClick: (Char) -> Unit
) {
    val letters = listOf(
        'A', 'B', 'C', 'D', 'E', 'F', 'G',
        'H', 'I', 'J', 'K', 'L', 'M', 'N',
        'O', 'P', 'Q', 'R', 'S', 'T', 'U',
        'V', 'W', 'X', 'Y', 'Z'
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            letters.forEach { letter ->
                val alreadyUsed = letter in guessedLetters || letter in wrongLetters
                OutlinedButton(
                    onClick = { onLetterClick(letter) },
                    enabled = !finished && !alreadyUsed,
                    modifier = Modifier
                        .height(44.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(12.dp)
                        )
                ) {
                    Text(
                        text = letter.toString(),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

private fun normalizeWordGuess(input: String): String {
    return input
        .trim()
        .uppercase()
        .replace("Ä", "AE")
        .replace("Ö", "OE")
        .replace("Ü", "UE")
        .replace("ß", "SS")
        .filter { it in 'A'..'Z' }
}
