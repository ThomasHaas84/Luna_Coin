package de.meson_labs.luna_coin.screens

import android.widget.VideoView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.net.toUri
import de.meson_labs.luna_coin.R
import de.meson_labs.luna_coin.components.CoinDisplay
import de.meson_labs.luna_coin.components.LunaScreenHeader
import de.meson_labs.luna_coin.components.dialogs.NotEnoughCoinsDialog
import de.meson_labs.luna_coin.components.dialogs.PurchaseResultDialog
import de.meson_labs.luna_coin.models.Child
import de.meson_labs.luna_coin.models.LogType
import de.meson_labs.luna_coin.models.LunaCoinData
import de.meson_labs.luna_coin.models.ShopItem
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun ShopScreen(
    modifier: Modifier = Modifier,
    data: LunaCoinData,
    selectedChild: Child?,
    onBuyItem: (String) -> Unit,
    onLuckyWheelResult: (
        childId: String,
        costCoins: Int,
        result: LuckyWheelResult
    ) -> LuckyWheelResult,
    onLogout: () -> Unit
) {
    var showSugarVideo by remember { mutableStateOf(false) }
    var showNotEnoughCoinsDialog by remember { mutableStateOf(false) }
    var purchaseMessage by remember { mutableStateOf<String?>(null) }
    var isPurchaseLocked by remember { mutableStateOf(false) }
    var showLuckyWheelDialog by remember { mutableStateOf(false) }

    val configuration = LocalConfiguration.current
    val isTabletLayout = configuration.smallestScreenWidthDp >= 600
    val isPhone = !isTabletLayout

    val screenPadding = if (isPhone) {
        14.dp
    } else {
        24.dp
    }

    val headerBottomSpacing = if (isPhone) {
        16.dp
    } else {
        24.dp
    }

    val todayText = LocalDate.now().toString()
    val selectedChildId = selectedChild?.id

    val luckyWheelUsageToday = data.luckyWheelUsage.firstOrNull { usage ->
        usage.childId == selectedChildId && usage.date == todayText
    }

    val luckyWheelIsFreeToday = selectedChildId != null &&
            luckyWheelUsageToday?.freeSpinUsed != true

    val sortedShopItems = remember(data.shopItems) {
        data.shopItems.sortedWith(
            compareBy<ShopItem> { it.priceCoins }
                .thenBy { it.title.lowercase() }
        )
    }

    LaunchedEffect(purchaseMessage) {
        if (purchaseMessage != null) {
            delay(3000)
            purchaseMessage = null
            isPurchaseLocked = false
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(screenPadding)
        ) {
            item {
                LunaScreenHeader(
                    title = "Shop",
                    selectedChild = selectedChild,
                    onLogout = onLogout
                )

                Spacer(modifier = Modifier.height(headerBottomSpacing))
            }

            item {
                LuckyWheelShopCard(
                    currentCoins = selectedChild?.coins ?: 0,
                    isFreeToday = luckyWheelIsFreeToday,
                    isPurchaseLocked = isPurchaseLocked,
                    onSpinClick = {
                        if (isPurchaseLocked) return@LuckyWheelShopCard

                        val costCoins = if (luckyWheelIsFreeToday) 0 else 1
                        val hasEnoughCoins = (selectedChild?.coins ?: 0) >= costCoins

                        if (!hasEnoughCoins) {
                            showNotEnoughCoinsDialog = true
                            return@LuckyWheelShopCard
                        }

                        isPurchaseLocked = true
                        showLuckyWheelDialog = true
                    }
                )
            }

            if (sortedShopItems.isEmpty()) {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Noch keine Shop-Artikel vorhanden.",
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            } else {
                items(sortedShopItems) { item ->
                    val purchasesToday = selectedChildId?.let { childId ->
                        countShopItemPurchasesToday(
                            data = data,
                            childId = childId,
                            itemTitle = item.title
                        )
                    } ?: 0

                    ShopItemCard(
                        item = item,
                        currentCoins = selectedChild?.coins ?: 0,
                        purchasesToday = purchasesToday,
                        isPurchaseLocked = isPurchaseLocked,
                        onBuyItem = {
                            if (isPurchaseLocked) return@ShopItemCard

                            val hasEnoughCoins = (selectedChild?.coins ?: 0) >= item.priceCoins
                            val limitReached =
                                item.maxPurchasesPerDay > 0 &&
                                        purchasesToday >= item.maxPurchasesPerDay

                            if (limitReached) {
                                purchaseMessage =
                                    "Tageslimit erreicht: ${item.title} kann heute nur ${item.maxPurchasesPerDay}x gekauft werden."
                                isPurchaseLocked = true
                                return@ShopItemCard
                            }

                            if (hasEnoughCoins) {
                                isPurchaseLocked = true
                                onBuyItem(item.id)

                                purchaseMessage = "Gekauft: ${item.title}\nLogeintrag wurde erstellt."

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
            PurchaseResultDialog(
                message = message,
                onDismiss = {
                    purchaseMessage = null
                    isPurchaseLocked = false
                }
            )
        }
    }

    if (showLuckyWheelDialog) {
        LuckyWheelDialog(
            onDismiss = {
                showLuckyWheelDialog = false
                isPurchaseLocked = false
            },
            onResult = { result ->
                val childId = selectedChild?.id ?: return@LuckyWheelDialog result

                val costCoins = if (luckyWheelIsFreeToday) 0 else 1

                val finalResult = onLuckyWheelResult(childId, costCoins, result)

                finalResult
            }
        )
    }

    if (showSugarVideo) {
        SugarVideoDialog(onDismiss = { showSugarVideo = false })
    }

    if (showNotEnoughCoinsDialog) {
        NotEnoughCoinsDialog(
            onDismiss = { showNotEnoughCoinsDialog = false }
        )
    }
}

@Composable
private fun ShopItemCard(
    item: ShopItem,
    currentCoins: Int,
    purchasesToday: Int,
    isPurchaseLocked: Boolean,
    onBuyItem: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val isTabletLayout = configuration.smallestScreenWidthDp >= 600
    val isPhone = !isTabletLayout

    val canBuy = currentCoins >= item.priceCoins
    val limitReached =
        item.maxPurchasesPerDay > 0 &&
                purchasesToday >= item.maxPurchasesPerDay

    val buttonContainerColor = if (canBuy && !isPurchaseLocked && !limitReached) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    val buttonContentColor = if (canBuy && !isPurchaseLocked && !limitReached) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    val cardPadding = if (isPhone) {
        12.dp
    } else {
        16.dp
    }

    val coinSize = if (isPhone) {
        34.dp
    } else {
        48.dp
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = if (isPhone) 5.dp else 6.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        if (isPhone) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(cardPadding),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ShopItemInfo(
                    item = item,
                    purchasesToday = purchasesToday,
                    canBuy = canBuy,
                    limitReached = limitReached,
                    isPurchaseLocked = isPurchaseLocked,
                    coinSize = coinSize
                )

                Button(
                    onClick = onBuyItem,
                    enabled = !isPurchaseLocked && !limitReached,
                    modifier = Modifier.fillMaxWidth(),
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
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(cardPadding),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ShopItemInfo(
                    item = item,
                    purchasesToday = purchasesToday,
                    canBuy = canBuy,
                    limitReached = limitReached,
                    isPurchaseLocked = isPurchaseLocked,
                    coinSize = coinSize,
                    modifier = Modifier.weight(1f)
                )

                Button(
                    onClick = onBuyItem,
                    enabled = !isPurchaseLocked && !limitReached,
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
}

@Composable
private fun ShopItemInfo(
    item: ShopItem,
    purchasesToday: Int,
    canBuy: Boolean,
    limitReached: Boolean,
    isPurchaseLocked: Boolean,
    coinSize: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = item.title,
            style = MaterialTheme.typography.titleMedium,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis
        )

        if (item.description.isNotBlank()) {
            Text(
                text = item.description,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        CoinDisplay(
            amount = item.priceCoins,
            coinSize = coinSize
        )

        if (item.maxPurchasesPerDay > 0) {
            Text(
                text = "Heute gekauft: $purchasesToday/${item.maxPurchasesPerDay}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (!canBuy) {
            Text(
                text = "Nicht genug Coins",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }

        if (limitReached) {
            Text(
                text = "Tageslimit erreicht",
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
}

private fun countShopItemPurchasesToday(
    data: LunaCoinData,
    childId: String,
    itemTitle: String
): Int {
    val todayPrefix = LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
    val expectedText = "hat gekauft: $itemTitle"

    return data.logs.count { log ->
        log.childId == childId &&
                log.type == LogType.SHOP_BUY &&
                log.timestamp.startsWith(todayPrefix) &&
                log.text.contains(expectedText)
    }
}

@Composable
private fun SugarVideoDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            color = Color.Black
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = {
                        VideoView(context).apply {
                            val videoUri = "android.resource://${context.packageName}/${R.raw.gib_mir_zucker}".toUri()

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