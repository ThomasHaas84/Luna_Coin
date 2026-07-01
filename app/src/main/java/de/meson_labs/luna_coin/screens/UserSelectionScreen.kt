// screens/UserSelectionScreen.kt
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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.material3.Checkbox
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
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
import de.meson_labs.luna_coin.storage.TrustedDeviceStorage
import de.meson_labs.luna_coin.viewmodel.LunaCoinViewModel
import kotlinx.coroutines.delay

@Composable
fun UserSelectionScreen(
    viewModel: LunaCoinViewModel,
    onChildSelected: (String) -> Unit
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current

    val screenWidthDp = configuration.screenWidthDp
    val isPhone = screenWidthDp < 600

    val screenPadding: Dp = when {
        screenWidthDp < 360 -> 14.dp
        screenWidthDp < 420 -> 18.dp
        isPhone -> 22.dp
        else -> 32.dp
    }

    val gridMinSize: Dp = when {
        screenWidthDp < 360 -> 104.dp
        screenWidthDp < 420 -> 118.dp
        isPhone -> 130.dp
        else -> 150.dp
    }

    val gridSpacing: Dp = when {
        screenWidthDp < 420 -> 14.dp
        isPhone -> 18.dp
        else -> 24.dp
    }

    val gridBottomPadding: Dp = if (isPhone) {
        112.dp
    } else {
        96.dp
    }

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

    var autoStartEnabled by remember {
        mutableStateOf(LunaImageModeStorage.isAutoStartEnabled(context))
    }

    var autoStartDelaySeconds by remember {
        mutableLongStateOf(LunaImageModeStorage.getAutoStartDelaySeconds(context))
    }

    var idleResetKey by remember {
        mutableLongStateOf(System.currentTimeMillis())
    }

    val hasBuiltInAdmin = children.any {
        it.isBuiltInAdmin || it.role == UserRole.ADMIN
    }

    LaunchedEffect(
        idleResetKey,
        imageModeActive,
        passwordUser,
        showImageSettingsDialog,
        autoStartEnabled,
        autoStartDelaySeconds
    ) {
        if (
            autoStartEnabled &&
            !imageModeActive &&
            passwordUser == null &&
            !showImageSettingsDialog
        ) {
            delay(autoStartDelaySeconds * 1000L)

            if (
                autoStartEnabled &&
                !imageModeActive &&
                passwordUser == null &&
                !showImageSettingsDialog
            ) {
                imageModeActive = true
            }
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(screenPadding),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Luna Coin",
                    style = if (isPhone) {
                        MaterialTheme.typography.displaySmall
                    } else {
                        MaterialTheme.typography.displayMedium
                    },
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = "Wer bist du?",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(
                        top = 8.dp,
                        bottom = if (isPhone) 20.dp else 32.dp
                    )
                )

                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = gridMinSize),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = gridBottomPadding),
                    horizontalArrangement = Arrangement.spacedBy(gridSpacing),
                    verticalArrangement = Arrangement.spacedBy(gridSpacing)
                ) {
                    items(children) { child ->
                        UserProfileCard(
                            child = child,
                            isCompact = isPhone,
                            onClick = {
                                idleResetKey = System.currentTimeMillis()

                                val needsPassword =
                                    child.passwordRequired &&
                                            child.password.isNotBlank() &&
                                            !TrustedDeviceStorage.isTrusted(
                                                context = context,
                                                childId = child.id
                                            )

                                if (needsPassword) {
                                    passwordUser = child
                                } else {
                                    onChildSelected(child.id)
                                }
                            }
                        )
                    }
                }
            }
        }

        if (hasBuiltInAdmin && !isLoading) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .navigationBarsPadding()
                    .padding(
                        start = 16.dp,
                        end = if (isPhone) 16.dp else 24.dp,
                        bottom = if (isPhone) 16.dp else 24.dp
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        imageModeActive = true
                    }
                ) {
                    Text("Bild-Modus starten")
                }

                Spacer(modifier = Modifier.width(12.dp))

                Button(
                    onClick = {
                        showImageSettingsDialog = true
                        idleResetKey = System.currentTimeMillis()
                    }
                ) {
                    Text("⚙")
                }
            }
        }
    }

    if (showImageSettingsDialog) {
        ImageModeSettingsDialog(
            currentDelayMs = imageChangeDelayMs,
            currentPlayMode = imagePlayMode,
            autoStartEnabled = autoStartEnabled,
            autoStartDelaySeconds = autoStartDelaySeconds,
            onCancel = {
                showImageSettingsDialog = false
                idleResetKey = System.currentTimeMillis()
            },
            onSave = { newDelayMs, newPlayMode, newAutoStartEnabled, newAutoStartDelaySeconds ->
                imageChangeDelayMs = newDelayMs
                imagePlayMode = newPlayMode
                autoStartEnabled = newAutoStartEnabled
                autoStartDelaySeconds = newAutoStartDelaySeconds

                LunaImageModeStorage.setImageChangeDelayMs(
                    context = context,
                    delayMs = newDelayMs
                )

                LunaImageModeStorage.setPlayMode(
                    context = context,
                    playMode = newPlayMode
                )

                LunaImageModeStorage.setAutoStartEnabled(
                    context = context,
                    enabled = newAutoStartEnabled
                )

                LunaImageModeStorage.setAutoStartDelaySeconds(
                    context = context,
                    seconds = newAutoStartDelaySeconds
                )

                showImageSettingsDialog = false
                idleResetKey = System.currentTimeMillis()
            }
        )
    }

    passwordUser?.let { child ->
        PasswordDialog(
            child = child,
            canRememberLogin = child.allowRememberLogin,
            onCancel = {
                passwordUser = null
                idleResetKey = System.currentTimeMillis()
            },
            onSuccess = { rememberLogin ->
                if (rememberLogin && child.allowRememberLogin) {
                    TrustedDeviceStorage.setTrusted(
                        context = context,
                        childId = child.id,
                        trusted = true
                    )
                }

                passwordUser = null
                onChildSelected(child.id)
            }
        )
    }
}

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

    var imageOrder by remember(
        images,
        imagePlayMode
    ) {
        mutableStateOf(
            createImageOrder(
                imageCount = images.size,
                imagePlayMode = imagePlayMode
            )
        )
    }

    var currentOrderPosition by remember(
        images,
        imagePlayMode
    ) {
        mutableIntStateOf(0)
    }

    var autoPlayActive by remember {
        mutableStateOf(true)
    }

    var showExitButton by remember {
        mutableStateOf(false)
    }

    DisposableEffect(Unit) {
        val window = activity?.window

        if (window != null) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            WindowCompat.setDecorFitsSystemWindows(window, false)

            val controller = WindowInsetsControllerCompat(
                window,
                view
            )

            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        onDispose {
            val disposeWindow = activity?.window

            if (disposeWindow != null) {
                disposeWindow.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

                val controller = WindowInsetsControllerCompat(
                    disposeWindow,
                    view
                )

                controller.show(WindowInsetsCompat.Type.systemBars())
                WindowCompat.setDecorFitsSystemWindows(disposeWindow, true)
            }
        }
    }

    fun goToNextImage() {
        if (images.isEmpty() || imageOrder.isEmpty()) {
            return
        }

        if (currentOrderPosition >= imageOrder.lastIndex) {
            imageOrder = createImageOrder(
                imageCount = images.size,
                imagePlayMode = imagePlayMode
            )
            currentOrderPosition = 0
        } else {
            currentOrderPosition++
        }
    }

    fun goToPreviousImage() {
        if (images.isEmpty() || imageOrder.isEmpty()) {
            return
        }

        currentOrderPosition =
            if (currentOrderPosition == 0) {
                imageOrder.lastIndex
            } else {
                currentOrderPosition - 1
            }
    }

    LaunchedEffect(
        images,
        imageChangeDelayMs,
        imagePlayMode,
        autoPlayActive
    ) {
        if (autoPlayActive) {
            while (true) {
                delay(imageChangeDelayMs)
                goToNextImage()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(
                autoPlayActive,
                images,
                imageOrder,
                currentOrderPosition
            ) {
                detectTapGestures { offset ->
                    if (autoPlayActive) {
                        autoPlayActive = false
                        showExitButton = true
                    } else {
                        if (offset.x < size.width / 2f) {
                            goToPreviousImage()
                        } else {
                            goToNextImage()
                        }
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        if (images.isNotEmpty() && imageOrder.isNotEmpty()) {
            val imageIndex = imageOrder[currentOrderPosition]

            AsyncImage(
                model = images[imageIndex],
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
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = {
                        autoPlayActive = true
                        showExitButton = false
                    }
                ) {
                    Text("Autoplay fortsetzen")
                }

                Button(
                    onClick = onExit,
                    modifier = Modifier.padding(top = 12.dp)
                ) {
                    Text("Bild-Modus verlassen")
                }
            }
        }
    }
}

private fun createImageOrder(
    imageCount: Int,
    imagePlayMode: LunaImagePlayMode
): List<Int> {
    if (imageCount <= 0) {
        return emptyList()
    }

    return when (imagePlayMode) {
        LunaImagePlayMode.SEQUENTIAL -> {
            (0 until imageCount).toList()
        }

        LunaImagePlayMode.RANDOM -> {
            (0 until imageCount).shuffled()
        }
    }
}

@Composable
private fun ImageModeSettingsDialog(
    currentDelayMs: Long,
    currentPlayMode: LunaImagePlayMode,
    autoStartEnabled: Boolean,
    autoStartDelaySeconds: Long,
    onCancel: () -> Unit,
    onSave: (
        Long,
        LunaImagePlayMode,
        Boolean,
        Long
    ) -> Unit
) {
    var secondsInput by remember {
        mutableStateOf((currentDelayMs / 1000L).toString())
    }

    var selectedPlayMode by remember {
        mutableStateOf(currentPlayMode)
    }

    var autoStartEnabledState by remember {
        mutableStateOf(autoStartEnabled)
    }

    var autoStartDelayInput by remember {
        mutableStateOf(autoStartDelaySeconds.toString())
    }

    var showImageDelayError by remember {
        mutableStateOf(false)
    }

    var showAutoStartDelayError by remember {
        mutableStateOf(false)
    }

    AlertDialog(
        onDismissRequest = onCancel,
        title = {
            Text("Bild-Modus Einstellungen")
        },
        text = {
            Column {
                OutlinedTextField(
                    value = secondsInput,
                    onValueChange = {
                        secondsInput = it.filter { char ->
                            char.isDigit()
                        }
                        showImageDelayError = false
                    },
                    label = {
                        Text("Dauer pro Bild in Sekunden")
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    )
                )

                Text(
                    text = "Bildreihenfolge",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(
                        top = 20.dp,
                        bottom = 8.dp
                    )
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable {
                        selectedPlayMode = LunaImagePlayMode.SEQUENTIAL
                    }
                ) {
                    RadioButton(
                        selected = selectedPlayMode == LunaImagePlayMode.SEQUENTIAL,
                        onClick = {
                            selectedPlayMode = LunaImagePlayMode.SEQUENTIAL
                        }
                    )

                    Text("Der Reihe nach")
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable {
                        selectedPlayMode = LunaImagePlayMode.RANDOM
                    }
                ) {
                    RadioButton(
                        selected = selectedPlayMode == LunaImagePlayMode.RANDOM,
                        onClick = {
                            selectedPlayMode = LunaImagePlayMode.RANDOM
                        }
                    )

                    Text("Zufällig")
                }

                Text(
                    text = "Automatischer Start",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(
                        top = 20.dp,
                        bottom = 8.dp
                    )
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable {
                        autoStartEnabledState = !autoStartEnabledState
                    }
                ) {
                    Checkbox(
                        checked = autoStartEnabledState,
                        onCheckedChange = {
                            autoStartEnabledState = it
                            showAutoStartDelayError = false
                        }
                    )

                    Text("Bild-Modus automatisch starten")
                }

                if (autoStartEnabledState) {
                    OutlinedTextField(
                        value = autoStartDelayInput,
                        onValueChange = {
                            autoStartDelayInput = it.filter { char ->
                                char.isDigit()
                            }
                            showAutoStartDelayError = false
                        },
                        label = {
                            Text("Start nach Sekunden")
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        ),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                if (showImageDelayError) {
                    Text(
                        text = "Bitte für die Bilddauer einen Wert von ${LunaImageModeConfig.MIN_IMAGE_CHANGE_DELAY_SECONDS} bis ${LunaImageModeConfig.MAX_IMAGE_CHANGE_DELAY_SECONDS} Sekunden eingeben.",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                if (showAutoStartDelayError) {
                    Text(
                        text = "Bitte für den automatischen Start einen Wert von 10 bis 300 Sekunden eingeben.",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 8.dp)
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
                    val imageSeconds = secondsInput.toLongOrNull()

                    if (
                        imageSeconds == null ||
                        imageSeconds < LunaImageModeConfig.MIN_IMAGE_CHANGE_DELAY_SECONDS ||
                        imageSeconds > LunaImageModeConfig.MAX_IMAGE_CHANGE_DELAY_SECONDS
                    ) {
                        showImageDelayError = true
                        return@TextButton
                    }

                    val autoStartSeconds = autoStartDelayInput.toLongOrNull()

                    if (
                        autoStartEnabledState &&
                        (
                                autoStartSeconds == null ||
                                        autoStartSeconds < 10L ||
                                        autoStartSeconds > 300L
                                )
                    ) {
                        showAutoStartDelayError = true
                        return@TextButton
                    }

                    onSave(
                        imageSeconds * 1000L,
                        selectedPlayMode,
                        autoStartEnabledState,
                        autoStartSeconds ?: autoStartDelaySeconds
                    )
                }
            ) {
                Text("Speichern")
            }
        }
    )
}

@Composable
private fun UserProfileCard(
    child: Child,
    isCompact: Boolean,
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
        child.profileImageItem?.let {
            LunaItemCatalog.getDefinition(it).lunaImageRes
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
                if (isCompact) 18.dp else 24.dp
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
                        painter = androidx.compose.ui.res.painterResource(
                            id = profileImageRes
                        ),
                        contentDescription = child.name,
                        modifier = Modifier
                            .fillMaxSize()
                            .scale(1.4f),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Text(
                        text = child.name.firstOrNull()?.uppercase() ?: "?",
                        style = if (isCompact) {
                            MaterialTheme.typography.displayMedium
                        } else {
                            MaterialTheme.typography.displayLarge
                        },
                        color = textColor
                    )
                }
            }
        }

        Text(
            text = child.name,
            style = if (isCompact) {
                MaterialTheme.typography.titleMedium
            } else {
                MaterialTheme.typography.titleLarge
            },
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = if (isCompact) 8.dp else 10.dp)
        )

        when (child.role) {
            UserRole.CHILD -> {
                CoinDisplay(
                    amount = child.coins,
                    coinSize = if (isCompact) 36.dp else 48.dp
                )
            }

            UserRole.PARENT -> {
                Text(
                    text = "Eltern",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            UserRole.ADMIN -> {
                Text(
                    text = "Administrator",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        }
    }
}

@Composable
private fun PasswordDialog(
    child: Child,
    canRememberLogin: Boolean,
    onCancel: () -> Unit,
    onSuccess: (Boolean) -> Unit
) {
    var passwordInput by remember {
        mutableStateOf("")
    }

    var rememberLogin by remember {
        mutableStateOf(false)
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

                if (canRememberLogin) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(top = 12.dp)
                            .clickable {
                                rememberLogin = !rememberLogin
                            }
                    ) {
                        Checkbox(
                            checked = rememberLogin,
                            onCheckedChange = {
                                rememberLogin = it
                            }
                        )

                        Text("Diesem Gerät vertrauen")
                    }
                }

                if (showError) {
                    Text(
                        text = "Passwort falsch",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 8.dp)
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
                        onSuccess(rememberLogin)
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