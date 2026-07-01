package de.meson_labs.luna_coin.manager

import de.meson_labs.luna_coin.data.repository.DataRepository
import de.meson_labs.luna_coin.models.LogEntry
import de.meson_labs.luna_coin.models.LogType
import de.meson_labs.luna_coin.models.LunaCoinData

class CoinManager(
    private val repository: DataRepository
) {

    fun prepareSetChildCoins(
        currentData: LunaCoinData,
        childId: String,
        newCoins: Int,
        comment: String?
    ): SetChildCoinsOperation? {
        val child = currentData.children.firstOrNull { it.id == childId } ?: return null

        val oldCoins = child.coins
        val coinDelta = newCoins - oldCoins

        val logText = if (comment.isNullOrBlank()) {
            "Coins von ${child.name} manuell angepasst: $oldCoins → $newCoins"
        } else {
            "Coins von ${child.name} manuell angepasst: $oldCoins → $newCoins ($comment)"
        }

        val log = LogEntry(
            id = uuid(),
            timestamp = nowText(),
            childId = childId,
            type = LogType.SYSTEM,
            text = logText,
            coinChange = coinDelta
        )

        val optimisticChild = child.copy(
            coins = newCoins
        )

        val optimisticData = sortChildrenInData(
            currentData.copy(
                children = currentData.children.map { currentChild ->
                    if (currentChild.id == childId) optimisticChild else currentChild
                },
                logs = addLogToList(log, currentData.logs)
            )
        )

        return SetChildCoinsOperation(
            originalData = currentData,
            optimisticData = optimisticData,
            childId = childId,
            newCoins = newCoins,
            log = log
        )
    }

    suspend fun persistSetChildCoins(operation: SetChildCoinsOperation): Int {
        val realCoinValue = repository.setChildCoins(
            childId = operation.childId,
            coins = operation.newCoins
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
}

data class SetChildCoinsOperation(
    val originalData: LunaCoinData,
    val optimisticData: LunaCoinData,
    val childId: String,
    val newCoins: Int,
    val log: LogEntry
)
