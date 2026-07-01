package de.meson_labs.luna_coin.games

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.meson_labs.luna_coin.components.LunaScreenHeader
import de.meson_labs.luna_coin.components.dialogs.ConfirmationDialog
import de.meson_labs.luna_coin.models.Child
import de.meson_labs.luna_coin.models.GameHighscore
import de.meson_labs.luna_coin.models.LunaGameLevel
import de.meson_labs.luna_coin.models.LunaGameScoreType
import de.meson_labs.luna_coin.models.LunaGameType
import de.meson_labs.luna_coin.viewmodel.LunaCoinViewModel
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
    viewModel: LunaCoinViewModel,
    onLogout: () -> Unit,
    onBack: () -> Unit
) {
    val data by viewModel.data.collectAsState()
    val highscores = data.gameHighscores
    val children = data.children

    val cards = remember { mutableStateListOf<MemoryCard>() }

    var selectedDifficulty by remember { mutableStateOf<MemoryDifficulty?>(null) }
    var selectedPlayerMode by remember { mutableStateOf<PlayerMode?>(null) }

    var moves by remember { mutableIntStateOf(0) }
    var elapsedSeconds by remember { mutableLongStateOf(0L) }
    var timerStarted by remember { mutableStateOf(false) }
    var highscoreSaved by remember { mutableStateOf(false) }

    var pendingMismatch by remember { mutableStateOf<Pair<Int, Int>?>(null) }

    var currentPlayer by remember { mutableIntStateOf(1) }
    var playerOneScore by remember { mutableIntStateOf(0) }
    var playerTwoScore by remember { mutableIntStateOf(0) }

    var showRestartConfirmation by remember { mutableStateOf(false) }
    var showExitConfirmation by remember { mutableStateOf(false) }

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
        highscoreSaved = false
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
        highscoreSaved = false
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

    fun closeMismatchAndSwitchPlayerIfNeeded() {
        val mismatch = pendingMismatch ?: return

        cards[mismatch.first] = cards[mismatch.first].copy(isOpen = false)
        cards[mismatch.second] = cards[mismatch.second].copy(isOpen = false)

        if (selectedPlayerMode == PlayerMode.TWO_PLAYERS) {
            currentPlayer = if (currentPlayer == 1) 2 else 1
        }

        pendingMismatch = null
    }

    fun saveMemoryHighscores() {
        val child = selectedChild ?: return
        val difficulty = selectedDifficulty ?: return
        val level = difficulty.toLunaGameLevel()

        viewModel.saveGameHighscore(
            game = LunaGameType.MEMORY,
            childId = child.id,
            scoreType = LunaGameScoreType.ATTEMPTS,
            level = level,
            value = moves
        )

        viewModel.saveGameHighscore(
            game = LunaGameType.MEMORY,
            childId = child.id,
            scoreType = LunaGameScoreType.TIME_SECONDS,
            level = level,
            value = elapsedSeconds.toInt()
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

    LaunchedEffect(finished) {
        if (finished && !highscoreSaved && moves > 0) {
            saveMemoryHighscores()
            highscoreSaved = true
        }
    }

    LaunchedEffect(pendingMismatch, selectedPlayerMode) {
        if (pendingMismatch != null && selectedPlayerMode == PlayerMode.TWO_PLAYERS) {
            delay(2000)
            closeMismatchAndSwitchPlayerIfNeeded()
        }
    }

    val configuration = LocalConfiguration.current
    val isTabletLayout = configuration.smallestScreenWidthDp >= 600
    val isPhone = !isTabletLayout

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .padding(if (isPhone) 14.dp else 24.dp)
    ) {
        val difficulty = selectedDifficulty
        val playerMode = selectedPlayerMode

        val columns = when (difficulty) {
            MemoryDifficulty.EASY -> if (isPhone) 3 else 4
            MemoryDifficulty.HARD -> if (isPhone) 4 else 6
            null -> 1
        }

        val rows = when (difficulty) {
            MemoryDifficulty.EASY -> if (isPhone) 4 else 3
            MemoryDifficulty.HARD -> if (isPhone) 6 else 4
            null -> 1
        }

        val spacing = if (isPhone) 6.dp else 8.dp
        val sidePanelWidth = 220.dp

        val gameAreaWidth = if (difficulty != null && !isPhone) {
            maxWidth - sidePanelWidth - 24.dp
        } else {
            maxWidth
        }

        val availableHeight = if (isPhone) {
            maxHeight - 230.dp
        } else {
            maxHeight - 170.dp
        }

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
                    highscores = highscores,
                    children = children,
                    selectedChild = selectedChild,
                    isPhone = isPhone,
                    onStart = { selectedDifficultyValue, selectedPlayerModeValue ->
                        startGame(
                            difficulty = selectedDifficultyValue,
                            playerMode = selectedPlayerModeValue
                        )
                    }
                )
            } else {
                if (isPhone) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        MemoryCompactScorePanel(
                            playerMode = playerMode,
                            currentPlayer = currentPlayer,
                            playerOneScore = playerOneScore,
                            playerTwoScore = playerTwoScore,
                            moves = moves,
                            elapsedSeconds = elapsedSeconds,
                            finished = finished,
                            onRestart = { showRestartConfirmation = true },
                            onExit = { showExitConfirmation = true }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        cards.chunked(columns).forEach { rowCards ->
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(spacing)
                            ) {
                                rowCards.forEach { card ->
                                    val index = cards.indexOfFirst { it.id == card.id }

                                    MemoryCardView(
                                        card = card,
                                        isCompact = true,
                                        modifier = Modifier.size(cardSize),
                                        onClick = {
                                            if (finished || index == -1 || cards[index].isMatched) {
                                                return@MemoryCardView
                                            }

                                            val mismatch = pendingMismatch

                                            if (mismatch != null) {
                                                if (selectedPlayerMode == PlayerMode.TWO_PLAYERS) {
                                                    return@MemoryCardView
                                                }

                                                closeMismatchAndSwitchPlayerIfNeeded()

                                                if (index == mismatch.first || index == mismatch.second) {
                                                    return@MemoryCardView
                                                }
                                            }

                                            if (cards[index].isOpen || cards[index].isMatched) {
                                                return@MemoryCardView
                                            }

                                            if (!timerStarted) {
                                                timerStarted = true
                                            }

                                            cards[index] = cards[index].copy(isOpen = true)

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
                                                if (finished || index == -1 || cards[index].isMatched) {
                                                    return@MemoryCardView
                                                }

                                                val mismatch = pendingMismatch

                                                if (mismatch != null) {
                                                    if (selectedPlayerMode == PlayerMode.TWO_PLAYERS) {
                                                        return@MemoryCardView
                                                    }

                                                    closeMismatchAndSwitchPlayerIfNeeded()

                                                    if (index == mismatch.first || index == mismatch.second) {
                                                        return@MemoryCardView
                                                    }
                                                }

                                                if (cards[index].isOpen || cards[index].isMatched) {
                                                    return@MemoryCardView
                                                }

                                                if (!timerStarted) {
                                                    timerStarted = true
                                                }

                                                cards[index] = cards[index].copy(isOpen = true)

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
                            difficulty = difficulty,
                            highscores = highscores,
                            children = children,
                            selectedChild = selectedChild,
                            currentPlayer = currentPlayer,
                            playerOneScore = playerOneScore,
                            playerTwoScore = playerTwoScore,
                            moves = moves,
                            elapsedSeconds = elapsedSeconds,
                            finished = finished,
                            onRestart = { showRestartConfirmation = true },
                            onExit = { showExitConfirmation = true }
                        )
                    }
                }
            }
        }
    }

    if (showRestartConfirmation) {
        ConfirmationDialog(
            title = "Spiel neu starten?",
            message = "Möchtest du das aktuelle Spiel wirklich neu starten?",
            confirmText = "Neu starten",
            dismissText = "Abbrechen",
            onConfirm = {
                restartSameGame()
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
                resetToSelection()
                showExitConfirmation = false
            },
            onDismiss = { showExitConfirmation = false }
        )
    }
}

