package de.meson_labs.luna_coin.screens

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import coil.compose.AsyncImage
import de.meson_labs.luna_coin.R
import de.meson_labs.luna_coin.components.CoinDisplay
import de.meson_labs.luna_coin.models.Child
import de.meson_labs.luna_coin.models.LunaItemCatalog
import de.meson_labs.luna_coin.models.UserRole
import de.meson_labs.luna_coin.screens.image_mode.LunaImageModeConfig
import de.meson_labs.luna_coin.screens.image_mode.LunaImageModeStorage
import kotlinx.coroutines.delay

@Composable
fun UserSelectionScreen(
    children: List<Child>,
    onChildSelected: (String) -> Unit
) {
    var passwordUser by remember {
        mutableStateOf<Child?>(null)
    }

    var imageModeActive by remember {
        mutableStateOf(false)
    }

    var idleResetKey by remember {
        mutableLongStateOf(System.currentTimeMillis())
    }

    val hasThomas = children.any { child ->
        child.name.equals(
            other = "Thomas",
            ignoreCase = true
        )
    }

    LaunchedEffect(
        idleResetKey,
        imageModeActive,
        passwordUser
    ) {
        if (!imageModeActive && passwordUser == null) {
            delay(LunaImageModeConfig.AUTO_START_DELAY_MS)
            imageModeActive = true
        }
    }

    if (imageModeActive) {
        LunaImageModeScreen(
            onExit = {
                imageModeActive = false
                idleResetKey = System.currentTimeMillis()
            }
        )

        return
    }

    Box(
        modifier = Modifier.fillMaxSize()
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
                            idleResetKey = System.currentTimeMillis()

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

        if (hasThomas) {
            Button(
                onClick = {
                    imageModeActive = true
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(24.dp)
            ) {
                Text("Bild-Modus starten")
            }
        }
    }

    passwordUser?.let { child ->
        PasswordDialog(
            child = child,
            onCancel = {
                passwordUser = null
                idleResetKey = System.currentTimeMillis()
            },
            onSuccess = {
                passwordUser = null
                onChildSelected(child.id)
            }
        )
    }
}

@Composable
private fun LunaImageModeScreen(
    onExit: () -> Unit
) {
    val context = LocalContext.current
    val view = LocalView.current
    val activity = context.findActivity()

    val images = remember {
        LunaImageModeStorage.getImageFiles(
            context = context
        )
    }

    var currentImageIndex by remember {
        mutableIntStateOf(0)
    }

    DisposableEffect(Unit) {
        val window = activity?.window

        if (window != null) {
            WindowCompat.setDecorFitsSystemWindows(
                window,
                false
            )

            val controller = WindowInsetsControllerCompat(
                window,
                view
            )

            controller.hide(
                WindowInsetsCompat.Type.systemBars()
            )

            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        onDispose {
            if (window != null) {
                val controller = WindowInsetsControllerCompat(
                    window,
                    view
                )

                controller.show(
                    WindowInsetsCompat.Type.systemBars()
                )

                WindowCompat.setDecorFitsSystemWindows(
                    window,
                    true
                )
            }
        }
    }

    LaunchedEffect(images) {
        while (true) {
            delay(LunaImageModeConfig.IMAGE_CHANGE_DELAY_MS)

            if (images.isNotEmpty()) {
                currentImageIndex = (currentImageIndex + 1) % images.size
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable {
                onExit()
            },
        contentAlignment = Alignment.Center
    ) {
        if (images.isNotEmpty()) {
            AsyncImage(
                model = images[currentImageIndex],
                contentDescription = "Bild-Modus",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        } else {
            Text(
                text = "Keine Bilder im Ordner Bilderrahmen gefunden",
                color = Color.White,
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(24.dp)
            )
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
                        painter = androidx.compose.ui.res.painterResource(id = profileImageRes),
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

private fun Context.findActivity(): Activity? {
    var currentContext = this

    while (currentContext is ContextWrapper) {
        if (currentContext is Activity) {
            return currentContext
        }

        currentContext = currentContext.baseContext
    }

    return null
}