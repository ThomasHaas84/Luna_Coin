package de.meson_labs.luna_coin.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.meson_labs.luna_coin.R
import de.meson_labs.luna_coin.components.LunaScreenHeader
import de.meson_labs.luna_coin.games.LunaMemoryGameScreen
import de.meson_labs.luna_coin.games.LunaMultiplicationGameScreen
import de.meson_labs.luna_coin.games.LunaNumberGuessGameScreen
import de.meson_labs.luna_coin.models.Child
import de.meson_labs.luna_coin.viewmodel.LunaCoinViewModel

private enum class ActiveGame {
    NONE,
    MEMORY,
    NUMBER_GUESS,
    MULTIPLICATION
}

@Composable
fun LunaGamesScreen(
    modifier: Modifier = Modifier,
    selectedChild: Child?,
    viewModel: LunaCoinViewModel,
    onLogout: () -> Unit
) {
    var activeGame by remember { mutableStateOf(ActiveGame.NONE) }

    when (activeGame) {
        ActiveGame.MEMORY -> {
            LunaMemoryGameScreen(
                modifier = modifier,
                selectedChild = selectedChild,
                viewModel = viewModel,
                onLogout = onLogout,
                onBack = { activeGame = ActiveGame.NONE }
            )
        }

        ActiveGame.NUMBER_GUESS -> {
            LunaNumberGuessGameScreen(
                modifier = modifier,
                selectedChild = selectedChild,
                viewModel = viewModel,
                onLogout = onLogout,
                onBack = { activeGame = ActiveGame.NONE }
            )
        }

        ActiveGame.MULTIPLICATION -> {
            LunaMultiplicationGameScreen(
                modifier = modifier,
                selectedChild = selectedChild,
                viewModel = viewModel,
                onLogout = onLogout,
                onBack = { activeGame = ActiveGame.NONE }
            )
        }

        ActiveGame.NONE -> {
            val configuration = LocalConfiguration.current
            val isTabletLayout = configuration.smallestScreenWidthDp >= 600
            val isPhone = !isTabletLayout

            val screenPadding = if (isPhone) 14.dp else 24.dp
            val sectionSpacing = if (isPhone) 18.dp else 32.dp
            val cardSpacing = if (isPhone) 10.dp else 12.dp

            Column(
                modifier = modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(screenPadding)
            ) {
                LunaScreenHeader(
                    title = "Luna-Games",
                    selectedChild = selectedChild,
                    onLogout = onLogout
                )

                Spacer(modifier = Modifier.height(if (isPhone) 16.dp else 24.dp))

                Text(
                    text = "Minispiele:",
                    style = if (isPhone) {
                        MaterialTheme.typography.titleLarge
                    } else {
                        MaterialTheme.typography.titleLarge
                    },
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                MiniGameGrid(
                    games = listOf(
                        MiniGameItem(
                            title = "Memory",
                            description = "Paare finden",
                            symbol = "🧠",
                            onClick = { activeGame = ActiveGame.MEMORY }
                        ),
                        MiniGameItem(
                            title = "Zahlenraten",
                            description = "Zahl erraten",
                            symbol = "🔢",
                            onClick = { activeGame = ActiveGame.NUMBER_GUESS }
                        ),
                        MiniGameItem(
                            title = "1 x 1",
                            description = "10 Felder lösen",
                            symbol = "✖️",
                            onClick = { activeGame = ActiveGame.MULTIPLICATION }
                        )
                    ),
                    columns = if (isPhone) 1 else 3,
                    spacing = cardSpacing,
                    isPhone = isPhone
                )

                Spacer(modifier = Modifier.height(sectionSpacing))

                Text(
                    text = "Coming soon:",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                ComingSoonGrid(
                    columns = if (isPhone) 2 else 3,
                    spacing = cardSpacing,
                    isPhone = isPhone
                )
            }
        }
    }
}

private data class MiniGameItem(
    val title: String,
    val description: String,
    val symbol: String,
    val onClick: () -> Unit
)

@Composable
private fun MiniGameGrid(
    games: List<MiniGameItem>,
    columns: Int,
    spacing: androidx.compose.ui.unit.Dp,
    isPhone: Boolean
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(spacing)
    ) {
        games.chunked(columns).forEach { rowGames ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing)
            ) {
                rowGames.forEach { game ->
                    MiniGameCard(
                        title = game.title,
                        description = game.description,
                        symbol = game.symbol,
                        onClick = game.onClick,
                        isPhone = isPhone,
                        modifier = Modifier.weight(1f)
                    )
                }

                repeat(columns - rowGames.size) {
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
    symbol: String,
    onClick: () -> Unit,
    isPhone: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(if (isPhone) 118.dp else 160.dp),
        shape = RoundedCornerShape(if (isPhone) 16.dp else 18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(if (isPhone) 10.dp else 10.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = title,
                        style = if (isPhone) {
                            MaterialTheme.typography.titleMedium
                        } else {
                            MaterialTheme.typography.titleLarge
                        },
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Text(
                    text = symbol,
                    fontSize = if (isPhone) 34.sp else 42.sp
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
private fun ComingSoonGrid(
    columns: Int,
    spacing: androidx.compose.ui.unit.Dp,
    isPhone: Boolean
) {
    val images = listOf(
        R.drawable.cyberluna,
        R.drawable.gtl,
        R.drawable.lunarim,
        R.drawable.reddeadluna,
        R.drawable.witcher_luna
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(spacing)
    ) {
        images.chunked(columns).forEach { rowImages ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing)
            ) {
                rowImages.forEach { imageRes ->
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(if (isPhone) 190.dp else 350.dp),
                        shape = RoundedCornerShape(if (isPhone) 14.dp else 18.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                        )
                    ) {
                        Image(
                            painter = painterResource(id = imageRes),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(if (isPhone) 4.dp else 6.dp),
                            contentScale = ContentScale.Fit
                        )
                    }
                }

                repeat(columns - rowImages.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}