@Composable
private fun MemoryStartSelection(
    highscores: List<GameHighscore>,
    children: List<Child>,
    selectedChild: Child?,
    isPhone: Boolean,
    onStart: (MemoryDifficulty, PlayerMode) -> Unit
) {
    var selectedDifficulty by remember { mutableStateOf(MemoryDifficulty.EASY) }
    var selectedPlayerMode by remember { mutableStateOf(PlayerMode.ONE_PLAYER) }

    if (isPhone) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Schwierigkeit auswählen",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        selectedDifficulty = MemoryDifficulty.EASY
                    },
                    colors = memorySelectionButtonColors(
                        selected = selectedDifficulty == MemoryDifficulty.EASY
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = "Stufe 1")
                }

                Button(
                    onClick = {
                        selectedDifficulty = MemoryDifficulty.HARD
                    },
                    colors = memorySelectionButtonColors(
                        selected = selectedDifficulty == MemoryDifficulty.HARD
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = "Stufe 2")
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "Spieler auswählen",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        selectedPlayerMode = PlayerMode.ONE_PLAYER
                    },
                    colors = memorySelectionButtonColors(
                        selected = selectedPlayerMode == PlayerMode.ONE_PLAYER
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = "1 Spieler")
                }

                Button(
                    onClick = {
                        selectedPlayerMode = PlayerMode.TWO_PLAYERS
                    },
                    colors = memorySelectionButtonColors(
                        selected = selectedPlayerMode == PlayerMode.TWO_PLAYERS
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = "2 Spieler")
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "${difficultyText(selectedDifficulty)}, ${playerModeText(selectedPlayerMode)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = {
                    onStart(
                        selectedDifficulty,
                        selectedPlayerMode
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Spiel starten")
            }

            Spacer(modifier = Modifier.height(16.dp))

            MemoryHighscoreOverview(
                difficulty = selectedDifficulty,
                highscores = highscores,
                children = children,
                selectedChild = selectedChild,
                compact = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
    } else {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
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

                Spacer(modifier = Modifier.height(20.dp))

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

            MemoryHighscoreOverview(
                difficulty = selectedDifficulty,
                highscores = highscores,
                children = children,
                selectedChild = selectedChild,
                modifier = Modifier.width(260.dp)
            )
        }
    }
}

