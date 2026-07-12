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
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.meson_labs.luna_coin.R
import de.meson_labs.luna_coin.components.LunaScreenHeader
import de.meson_labs.luna_coin.games.LunaMemoryGameScreen
import de.meson_labs.luna_coin.games.LunaMultiplicationGameScreen
import de.meson_labs.luna_coin.games.LunaNumberGuessGameScreen
import de.meson_labs.luna_coin.games.LunaWordGuessGameScreen
import de.meson_labs.luna_coin.models.Child
import de.meson_labs.luna_coin.tools.LunaPasswordGenerator
import de.meson_labs.luna_coin.viewmodel.LunaCoinViewModel

private enum class LunaGamesDestination {
    NONE,
    MEMORY,
    NUMBER_GUESS,
    MULTIPLICATION,
    WORD_GUESS,
    PASSWORD_GENERATOR
}

private enum class GamesToolsTab {
    GAMES,
    TOOLS
}

@Composable
fun LunaGamesScreen(
    modifier: Modifier = Modifier,
    selectedChild: Child?,
    viewModel: LunaCoinViewModel,
    onLogout: () -> Unit
) {
    var activeGame by remember { mutableStateOf(LunaGamesDestination.NONE) }
    var selectedTab by remember { mutableStateOf(GamesToolsTab.GAMES) }

    when (activeGame) {
        LunaGamesDestination.MEMORY -> {
            LunaMemoryGameScreen(
                modifier = modifier,
                selectedChild = selectedChild,
                viewModel = viewModel,
                onLogout = onLogout,
                onBack = { activeGame = LunaGamesDestination.NONE }
            )
        }

        LunaGamesDestination.NUMBER_GUESS -> {
            LunaNumberGuessGameScreen(
                modifier = modifier,
                selectedChild = selectedChild,
                viewModel = viewModel,
                onLogout = onLogout,
                onBack = { activeGame = LunaGamesDestination.NONE }
            )
        }

        LunaGamesDestination.MULTIPLICATION -> {
            LunaMultiplicationGameScreen(
                modifier = modifier,
                selectedChild = selectedChild,
                viewModel = viewModel,
                onLogout = onLogout,
                onBack = { activeGame = LunaGamesDestination.NONE }
            )
        }

        LunaGamesDestination.WORD_GUESS -> {
            LunaWordGuessGameScreen(
                modifier = modifier,
                selectedChild = selectedChild,
                viewModel = viewModel,
                onLogout = onLogout,
                onBack = { activeGame = LunaGamesDestination.NONE }
            )
        }

        LunaGamesDestination.PASSWORD_GENERATOR -> {
            PasswordGeneratorScreen(
                modifier = modifier,
                selectedChild = selectedChild,
                onLogout = onLogout,
                onBack = { activeGame = LunaGamesDestination.NONE }
            )
        }

        LunaGamesDestination.NONE -> {
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
                    title = "Games & Tools",
                    selectedChild = selectedChild,
                    onLogout = onLogout
                )

                Spacer(modifier = Modifier.height(if (isPhone) 12.dp else 14.dp))

                GamesToolsTabSelector(
                    selectedTab = selectedTab,
                    isPhone = isPhone,
                    onTabSelected = { selectedTab = it }
                )

                Spacer(modifier = Modifier.height(if (isPhone) 12.dp else 16.dp))

                when (selectedTab) {
                    GamesToolsTab.GAMES -> GamesContent(
                        isPhone = isPhone,
                        cardSpacing = cardSpacing,
                        sectionSpacing = sectionSpacing,
                        onGameSelected = { activeGame = it }
                    )

                    GamesToolsTab.TOOLS -> ToolsContent(
                        isPhone = isPhone,
                        cardSpacing = cardSpacing,
                        onPasswordGeneratorSelected = {
                            activeGame = LunaGamesDestination.PASSWORD_GENERATOR
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun GamesToolsTabSelector(
    selectedTab: GamesToolsTab,
    isPhone: Boolean,
    onTabSelected: (GamesToolsTab) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (selectedTab == GamesToolsTab.GAMES) {
            Button(
                onClick = { onTabSelected(GamesToolsTab.GAMES) },
                modifier = Modifier
                    .weight(1f)
                    .height(if (isPhone) 48.dp else 42.dp)
            ) {
                Text(
                    text = "Games",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
        } else {
            OutlinedButton(
                onClick = { onTabSelected(GamesToolsTab.GAMES) },
                modifier = Modifier
                    .weight(1f)
                    .height(if (isPhone) 48.dp else 42.dp)
            ) {
                Text(
                    text = "Games",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        if (selectedTab == GamesToolsTab.TOOLS) {
            Button(
                onClick = { onTabSelected(GamesToolsTab.TOOLS) },
                modifier = Modifier
                    .weight(1f)
                    .height(if (isPhone) 48.dp else 42.dp)
            ) {
                Text(
                    text = "Tools",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
        } else {
            OutlinedButton(
                onClick = { onTabSelected(GamesToolsTab.TOOLS) },
                modifier = Modifier
                    .weight(1f)
                    .height(if (isPhone) 48.dp else 42.dp)
            ) {
                Text(
                    text = "Tools",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun GamesContent(
    isPhone: Boolean,
    cardSpacing: Dp,
    sectionSpacing: Dp,
    onGameSelected: (LunaGamesDestination) -> Unit
) {
    Text(
        text = "Minispiele:",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold
    )

    Spacer(modifier = Modifier.height(12.dp))

    GameToolGrid(
        items = listOf(
            GameToolItem(
                title = "Memory",
                description = "Paare finden",
                symbol = "🧠",
                buttonText = "Start",
                enabled = true,
                onClick = { onGameSelected(LunaGamesDestination.MEMORY) }
            ),
            GameToolItem(
                title = "Zahlenraten",
                description = "Zahl erraten",
                symbol = "🔢",
                buttonText = "Start",
                enabled = true,
                onClick = { onGameSelected(LunaGamesDestination.NUMBER_GUESS) }
            ),
            GameToolItem(
                title = "1 x 1",
                description = "10 Felder lösen",
                symbol = "✖️",
                buttonText = "Start",
                enabled = true,
                onClick = { onGameSelected(LunaGamesDestination.MULTIPLICATION) }
            ),
            GameToolItem(
                title = "Neun Leben",
                description = "Wort erraten",
                symbol = "❤",
                buttonText = "Start",
                enabled = true,
                onClick = { onGameSelected(LunaGamesDestination.WORD_GUESS) }
            )
        ),
        columns = if (isPhone) 2 else 4,
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

@Composable
private fun ToolsContent(
    isPhone: Boolean,
    cardSpacing: Dp,
    onPasswordGeneratorSelected: () -> Unit
) {
    Text(
        text = "Tools:",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold
    )

    Spacer(modifier = Modifier.height(12.dp))

    GameToolGrid(
        items = listOf(
            GameToolItem(
                title = "Passwort-Generator",
                description = "Sichere Passwörter erstellen",
                infoText = LunaPasswordGenerator.getCombinationCountText(),
                symbol = "🔐",
                buttonText = "Öffnen",
                enabled = true,
                onClick = onPasswordGeneratorSelected
            ),
            GameToolItem(
                title = "Coins senden",
                description = "Coins mit Kommentar senden",
                infoText = "",
                symbol = "🪙",
                buttonText = "Bald verfügbar",
                enabled = false,
                onClick = {}
            )
        ),
        columns = if (isPhone) 1 else 4,
        spacing = cardSpacing,
        isPhone = isPhone
    )
}

private data class GameToolItem(
    val title: String,
    val description: String,
    val infoText: String = "",
    val symbol: String,
    val buttonText: String,
    val enabled: Boolean,
    val onClick: () -> Unit
)

@Composable
private fun GameToolGrid(
    items: List<GameToolItem>,
    columns: Int,
    spacing: Dp,
    isPhone: Boolean
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(spacing)
    ) {
        items.chunked(columns).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing)
            ) {
                rowItems.forEach { item ->
                    GameToolCard(
                        title = item.title,
                        description = item.description,
                        infoText = item.infoText,
                        symbol = item.symbol,
                        buttonText = item.buttonText,
                        enabled = item.enabled,
                        onClick = item.onClick,
                        isPhone = isPhone,
                        modifier = Modifier.weight(1f)
                    )
                }

                repeat(columns - rowItems.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun GameToolCard(
    title: String,
    description: String,
    infoText: String,
    symbol: String,
    buttonText: String,
    enabled: Boolean,
    onClick: () -> Unit,
    isPhone: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(if (isPhone) 132.dp else 170.dp),
        shape = RoundedCornerShape(if (isPhone) 16.dp else 18.dp),
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

                    if (infoText.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = infoText,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Text(
                    text = symbol,
                    fontSize = if (isPhone) 34.sp else 42.sp
                )
            }

            Button(
                onClick = onClick,
                enabled = enabled,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = buttonText,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun ComingSoonGrid(
    columns: Int,
    spacing: Dp,
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
