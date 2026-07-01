package de.meson_labs.luna_coin.manager

import de.meson_labs.luna_coin.data.repository.DataRepository
import de.meson_labs.luna_coin.models.LogEntry
import de.meson_labs.luna_coin.models.LogType
import de.meson_labs.luna_coin.models.LunaCoinData
import de.meson_labs.luna_coin.models.ShopItem
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ShopManager(
    private val repository: DataRepository
) {

    fun prepareAddShopItem(
        currentData: LunaCoinData,
        title: String,
        description: String,
        priceCoins: Int,
        maxPurchasesPerDay: Int
    ): ShopItemOperation {
        val newItem = ShopItem(
            id = uuid(),
            title = title,
            description = description,
            priceCoins = priceCoins,
            maxPurchasesPerDay = maxPurchasesPerDay
        )

        return ShopItemOperation(
            originalData = currentData,
            optimisticData = currentData.copy(
                shopItems = currentData.shopItems + newItem
            ),
            item = newItem
        )
    }

    fun prepareUpdateShopItem(
        currentData: LunaCoinData,
        itemId: String,
        title: String,
        description: String,
        priceCoins: Int,
        maxPurchasesPerDay: Int
    ): ShopItemOperation? {
        var updatedItem: ShopItem? = null

        val updatedShopItems = currentData.shopItems.map { item ->
            if (item.id == itemId) {
                item.copy(
                    title = title,
                    description = description,
                    priceCoins = priceCoins,
                    maxPurchasesPerDay = maxPurchasesPerDay
                ).also {
                    updatedItem = it
                }
            } else {
                item
            }
        }

        val safeUpdatedItem = updatedItem ?: return null

        return ShopItemOperation(
            originalData = currentData,
            optimisticData = currentData.copy(
                shopItems = updatedShopItems
            ),
            item = safeUpdatedItem
        )
    }

    fun prepareDeleteShopItem(
        currentData: LunaCoinData,
        itemId: String
    ): DeleteShopItemOperation {
        return DeleteShopItemOperation(
            originalData = currentData,
            optimisticData = currentData.copy(
                shopItems = currentData.shopItems.filterNot { it.id == itemId }
            ),
            itemId = itemId
        )
    }

    fun prepareBuyShopItem(
        currentData: LunaCoinData,
        childId: String,
        shopItemId: String
    ): BuyShopItemPreparation {
        val child = currentData.children.firstOrNull { it.id == childId }
            ?: return BuyShopItemPreparation.Error("❌ Benutzer konnte nicht gefunden werden")

        val item = currentData.shopItems.firstOrNull { it.id == shopItemId }
            ?: return BuyShopItemPreparation.Error("❌ Shop-Item konnte nicht gefunden werden")

        if (child.coins < item.priceCoins) {
            return BuyShopItemPreparation.Error("Nicht genug Luna Coins")
        }

        if (item.maxPurchasesPerDay > 0) {
            val purchasesToday = countShopItemPurchasesToday(
                logs = currentData.logs,
                childId = childId,
                itemTitle = item.title
            )

            if (purchasesToday >= item.maxPurchasesPerDay) {
                return BuyShopItemPreparation.Error(
                    "Tageslimit erreicht: ${item.title} kann heute nur ${item.maxPurchasesPerDay}x gekauft werden"
                )
            }
        }

        val coinDelta = -item.priceCoins

        val log = LogEntry(
            id = uuid(),
            timestamp = nowText(),
            childId = childId,
            type = LogType.SHOP_BUY,
            text = "${child.name} hat gekauft: ${item.title}",
            coinChange = coinDelta
        )

        val optimisticChild = child.copy(
            coins = child.coins + coinDelta
        )

        val optimisticData = sortChildrenInData(
            currentData.copy(
                children = currentData.children.map { currentChild ->
                    if (currentChild.id == childId) optimisticChild else currentChild
                },
                logs = addLogToList(log, currentData.logs)
            )
        )

        return BuyShopItemPreparation.Success(
            BuyShopItemOperation(
                originalData = currentData,
                optimisticData = optimisticData,
                childId = childId,
                coinDelta = coinDelta,
                log = log
            )
        )
    }

    suspend fun persistAddShopItem(operation: ShopItemOperation) {
        repository.saveShopItem(operation.item)
    }

    suspend fun persistUpdateShopItem(operation: ShopItemOperation) {
        repository.saveShopItem(operation.item)
    }

    suspend fun persistDeleteShopItem(operation: DeleteShopItemOperation) {
        repository.deleteShopItem(operation.itemId)
    }

    suspend fun persistBuyShopItem(operation: BuyShopItemOperation): Int {
        val realCoinValue = repository.changeChildCoins(
            childId = operation.childId,
            coinDelta = operation.coinDelta
        )

        repository.saveLog(operation.log)

        return realCoinValue
    }

    fun applyRealCoinValue(
        currentData: LunaCoinData,
        childId: String,
        realCoinValue: Int
    ): LunaCoinData {
        return sortChildrenInData(
            currentData.copy(
                children = currentData.children.map { child ->
                    if (child.id == childId) child.copy(coins = realCoinValue) else child
                }
            )
        )
    }

    private fun countShopItemPurchasesToday(
        logs: List<LogEntry>,
        childId: String,
        itemTitle: String
    ): Int {
        val todayPrefix = LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
        val expectedText = "hat gekauft: $itemTitle"

        return logs.count { log ->
            log.childId == childId &&
                    log.type == LogType.SHOP_BUY &&
                    log.timestamp.startsWith(todayPrefix) &&
                    log.text.contains(expectedText)
        }
    }
}

data class ShopItemOperation(
    val originalData: LunaCoinData,
    val optimisticData: LunaCoinData,
    val item: ShopItem
)

data class DeleteShopItemOperation(
    val originalData: LunaCoinData,
    val optimisticData: LunaCoinData,
    val itemId: String
)

sealed class BuyShopItemPreparation {
    data class Success(
        val operation: BuyShopItemOperation
    ) : BuyShopItemPreparation()

    data class Error(
        val message: String
    ) : BuyShopItemPreparation()
}

data class BuyShopItemOperation(
    val originalData: LunaCoinData,
    val optimisticData: LunaCoinData,
    val childId: String,
    val coinDelta: Int,
    val log: LogEntry
)
