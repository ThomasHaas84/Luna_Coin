package de.meson_labs.luna_coin.games

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.meson_labs.luna_coin.components.LunaScreenHeader
import de.meson_labs.luna_coin.models.Child
import kotlinx.coroutines.delay
import kotlin.math.min

private enum class MemoryDifficulty {
    EASY,
    HARD
}

private enum class PlayerMode {
    ONE_PLAYER,
    TWO_PLAYERS
}

private data class MemoryCard(
    val id: Int,
    val pairId: Int,
    val symbol: String,
    val isOpen: Boolean = false,
    val isMatched: Boolean = false
)

@Composable
fun LunaMemoryGameScreen(
    modifier: Modifier = Modifier,
    selectedChild: Child?,
    onLogout: () -> Unit,
    onBack: () -> Unit
) {
    val cards = remember { mutableStateListOf<MemoryCard>() }

    var selectedDifficulty by remember { mutableStateOf<MemoryDifficulty?>(null) }
    var selectedPlayerMode by remember { mutableStateOf<PlayerMode?>(null) }

    var moves by remember { mutableIntStateOf(0) }
    var elapsedSeconds by remember { mutableLongStateOf(0L) }
    var timerStarted by remember { mutableStateOf(false) }

    var pendingMismatch by remember { mutableStateOf<Pair<Int, Int>?>(null) }

    var currentPlayer by remember { mutableIntStateOf(1) }
    var playerOneScore by remember { mutableIntStateOf(0) }
    var playerTwoScore by remember { mutableIntStateOf(0) }

    fun createCards(difficulty: MemoryDifficulty): List<MemoryCard> {
        val symbols = when (difficulty) {
            MemoryDifficulty.EASY -> listOf(
                "🌙", "⭐", "🪙", "🚀", "🎮", "💎"
            )

            MemoryDifficulty.HARD -> listOf(
                "🌙", "⭐", "🪙", "🚀", "🎮", "💎",
                "🐶", "🦊", "🍀", "⚡", "🎲", "🏆"
            )
        }

        return symbols
            .flatMapIndexed { index, symbol ->
                listOf(
                    MemoryCard(id = index * 2, pairId = index, symbol = symbol),
                    MemoryCard(id = index * 2 + 1, pairId = index, symbol = symbol)
                )
            }
            .shuffled()
    }

    fun startGame(
        difficulty: MemoryDifficulty,
        playerMode: PlayerMode
    ) {
        selectedDifficulty = difficulty
        selectedPlayerMode = playerMode

        cards.clear()
        cards.addAll(createCards(difficulty))

        moves = 0
        elapsedSeconds = 0L
        timerStarted = false
        pendingMismatch = null

        currentPlayer = 1
        playerOneScore = 0
        playerTwoScore = 0
    }

    fun resetToSelection() {
        selectedDifficulty = null
        selectedPlayerMode = null

        cards.clear()

        moves = 0
        elapsedSeconds = 0L
        timerStarted = false
        pendingMismatch = null

        currentPlayer = 1
        playerOneScore = 0
        playerTwoScore = 0
    }

    fun restartSameGame() {
        val difficulty = selectedDifficulty ?: return
        val playerMode = selectedPlayerMode ?: return

        startGame(
            difficulty = difficulty,
            playerMode = playerMode
        )
    }

    val finished = cards.isNotEmpty() && cards.all { it.isMatched }
    val gameIsRunning = selectedDifficulty != null &&
            selectedPlayerMode != null &&
            timerStarted &&
            !finished

    LaunchedEffect(gameIsRunning) {
        while (gameIsRunning) {
            delay(1000)
            elapsedSeconds++
        }
    }

    LaunchedEffect(pendingMismatch) {
        val mismatch = pendingMismatch ?: return@LaunchedEffect

        delay(800)

        cards[mismatch.first] = cards[mismatch.first].copy(isOpen = false)
        cards[mismatch.second] = cards[mismatch.second].copy(isOpen = false)

        if (selectedPlayerMode == PlayerMode.TWO_PLAYERS) {
            currentPlayer = if (currentPlayer == 1) 2 else 1
        }

        pendingMismatch = null
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        val difficulty = selectedDifficulty
        val playerMode = selectedPlayerMode

        val columns = when (difficulty) {
            MemoryDifficulty.EASY -> 4
            MemoryDifficulty.HARD -> 6
            null -> 1
        }

        val rows = when (difficulty) {
            MemoryDifficulty.EASY -> 3
            MemoryDifficulty.HARD -> 4
            null -> 1
        }

        val spacing = 8.dp
        val sidePanelWidth = 180.dp

        val gameAreaWidth = if (difficulty != null) {
            maxWidth - sidePanelWidth - 24.dp
        } else {
            maxWidth
        }

        val availableHeight = maxHeight - 170.dp

        val cardSizeByWidth = (gameAreaWidth - spacing * (columns - 1)) / columns
        val cardSizeByHeight = (availableHeight - spacing * (rows - 1)) / rows
        val cardSize = min(cardSizeByWidth.value, cardSizeByHeight.value).dp

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            LunaScreenHeader(
                title = "Luna Memory",
                selectedChild = selectedChild,
                onLogout = onLogout
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(onClick = onBack) {
                    Text(text = "Zurück")
                }

                if (difficulty != null && playerMode != null && !finished) {
                    Text(
                        text = if (playerMode == PlayerMode.ONE_PLAYER) {
                            "Züge: $moves   Zeit: ${formatMemoryTime(elapsedSeconds)}"
                        } else {
                            "Spieler $currentPlayer ist dran"
                        },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (difficulty == null || playerMode == null) {
                MemoryStartSelection(
                    onStart = { selectedDifficultyValue, selectedPlayerModeValue ->
                        startGame(
                            difficulty = selectedDifficultyValue,
                            playerMode = selectedPlayerModeValue
                        )
                    }
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    Column {
                        cards.chunked(columns).forEach { rowCards ->
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(spacing)
                            ) {
                                rowCards.forEach { card ->
                                    val index = cards.indexOfFirst { it.id == card.id }

                                    MemoryCardView(
                                        card = card,
                                        modifier = Modifier.size(cardSize),
                                        onClick = {
                                            if (
                                                finished ||
                                                pendingMismatch != null ||
                                                card.isOpen ||
                                                card.isMatched
                                            ) {
                                                return@MemoryCardView
                                            }

                                            if (!timerStarted) {
                                                timerStarted = true
                                            }

                                            cards[index] = card.copy(isOpen = true)

                                            val openCards = cards
                                                .withIndex()
                                                .filter { it.value.isOpen && !it.value.isMatched }

                                            if (openCards.size == 2) {
                                                moves++

                                                val first = openCards[0]
                                                val second = openCards[1]

                                                if (first.value.pairId == second.value.pairId) {
                                                    cards[first.index] = first.value.copy(isMatched = true)
                                                    cards[second.index] = second.value.copy(isMatched = true)

                                                    if (currentPlayer == 1) {
                                                        playerOneScore++
                                                    } else {
                                                        playerTwoScore++
                                                    }
                                                } else {
                                                    pendingMismatch = Pair(first.index, second.index)
                                                }
                                            }
                                        }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(spacing))
                        }
                    }

                    MemoryScorePanel(
                        playerMode = playerMode,
                        currentPlayer = currentPlayer,
                        playerOneScore = playerOneScore,
                        playerTwoScore = playerTwoScore,
                        moves = moves,
                        elapsedSeconds = elapsedSeconds,
                        finished = finished,
                        onRestart = { restartSameGame() },
                        onExit = { resetToSelection() }
                    )
                }
            }
        }
    }
}

@Composable
private fun MemoryStartSelection(
    onStart: (MemoryDifficulty, PlayerMode) -> Unit
) {
    var selectedDifficulty by remember { mutableStateOf(MemoryDifficulty.EASY) }
    var selectedPlayerMode by remember { mutableStateOf(PlayerMode.ONE_PLAYER) }

    Column {
        Text(
            text = "Schwierigkeit auswählen",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = {
                    selectedDifficulty = MemoryDifficulty.EASY
                },
                colors = memorySelectionButtonColors(
                    selected = selectedDifficulty == MemoryDifficulty.EASY
                )
            ) {
                Text(text = "Stufe 1")
            }

            Button(
                onClick = {
                    selectedDifficulty = MemoryDifficulty.HARD
                },
                colors = memorySelectionButtonColors(
                    selected = selectedDifficulty == MemoryDifficulty.HARD
                )
            ) {
                Text(text = "Stufe 2")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Spieler auswählen",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = {
                    selectedPlayerMode = PlayerMode.ONE_PLAYER
                },
                colors = memorySelectionButtonColors(
                    selected = selectedPlayerMode == PlayerMode.ONE_PLAYER
                )
            ) {
                Text(text = "1 Spieler")
            }

            Button(
                onClick = {
                    selectedPlayerMode = PlayerMode.TWO_PLAYERS
                },
                colors = memorySelectionButtonColors(
                    selected = selectedPlayerMode == PlayerMode.TWO_PLAYERS
                )
            ) {
                Text(text = "2 Spieler")
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "Auswahl: ${difficultyText(selectedDifficulty)}, ${playerModeText(selectedPlayerMode)}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                onStart(
                    selectedDifficulty,
                    selectedPlayerMode
                )
            }
        ) {
            Text(text = "Spiel starten")
        }
    }
}

