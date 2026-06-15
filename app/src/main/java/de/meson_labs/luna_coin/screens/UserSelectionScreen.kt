package de.meson_labs.luna_coin.screens

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.view.WindowManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.input.KeyboardType
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
import de.meson_labs.luna_coin.screens.image_mode.LunaImagePlayMode
import de.meson_labs.luna_coin.viewmodel.LunaCoinViewModel
import kotlinx.coroutines.delay

@Composable
fun UserSelectionScreen(
    viewModel: LunaCoinViewModel,
    onChildSelected: (String) -> Unit
) {
    val context = LocalContext.current
    val isLoading by viewModel.isLoading.collectAsState()
    val data by viewModel.data.collectAsState()
    val children = data.children

    var passwordUser by remember { mutableStateOf<Child?>(null) }
    var imageModeActive by remember { mutableStateOf(false) }
    var showImageSettingsDialog by remember { mutableStateOf(false) }

    var imageChangeDelayMs by remember {
        mutableLongStateOf(LunaImageModeStorage.getImageChangeDelayMs(context))
    }
    var imagePlayMode by remember {
        mutableStateOf(LunaImageModeStorage.getPlayMode(context))
    }
    var idleResetKey by remember { mutableLongStateOf(System.currentTimeMillis()) }

    val hasThomas = children.any { it.name.equals("Thomas", ignoreCase = true) }

    // Auto-start Bild-Modus
    LaunchedEffect(idleResetKey, imageModeActive, passwordUser, showImageSettingsDialog) {
        if (!imageModeActive && passwordUser == null && !showImageSettingsDialog) {
            delay(LunaImageModeConfig.AUTO_START_DELAY_MS)
            imageModeActive = true
        }
    }

    if (imageModeActive) {
        LunaImageModeScreen(
            imageChangeDelayMs = imageChangeDelayMs,
            imagePlayMode = imagePlayMode,
            onExit = {
                imageModeActive = false
                idleResetKey = System.currentTimeMillis()
            }
        )
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (isLoading) {
            // Lade-Indikator
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 6.dp
                )
                Text(
                    text = "Daten werden geladen...",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(top = 24.dp)
                )
            }
        } else {
            // Normale Benutzerauswahl
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
                    modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
                )

                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 150.dp),
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
        }

        // Admin-Buttons
        if (hasThomas && !isLoading) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(onClick = { imageModeActive = true }) {
                    Text("Bild-Modus starten")
                }

                Spacer(modifier = Modifier.width(12.dp))

                Button(onClick = {
                    showImageSettingsDialog = true
                    idleResetKey = System.currentTimeMillis()
                }) {
                    Text("⚙")
                }
            }
        }
    }

    // Dialoge
    if (showImageSettingsDialog) {
        ImageModeSettingsDialog(
            currentDelayMs = imageChangeDelayMs,
            currentPlayMode = imagePlayMode,
            onCancel = {
                showImageSettingsDialog = false
                idleResetKey = System.currentTimeMillis()
            },
            onSave = { newDelayMs, newPlayMode ->
                imageChangeDelayMs = newDelayMs
                imagePlayMode = newPlayMode

                LunaImageModeStorage.setImageChangeDelayMs(context, newDelayMs)
                LunaImageModeStorage.setPlayMode(context, newPlayMode)

                showImageSettingsDialog = false
                idleResetKey = System.currentTimeMillis()
            }
        )
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

// ====================== UNVERÄNDERTE FUNKTIONEN ======================

@Composable
private fun LunaImageModeScreen(
    imageChangeDelayMs: Long,
    imagePlayMode: LunaImagePlayMode,
    onExit: () -> Unit
) {
    val context = LocalContext.current
    val view = LocalView.current
    val activity = context.findActivity()

    val images = remember {
        LunaImageModeStorage.getImageFiles(context)
    }

    var currentImageIndex by remember { mutableIntStateOf(0) }
    var autoPlayActive by remember { mutableStateOf(true) }
    var showExitButton by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        val window = activity?.window
        if (window != null) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            WindowCompat.setDecorFitsSystemWindows(window, false)

            val controller = WindowInsetsControllerCompat(window, view)
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        onDispose {
            val window = activity?.window
            if (window != null) {
                window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                val controller = WindowInsetsControllerCompat(window, view)
                controller.show(WindowInsetsCompat.Type.systemBars())
                WindowCompat.setDecorFitsSystemWindows(window, true)
            }
        }
    }

    LaunchedEffect(images, imageChangeDelayMs, imagePlayMode, autoPlayActive) {
        if (autoPlayActive) {
            while (true) {
                delay(imageChangeDelayMs)
                if (images.isNotEmpty()) {
                    currentImageIndex = getNextImageIndex(currentImageIndex, images.size, imagePlayMode)
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(autoPlayActive, images) {
                detectTapGestures { offset ->
                    if (autoPlayActive) {
                        autoPlayActive = false
                        showExitButton = true
                    } else {
                        if (images.isNotEmpty()) {
                            currentImageIndex = if (offset.x < size.width / 2f) {
                                if (currentImageIndex == 0) images.lastIndex else currentImageIndex - 1
                            } else {
                                (currentImageIndex + 1) % images.size
                            }
                        }
                    }
                }
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

        if (showExitButton) {
            Column(
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(onClick = { autoPlayActive = true; showExitButton = false }) {
                    Text("Autoplay fortsetzen")
                }
                Button(onClick = onExit, modifier = Modifier.padding(top = 12.dp)) {
                    Text("Bild-Modus verlassen")
                }
            }
        }
    }
}

private fun getNextImageIndex(
    currentImageIndex: Int,
    imageCount: Int,
    imagePlayMode: LunaImagePlayMode
): Int {
    if (imageCount <= 0) return 0

    return when (imagePlayMode) {
        LunaImagePlayMode.SEQUENTIAL -> (currentImageIndex + 1) % imageCount
        LunaImagePlayMode.RANDOM -> {
            if (imageCount == 1) 0
            else {
                var nextIndex: Int
                do {
                    nextIndex = (0 until imageCount).random()
                } while (nextIndex == currentImageIndex)
                nextIndex
            }
        }
    }
}

@Composable
private fun ImageModeSettingsDialog(
    currentDelayMs: Long,
    currentPlayMode: LunaImagePlayMode,
    onCancel: () -> Unit,
    onSave: (Long, LunaImagePlayMode) -> Unit
) {
    var secondsInput by remember { mutableStateOf((currentDelayMs / 1000L).toString()) }
    var selectedPlayMode by remember { mutableStateOf(currentPlayMode) }
    var showError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text("Bild-Modus Einstellungen") },
        text = {
            Column {
                OutlinedTextField(
                    value = secondsInput,
                    onValueChange = {
                        secondsInput = it.filter { char -> char.isDigit() }
                        showError = false
                    },
                    label = { Text("Dauer pro Bild in Sekunden") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                Text(
                    text = "Bildreihenfolge",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 20.dp, bottom = 8.dp)
                )

                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { selectedPlayMode = LunaImagePlayMode.SEQUENTIAL }) {
                    RadioButton(selected = selectedPlayMode == LunaImagePlayMode.SEQUENTIAL, onClick = { selectedPlayMode = LunaImagePlayMode.SEQUENTIAL })
                    Text("Der Reihe nach")
                }

                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { selectedPlayMode = LunaImagePlayMode.RANDOM }) {
                    RadioButton(selected = selectedPlayMode == LunaImagePlayMode.RANDOM, onClick = { selectedPlayMode = LunaImagePlayMode.RANDOM })
                    Text("Zufällig")
                }

                if (showError) {
                    Text(
                        text = "Bitte einen Wert von ${LunaImageModeConfig.MIN_IMAGE_CHANGE_DELAY_SECONDS} bis ${LunaImageModeConfig.MAX_IMAGE_CHANGE_DELAY_SECONDS} Sekunden eingeben.",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        },
        dismissButton = { TextButton(onClick = onCancel) { Text("Abbrechen") } },
        confirmButton = {
            TextButton(onClick = {
                val seconds = secondsInput.toLongOrNull()
                if (seconds == null || seconds < LunaImageModeConfig.MIN_IMAGE_CHANGE_DELAY_SECONDS || seconds > LunaImageModeConfig.MAX_IMAGE_CHANGE_DELAY_SECONDS) {
                    showError = true
                } else {
                    onSave(seconds * 1000L, selectedPlayMode)
                }
            }) {
                Text("Speichern")
            }
        }
    )
}

@Composable
private fun UserProfileCard(
    child: Child,
    onClick: () -> Unit
) {
    val cardColor = when (child.role) {
        UserRole.CHILD -> MaterialTheme.colorScheme.surface
        UserRole.PARENT -> Color(0xFF5B21B6)
        UserRole.ADMIN -> Color(0xFF0B3D20)
    }

    val textColor = when (child.role) {
        UserRole.CHILD -> MaterialTheme.colorScheme.onSurface
        else -> Color.White
    }

    val profileImageRes = if (child.hasProfileImage) {
        child.profileImageItem?.let { LunaItemCatalog.getDefinition(it).lunaImageRes } ?: R.drawable.luna_dog
    } else null

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Card(
            modifier = Modifier.aspectRatio(1f),
            colors = CardDefaults.cardColors(containerColor = cardColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            shape = RoundedCornerShape(24.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize().background(cardColor),
                contentAlignment = Alignment.Center
            ) {
                if (profileImageRes != null) {
                    Image(
                        painter = androidx.compose.ui.res.painterResource(id = profileImageRes),
                        contentDescription = child.name,
                        modifier = Modifier.fillMaxSize().scale(1.4f),
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
            modifier = Modifier.padding(top = 10.dp)
        )

        when (child.role) {
            UserRole.CHILD -> CoinDisplay(amount = child.coins, coinSize = 48.dp)
            UserRole.PARENT -> Text("Eltern", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
            UserRole.ADMIN -> Text("Administrator", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.tertiary)
        }
    }
}

@Composable
private fun PasswordDialog(
    child: Child,
    onCancel: () -> Unit,
    onSuccess: () -> Unit
) {
    var passwordInput by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text("${child.name} anmelden") },
        text = {
            Column {
                OutlinedTextField(
                    value = passwordInput,
                    onValueChange = {
                        passwordInput = it
                        showError = false
                    },
                    label = { Text("Passwort") },
                    visualTransformation = PasswordVisualTransformation()
                )

                if (showError) {
                    Text(
                        text = "Passwort falsch",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        },
        dismissButton = { TextButton(onClick = onCancel) { Text("Abbrechen") } },
        confirmButton = {
            TextButton(onClick = {
                if (passwordInput == child.password) {
                    onSuccess()
                } else {
                    showError = true
                    passwordInput = ""
                }
            }) {
                Text("Anmelden")
            }
        }
    )
}

private fun Context.findActivity(): Activity? {
    var currentContext = this
    while (currentContext is ContextWrapper) {
        if (currentContext is Activity) return currentContext
        currentContext = currentContext.baseContext
    }
    return null
}