package de.meson_labs.luna_coin.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.meson_labs.luna_coin.R
import de.meson_labs.luna_coin.components.LunaScreenHeader
import de.meson_labs.luna_coin.models.Child
import de.meson_labs.luna_coin.games.LunaNumberGuessGameScreen
import de.meson_labs.luna_coin.games.LunaMemoryGameScreen

private enum class ActiveGame {
    NONE,
    MEMORY,
    NUMBER_GUESS
}

@Composable
fun LunaGamesScreen(
    modifier: Modifier = Modifier,
    selectedChild: Child?,
    onLogout: () -> Unit
) {
    var activeGame by remember { mutableStateOf(ActiveGame.NONE) }

    when (activeGame) {
        ActiveGame.MEMORY -> {
            LunaMemoryGameScreen(
                modifier = modifier,
                selectedChild = selectedChild,
                onLogout = onLogout,
                onBack = { activeGame = ActiveGame.NONE }
            )
        }

        ActiveGame.NUMBER_GUESS -> {
            LunaNumberGuessGameScreen(
                modifier = modifier,
                selectedChild = selectedChild,
                onLogout = onLogout,
                onBack = { activeGame = ActiveGame.NONE }
            )
        }

        ActiveGame.NONE -> {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp)
            ) {
                LunaScreenHeader(
                    title = "Luna-Games",
                    selectedChild = selectedChild,
                    onLogout = onLogout
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Minispiele",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                MiniGameGrid(
                    games = listOf(
                        MiniGameItem(
                            title = "Memory",
                            description = "Paare finden",
                            onClick = { activeGame = ActiveGame.MEMORY }
                        ),
                        MiniGameItem(
                            title = "Zahlenraten",
                            description = "Zahl erraten",
                            onClick = { activeGame = ActiveGame.NUMBER_GUESS }
                        )
                    )
                )

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "Coming soon",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                ComingSoonGrid()
            }
        }
    }
}

private data class MiniGameItem(
    val title: String,
    val description: String,
    val onClick: () -> Unit
)

@Composable
private fun MiniGameGrid(
    games: List<MiniGameItem>
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        games.chunked(3).forEach { rowGames ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowGames.forEach { game ->
                    MiniGameCard(
                        title = game.title,
                        description = game.description,
                        onClick = game.onClick,
                        modifier = Modifier.weight(1f)
                    )
                }

                repeat(3 - rowGames.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun MiniGameCard(
    title: String,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(140.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Button(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Start")
            }
        }
    }
}

@Composable
private fun ComingSoonGrid() {
    val images = listOf(
        R.drawable.cyberluna,
        R.drawable.gtl,
        R.drawable.lunarim,
        R.drawable.reddeadluna,
        R.drawable.witcher_luna
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        images.chunked(3).forEach { rowImages ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowImages.forEach { imageRes ->
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(350.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceBright
                        )
                    ) {
                        Image(
                            painter = painterResource(id = imageRes),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    }
                }

                repeat(3 - rowImages.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}