@Composable
private fun MemoryHighscoreOverview(
    difficulty: MemoryDifficulty,
    highscores: List<GameHighscore>,
    children: List<Child>,
    selectedChild: Child?,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    val level = difficulty.toLunaGameLevel()

    val personalMoves = highscores.bestEntry(
        childId = selectedChild?.id,
        game = LunaGameType.MEMORY,
        scoreType = LunaGameScoreType.ATTEMPTS,
        level = level
    )

    val globalMoves = highscores.bestEntry(
        childId = null,
        game = LunaGameType.MEMORY,
        scoreType = LunaGameScoreType.ATTEMPTS,
        level = level
    )

    val personalTime = highscores.bestEntry(
        childId = selectedChild?.id,
        game = LunaGameType.MEMORY,
        scoreType = LunaGameScoreType.TIME_SECONDS,
        level = level
    )

    val globalTime = highscores.bestEntry(
        childId = null,
        game = LunaGameType.MEMORY,
        scoreType = LunaGameScoreType.TIME_SECONDS,
        level = level
    )

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Highscores",
                style = if (compact) {
                    MaterialTheme.typography.titleMedium
                } else {
                    MaterialTheme.typography.titleLarge
                },
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = difficultyText(difficulty),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(text = "Deine Züge", fontWeight = FontWeight.Bold)
            Text(text = personalMoves?.value?.toString() ?: "-")

            Spacer(modifier = Modifier.height(10.dp))

            Text(text = "App-Züge", fontWeight = FontWeight.Bold)
            Text(
                text = globalMoves?.let {
                    "${it.value} (${childName(it.childId, children)})"
                } ?: "-",
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(text = "Deine Zeit", fontWeight = FontWeight.Bold)
            Text(text = personalTime?.let { formatMemoryTime(it.value.toLong()) } ?: "-")

            Spacer(modifier = Modifier.height(10.dp))

            Text(text = "App-Zeit", fontWeight = FontWeight.Bold)
            Text(
                text = globalTime?.let {
                    "${formatMemoryTime(it.value.toLong())} (${childName(it.childId, children)})"
                } ?: "-",
                textAlign = TextAlign.Center
            )
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
private fun MemoryCompactScorePanel(
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
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (!finished) {
                    if (playerMode == PlayerMode.ONE_PLAYER) {
                        "Züge: $moves · Zeit: ${formatMemoryTime(elapsedSeconds)}"
                    } else {
                        "Spieler $currentPlayer ist dran"
                    }
                } else {
                    "Gewonnen: ${winnerText(playerMode, playerOneScore, playerTwoScore)}"
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (finished) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            if (playerMode == PlayerMode.TWO_PLAYERS) {
                Text(
                    text = "Spieler 1: $playerOneScore · Spieler 2: $playerTwoScore",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            } else {
                Text(
                    text = "Paare: $playerOneScore",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            }

            if (finished) {
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onRestart,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = "Neu")
                    }

                    Button(
                        onClick = onExit,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = "Verlassen")
                    }
                }
            }
        }
    }
}

