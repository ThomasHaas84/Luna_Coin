package de.meson_labs.luna_coin.screens

import androidx.compose.foundation.Image
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.meson_labs.luna_coin.R
import de.meson_labs.luna_coin.components.CoinDisplay
import de.meson_labs.luna_coin.models.Child
import de.meson_labs.luna_coin.models.LunaItemCatalog
import de.meson_labs.luna_coin.models.UserRole
import androidx.compose.ui.draw.scale

@Composable
fun UserSelectionScreen(
    children: List<Child>,
    onChildSelected: (String) -> Unit
) {
    var passwordUser by remember {
        mutableStateOf<Child?>(null)
    }

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
            text = "Wer bist du?",
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
                        if (child.password.isBlank()) {
                            onChildSelected(child.id)
                        } else {
                            passwordUser = child
                        }
                    }
                )
            }
        }
    }

    passwordUser?.let { child ->
        PasswordDialog(
            child = child,
            onCancel = {
                passwordUser = null
            },
            onSuccess = {
                passwordUser = null
                onChildSelected(child.id)
            }
        )
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
        UserRole.CHILD -> MaterialTheme.colorScheme.onSurface
        UserRole.PARENT -> Color.White
        UserRole.ADMIN -> Color.White
    }

    val profileImageRes = if (child.hasProfileImage) {
        child.profileImageItem?.let { item ->
            LunaItemCatalog.getDefinition(item).lunaImageRes
        } ?: R.drawable.luna_dog
    } else {
        null
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
                if (profileImageRes != null) {
                    Image(
                        painter = painterResource(id = profileImageRes),
                        contentDescription = child.name,
                        modifier = Modifier
                            .fillMaxSize()
                            .scale(1.4f),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Text(
                        text = child.name.firstOrNull()?.uppercase() ?: "?",
                        style = MaterialTheme.typography.displayLarge,
                        color = textColor
                    )
                }
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
                    amount = child.coins,
                    coinSize = 48.dp
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

@Composable
private fun PasswordDialog(
    child: Child,
    onCancel: () -> Unit,
    onSuccess: () -> Unit
) {
    var passwordInput by remember {
        mutableStateOf("")
    }

    var showError by remember {
        mutableStateOf(false)
    }

    AlertDialog(
        onDismissRequest = onCancel,
        title = {
            Text("${child.name} anmelden")
        },
        text = {
            Column {
                OutlinedTextField(
                    value = passwordInput,
                    onValueChange = {
                        passwordInput = it
                        showError = false
                    },
                    label = {
                        Text("Passwort")
                    },
                    visualTransformation = PasswordVisualTransformation()
                )

                if (showError) {
                    Text(
                        text = "Passwort falsch",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(
                            top = 8.dp
                        )
                    )
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onCancel
            ) {
                Text("Abbrechen")
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (passwordInput == child.password) {
                        onSuccess()
                    } else {
                        showError = true
                        passwordInput = ""
                    }
                }
            ) {
                Text("Anmelden")
            }
        }
    )
}