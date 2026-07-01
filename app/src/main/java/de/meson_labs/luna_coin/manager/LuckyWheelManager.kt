package de.meson_labs.luna_coin.manager

import de.meson_labs.luna_coin.data.repository.DataRepository
import de.meson_labs.luna_coin.models.Child
import de.meson_labs.luna_coin.models.LogEntry
import de.meson_labs.luna_coin.models.LogType
import de.meson_labs.luna_coin.models.LuckyWheelUsage
import de.meson_labs.luna_coin.models.LunaCoinData
import de.meson_labs.luna_coin.models.LunaItemCatalog
import de.meson_labs.luna_coin.screens.LuckyWheelResult
import java.time.LocalDate

class LuckyWheelManager(
    private val repository: DataRepository
) {

    fun prepareApplyLuckyWheelResult(
        currentData: LunaCoinData,
        childId: String,
        costCoins: Int,
        result: LuckyWheelResult
    ): LuckyWheelOperation? {
        val todayText = LocalDate.now().toString()
        val child = currentData.children.firstOrNull { it.id == childId } ?: return null

        val currentUsage = currentData.luckyWheelUsage.firstOrNull {
            it.childId == childId && it.date == todayText
        }

        val baseUsage = currentUsage ?: LuckyWheelUsage(
            id = "${childId}_${todayText}",
            childId = childId,
            date = todayText
        )

        val coinDelta = result.rewardCoins - costCoins

        var finalMessage = result.message
        var updatedInventory = child.inventory
        var skinWonToday = baseUsage.skinWon

        if (result.isSkinReward) {
            if (baseUsage.skinWon) {
                finalMessage = "Heute hast du bereits einen Skin gewonnen."
            } else {
                val nextSkin = LunaItemCatalog.allItems.firstOrNull {
                    it.item !in child.inventory
                }

                if (nextSkin != null) {
                    updatedInventory = child.inventory + nextSkin.item
                    skinWonToday = true
                    finalMessage = "Du hast den Skin „${nextSkin.title}“ gewonnen!"
                } else {
                    finalMessage = "Du hast bereits alle Skins gesammelt."
                }
            }
        }

        val updatedUsage = baseUsage.copy(
            freeSpinUsed = true,
            skinWon = skinWonToday
        )

        val updatedUsageList = if (currentUsage == null) {
            currentData.luckyWheelUsage + updatedUsage
        } else {
            currentData.luckyWheelUsage.map { usage ->
                if (usage.childId == childId && usage.date == todayText) updatedUsage else usage
            }
        }

        val optimisticChild = child.copy(
            coins = child.coins + coinDelta,
            inventory = updatedInventory
        )

        val log = LogEntry(
            id = uuid(),
            timestamp = nowText(),
            childId = childId,
            type = LogType.SYSTEM,
            text = if (costCoins == 0) {
                "${child.name} hat das Glücksrad kostenlos gedreht: $finalMessage"
            } else {
                "${child.name} hat das Glücksrad für $costCoins Luna Coin gedreht: $finalMessage"
            },
            coinChange = coinDelta
        )

        val optimisticData = sortChildrenInData(
            currentData.copy(
                children = currentData.children.map { currentChild ->
                    if (currentChild.id == childId) optimisticChild else currentChild
                },
                luckyWheelUsage = updatedUsageList,
                logs = addLogToList(log, currentData.logs)
            )
        )

        return LuckyWheelOperation(
            originalData = currentData,
            optimisticData = optimisticData,
            childId = childId,
            coinDelta = coinDelta,
            optimisticChild = optimisticChild,
            updatedUsage = updatedUsage,
            log = log,
            finalResult = result.copy(message = finalMessage)
        )
    }

    suspend fun persistLuckyWheelResult(operation: LuckyWheelOperation): Child {
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

        repository.saveLuckyWheelUsage(operation.updatedUsage)
        repository.saveLog(operation.log)

        return childWithRealCoins
    }

    fun applyPersistedLuckyWheelChild(
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

data class LuckyWheelOperation(
    val originalData: LunaCoinData,
    val optimisticData: LunaCoinData,
    val childId: String,
    val coinDelta: Int,
    val optimisticChild: Child,
    val updatedUsage: LuckyWheelUsage,
    val log: LogEntry,
    val finalResult: LuckyWheelResult
)