@Composable
private fun MemoryScorePanel(
    playerMode: PlayerMode,
    difficulty: MemoryDifficulty,
    highscores: List<GameHighscore>,
    children: List<Child>,
    selectedChild: Child?,
    currentPlayer: Int,
    playerOneScore: Int,
    playerTwoScore: Int,
    moves: Int,
    elapsedSeconds: Long,
    finished: Boolean,
    onRestart: () -> Unit,
    onExit: () -> Unit
) {
    val level = difficulty.toLunaGameLevel()

    val personalMoves = highscores.bestEntry(
        childId = selectedChild?.id,
        game = LunaGameType.MEMORY,
        scoreType = LunaGameScoreType.ATTEMPTS,
        level = level
    )

    val globalMoves = highscores.bestEntry(
        childId = null,
        game = LunaGameType.MEMORY,
        scoreType = LunaGameScoreType.ATTEMPTS,
        level = level
    )

    val personalTime = highscores.bestEntry(
        childId = selectedChild?.id,
        game = LunaGameType.MEMORY,
        scoreType = LunaGameScoreType.TIME_SECONDS,
        level = level
    )

    val globalTime = highscores.bestEntry(
        childId = null,
        game = LunaGameType.MEMORY,
        scoreType = LunaGameScoreType.TIME_SECONDS,
        level = level
    )

    Card(
        modifier = Modifier
            .width(220.dp)
            .height(520.dp),
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
            Text(
                text = if (!finished) "Spiel läuft" else "Gewonnen",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (!finished) {
                    if (playerMode == PlayerMode.ONE_PLAYER) "Solo" else "Spieler $currentPlayer"
                } else {
                    winnerText(
                        playerMode = playerMode,
                        playerOneScore = playerOneScore,
                        playerTwoScore = playerTwoScore
                    )
                },
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(text = "Züge", fontWeight = FontWeight.Bold)
            Text(text = moves.toString(), style = MaterialTheme.typography.headlineSmall)

            Spacer(modifier = Modifier.height(8.dp))

            Text(text = "Zeit", fontWeight = FontWeight.Bold)
            Text(text = formatMemoryTime(elapsedSeconds), style = MaterialTheme.typography.headlineSmall)

            Spacer(modifier = Modifier.height(12.dp))

            Text(text = "Highscore", fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(6.dp))

            Text(text = "Meine Züge: ${personalMoves?.value?.toString() ?: "-"}")
            Text(
                text = "Alle Züge: ${
                    globalMoves?.let { "${it.value} (${childName(it.childId, children)})" } ?: "-"
                }",
                textAlign = TextAlign.Center
            )

            Text(
                text = "Meine Zeit: ${
                    personalTime?.let { formatMemoryTime(it.value.toLong()) } ?: "-"
                }"
            )
            Text(
                text = "Alle Zeit: ${
                    globalTime?.let { "${formatMemoryTime(it.value.toLong())} (${childName(it.childId, children)})" } ?: "-"
                }",
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (playerMode == PlayerMode.TWO_PLAYERS) {
                Text(
                    text = "Spieler 1: $playerOneScore",
                    fontWeight = if (currentPlayer == 1 && !finished) FontWeight.Bold else FontWeight.Normal
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Spieler 2: $playerTwoScore",
                    fontWeight = if (currentPlayer == 2 && !finished) FontWeight.Bold else FontWeight.Normal
                )
            } else {
                Text(
                    text = "Paare: $playerOneScore",
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            if (finished) {
                Button(onClick = onRestart) {
                    Text(text = "Neu starten")
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(onClick = onExit) {
                    Text(text = "Verlassen")
                }
            }
        }
    }
}

private fun MemoryDifficulty.toLunaGameLevel(): LunaGameLevel {
    return when (this) {
        MemoryDifficulty.EASY -> LunaGameLevel.EASY
        MemoryDifficulty.HARD -> LunaGameLevel.HARD
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
    isCompact: Boolean = false,
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
                style = if (isCompact) {
                    MaterialTheme.typography.headlineMedium
                } else {
                    MaterialTheme.typography.displaySmall
                },
                fontWeight = FontWeight.Bold
            )
        }
    }
}