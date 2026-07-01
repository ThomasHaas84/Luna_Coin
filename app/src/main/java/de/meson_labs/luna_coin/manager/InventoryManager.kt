package de.meson_labs.luna_coin.manager

import de.meson_labs.luna_coin.data.repository.DataRepository
import de.meson_labs.luna_coin.models.Child
import de.meson_labs.luna_coin.models.LogEntry
import de.meson_labs.luna_coin.models.LogType
import de.meson_labs.luna_coin.models.LunaCoinData
import de.meson_labs.luna_coin.models.LunaInventoryItem
import de.meson_labs.luna_coin.models.LunaItemCatalog

class InventoryManager(
    private val repository: DataRepository
) {

    fun prepareBuyLunaMeItem(
        currentData: LunaCoinData,
        childId: String,
        itemName: String
    ): BuyLunaMeItemPreparation {
        val child = currentData.children.firstOrNull { it.id == childId }
            ?: return BuyLunaMeItemPreparation.Error("❌ Benutzer konnte nicht gefunden werden")

        val inventoryItem = try {
            LunaInventoryItem.valueOf(itemName)
        } catch (e: Exception) {
            println("❌ Unbekanntes LunaMe-Item: $itemName")
            return BuyLunaMeItemPreparation.Error("❌ Item konnte nicht gefunden werden")
        }

        if (inventoryItem in child.inventory) {
            return BuyLunaMeItemPreparation.Error("Item ist bereits freigeschaltet")
        }

        val definition = LunaItemCatalog.allItems.firstOrNull { it.item == inventoryItem }
            ?: run {
                println("❌ Keine Item-Definition gefunden für: $itemName")
                return BuyLunaMeItemPreparation.Error("❌ Item konnte nicht gefunden werden")
            }

        if (child.coins < definition.priceCoins) {
            return BuyLunaMeItemPreparation.Error("Nicht genug Luna Coins")
        }

        val coinDelta = -definition.priceCoins
        val newInventory = child.inventory + inventoryItem

        val optimisticChild = child.copy(
            coins = child.coins + coinDelta,
            inventory = newInventory,
            equippedItem = inventoryItem
        )

        val log = LogEntry(
            id = uuid(),
            timestamp = nowText(),
            childId = childId,
            type = LogType.SHOP_BUY,
            text = "${child.name} hat LunaME-Item gekauft: ${definition.title}",
            coinChange = coinDelta
        )

        val optimisticData = sortChildrenInData(
            currentData.copy(
                children = currentData.children.map { currentChild ->
                    if (currentChild.id == childId) optimisticChild else currentChild
                },
                logs = addLogToList(log, currentData.logs)
            )
        )

        return BuyLunaMeItemPreparation.Success(
            BuyLunaMeItemOperation(
                originalData = currentData,
                optimisticData = optimisticData,
                childId = childId,
                coinDelta = coinDelta,
                optimisticChild = optimisticChild,
                log = log,
                successMessage = "✅ ${definition.title} gekauft"
            )
        )
    }

    suspend fun persistBuyLunaMeItem(operation: BuyLunaMeItemOperation): Child {
        val realCoinValue = repository.changeChildCoins(
            childId = operation.childId,
            coinDelta = operation.coinDelta
        )

        val childWithRealCoins = operation.optimisticChild.copy(
            coins = realCoinValue
        )

        repository.updateChildInventory(
            childId = operation.childId,
            inventory = childWithRealCoins.inventory,
            equippedItem = childWithRealCoins.equippedItem,
            profileImageItem = childWithRealCoins.profileImageItem,
            hasProfileImage = childWithRealCoins.hasProfileImage
        )

        repository.saveLog(operation.log)

        return childWithRealCoins
    }

    fun applyPersistedInventoryChild(
        currentData: LunaCoinData,
        childWithRealCoins: Child
    ): LunaCoinData {
        return sortChildrenInData(
            currentData.copy(
                children = currentData.children.map { child ->
                    if (child.id == childWithRealCoins.id) childWithRealCoins else child
                }
            )
        )
    }
}

sealed class BuyLunaMeItemPreparation {
    data class Success(
        val operation: BuyLunaMeItemOperation
    ) : BuyLunaMeItemPreparation()

    data class Error(
        val message: String
    ) : BuyLunaMeItemPreparation()
}

data class BuyLunaMeItemOperation(
    val originalData: LunaCoinData,
    val optimisticData: LunaCoinData,
    val childId: String,
    val coinDelta: Int,
    val optimisticChild: Child,
    val log: LogEntry,
    val successMessage: String
)
