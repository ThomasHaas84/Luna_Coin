package de.meson_labs.luna_coin.screens

import android.widget.VideoView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.net.toUri
import de.meson_labs.luna_coin.R
import de.meson_labs.luna_coin.components.CoinDisplay
import de.meson_labs.luna_coin.models.Child
import de.meson_labs.luna_coin.models.LunaCoinData
import de.meson_labs.luna_coin.models.ShopItem
import kotlinx.coroutines.delay

@Composable
fun ShopScreen(
    modifier: Modifier = Modifier,
    data: LunaCoinData,
    selectedChild: Child?,
    onBuyItem: (String) -> Unit,
    onLogout: () -> Unit
) {
    var showSugarVideo by remember {
        mutableStateOf(false)
    }

    var showNotEnoughCoinsDialog by remember {
        mutableStateOf(false)
    }

    var purchaseMessage by remember {
        mutableStateOf<String?>(null)
    }

    var isPurchaseLocked by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(purchaseMessage) {
        if (purchaseMessage != null) {
            delay(3000)
            purchaseMessage = null
            isPurchaseLocked = false
        }
    }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Shop",
                            style = MaterialTheme.typography.displaySmall
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${selectedChild?.name ?: ""}  ",
                                style = MaterialTheme.typography.titleLarge
                            )

                            CoinDisplay(
                                amount = selectedChild?.coins ?: 0
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = onLogout
                    ) {
                        Text("Benutzer wechseln")
                    }
                }

                Spacer(
                    modifier = Modifier.height(24.dp)
                )
            }

            if (data.shopItems.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Noch keine Shop-Artikel vorhanden.",
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            } else {
                items(data.shopItems) { item ->
                    ShopItemCard(
                        item = item,
                        currentCoins = selectedChild?.coins ?: 0,
                        isPurchaseLocked = isPurchaseLocked,
                        onBuyItem = {
                            if (isPurchaseLocked) {
                                return@ShopItemCard
                            }

                            val hasEnoughCoins =
                                (selectedChild?.coins ?: 0) >= item.priceCoins

                            if (hasEnoughCoins) {
                                isPurchaseLocked = true
                                onBuyItem(item.id)

                                purchaseMessage =
                                    "Gekauft: ${item.title}\nLogeintrag wurde erstellt."

                                if (item.title == "Gib mir Zucker!") {
                                    showSugarVideo = true
                                }
                            } else {
                                showNotEnoughCoinsDialog = true
                            }
                        }
                    )
                }
            }
        }

        purchaseMessage?.let { message ->
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(
                        start = 24.dp,
                        end = 24.dp,
                        bottom = 24.dp
                    ),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 12.dp
                )
            ) {
                Text(
                    text = message,
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }

    if (showSugarVideo) {
        SugarVideoDialog(
            onDismiss = {
                showSugarVideo = false
            }
        )
    }

    if (showNotEnoughCoinsDialog) {
        NotEnoughCoinsDialog(
            onDismiss = {
                showNotEnoughCoinsDialog = false
            }
        )
    }
}

@Composable
private fun ShopItemCard(
    item: ShopItem,
    currentCoins: Int,
    isPurchaseLocked: Boolean,
    onBuyItem: () -> Unit
) {
    val canBuy = currentCoins >= item.priceCoins

    val buttonContainerColor =
        if (canBuy && !isPurchaseLocked) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        }

    val buttonContentColor =
        if (canBuy && !isPurchaseLocked) {
            MaterialTheme.colorScheme.onPrimary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                vertical = 6.dp
            ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium
                )

                if (item.description.isNotBlank()) {
                    Text(
                        text = item.description,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                CoinDisplay(
                    amount = item.priceCoins
                )

                if (!canBuy) {
                    Text(
                        text = "Nicht genug Coins",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                if (isPurchaseLocked) {
                    Text(
                        text = "Kauf wird verarbeitet...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Button(
                onClick = onBuyItem,
                enabled = !isPurchaseLocked,
                colors = ButtonDefaults.buttonColors(
                    containerColor = buttonContainerColor,
                    contentColor = buttonContentColor,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Text("Kaufen")
            }
        }
    }
}

@Composable
private fun SugarVideoDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            color = Color.Black
        ) {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = {
                        VideoView(context).apply {
                            val videoUri =
                                "android.resource://${context.packageName}/${R.raw.gib_mir_zucker}".toUri()

                            setVideoURI(videoUri)

                            setOnPreparedListener { mediaPlayer ->
                                mediaPlayer.isLooping = false
                                start()
                            }

                            setOnCompletionListener {
                                onDismiss()
                            }
                        }
                    }
                )

                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(24.dp)
                ) {
                    Text("Schließen")
                }
            }
        }
    }
}