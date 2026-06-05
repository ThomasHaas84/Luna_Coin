package de.meson_labs.luna_coin.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.meson_labs.luna_coin.components.CoinDisplay
import de.meson_labs.luna_coin.models.Child
import de.meson_labs.luna_coin.models.UserRole

@Composable
fun UserSelectionScreen(
    children: List<Child>,
    onChildSelected: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Luna Coin",
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            text = "Benutzer auswählen:",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(
                top = 8.dp,
                bottom = 32.dp
            )
        )

        LazyVerticalGrid(
            columns = GridCells.Adaptive(
                minSize = 150.dp
            ),
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            items(children) { child ->
                UserProfileCard(
                    child = child,
                    onClick = {
                        onChildSelected(child.id)
                    }
                )
            }
        }
    }
}

@Composable
private fun UserProfileCard(
    child: Child,
    onClick: () -> Unit
) {
    val cardColor = when (child.role) {
        UserRole.CHILD -> MaterialTheme.colorScheme.surface

        UserRole.PARENT -> Color(
            0xFF5B21B6
        )

        UserRole.ADMIN -> Color(
            0xFF0B3D20
        )
    }

    val textColor = when (child.role) {
        UserRole.CHILD ->
            MaterialTheme.colorScheme.onSurface

        UserRole.PARENT ->
            Color.White

        UserRole.ADMIN ->
            Color.White
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable {
            onClick()
        }
    ) {
        Card(
            modifier = Modifier.aspectRatio(1f),
            colors = CardDefaults.cardColors(
                containerColor = cardColor
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 8.dp
            ),
            shape = RoundedCornerShape(
                24.dp
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(cardColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = child.name
                        .firstOrNull()
                        ?.uppercase()
                        ?: "?",
                    style = MaterialTheme.typography.displayLarge,
                    color = textColor
                )
            }
        }

        Text(
            text = child.name,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(
                top = 10.dp
            )
        )

        when (child.role) {
            UserRole.CHILD -> {
                CoinDisplay(
                    amount = child.coins
                )
            }

            UserRole.PARENT -> {
                Text(
                    text = "Eltern",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
            }

            UserRole.ADMIN -> {
                Text(
                    text = "Administrator",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.tertiary,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}