@Composable
private fun memorySelectionButtonColors(
    selected: Boolean
) = ButtonDefaults.buttonColors(
    containerColor = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    },
    contentColor = if (selected) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurface
    }
)

@Composable
private fun MemoryScorePanel(
    playerMode: PlayerMode,
    currentPlayer: Int,
    playerOneScore: Int,
    playerTwoScore: Int,
    moves: Int,
    elapsedSeconds: Long,
    finished: Boolean,
    onRestart: () -> Unit,
    onExit: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(180.dp)
            .height(390.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (!finished) {
                Text(
                    text = if (playerMode == PlayerMode.ONE_PLAYER) {
                        "Spiel läuft"
                    } else {
                        "Dran:"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (playerMode == PlayerMode.ONE_PLAYER) {
                        "Solo"
                    } else {
                        "Spieler $currentPlayer"
                    },
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(18.dp))
            } else {
                Text(
                    text = "Gewonnen",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = winnerText(
                        playerMode = playerMode,
                        playerOneScore = playerOneScore,
                        playerTwoScore = playerTwoScore
                    ),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(18.dp))
            }

            Text(
                text = "Züge",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = moves.toString(),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Zeit",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = formatMemoryTime(elapsedSeconds),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(18.dp))

            if (playerMode == PlayerMode.TWO_PLAYERS) {
                Text(
                    text = "Spieler 1: $playerOneScore",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (currentPlayer == 1 && !finished) {
                        FontWeight.Bold
                    } else {
                        FontWeight.Normal
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Spieler 2: $playerTwoScore",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (currentPlayer == 2 && !finished) {
                        FontWeight.Bold
                    } else {
                        FontWeight.Normal
                    }
                )
            } else {
                Text(
                    text = "Paare: $playerOneScore",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            if (finished) {
                Button(
                    onClick = onRestart
                ) {
                    Text(text = "Neu starten")
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onExit
                ) {
                    Text(text = "Verlassen")
                }
            }
        }
    }
}

private fun difficultyText(
    difficulty: MemoryDifficulty
): String {
    return when (difficulty) {
        MemoryDifficulty.EASY -> "Stufe 1"
        MemoryDifficulty.HARD -> "Stufe 2"
    }
}

private fun playerModeText(
    playerMode: PlayerMode
): String {
    return when (playerMode) {
        PlayerMode.ONE_PLAYER -> "1 Spieler"
        PlayerMode.TWO_PLAYERS -> "2 Spieler"
    }
}

private fun winnerText(
    playerMode: PlayerMode,
    playerOneScore: Int,
    playerTwoScore: Int
): String {
    return if (playerMode == PlayerMode.ONE_PLAYER) {
        "Geschafft!"
    } else {
        when {
            playerOneScore > playerTwoScore -> "Spieler 1"
            playerTwoScore > playerOneScore -> "Spieler 2"
            else -> "Unentschieden"
        }
    }
}

private fun formatMemoryTime(
    seconds: Long
): String {
    val minutes = seconds / 60
    val restSeconds = seconds % 60

    return "%02d:%02d".format(minutes, restSeconds)
}

@Composable
private fun MemoryCardView(
    card: MemoryCard,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .aspectRatio(1f)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (card.isOpen || card.isMatched) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = if (card.isOpen || card.isMatched) card.symbol else "?",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}