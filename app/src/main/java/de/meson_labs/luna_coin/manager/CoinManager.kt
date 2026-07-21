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

        val safeNewCoins = newCoins.coerceAtLeast(0)
        val oldCoins = child.coins
        val coinDelta = safeNewCoins - oldCoins

        val logText = if (comment.isNullOrBlank()) {
            "Coins von ${child.name} manuell angepasst: $oldCoins → $safeNewCoins"
        } else {
            "Coins von ${child.name} manuell angepasst: $oldCoins → $safeNewCoins ($comment)"
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
            coins = safeNewCoins
        )

        val optimisticData = sortChildrenInData(
            currentData.copy(
                children = currentData.children.map { currentChild ->
                    if (currentChild.id == childId) {
                        optimisticChild
                    } else {
                        currentChild
                    }
                },
                logs = addLogToList(log, currentData.logs)
            )
        )

        return SetChildCoinsOperation(
            originalData = currentData,
            optimisticData = optimisticData,
            childId = childId,
            newCoins = safeNewCoins,
            log = log
        )
    }

    suspend fun persistSetChildCoins(
        operation: SetChildCoinsOperation
    ): Int {
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
                    if (child.id == childId) {
                        child.copy(
                            coins = realCoinValue.coerceAtLeast(0)
                        )
                    } else {
                        child
                    }
                }
            )
        )
    }

    fun prepareSetChildSilver(
        currentData: LunaCoinData,
        childId: String,
        newSilver: Long,
        comment: String?
    ): SetChildSilverOperation? {
        val child = currentData.children.firstOrNull { it.id == childId } ?: return null

        val safeNewSilver = newSilver.coerceAtLeast(0L)
        val oldSilver = child.silver
        val silverDelta = safeNewSilver - oldSilver

        val logText = if (comment.isNullOrBlank()) {
            "Luna Silver von ${child.name} manuell angepasst: $oldSilver → $safeNewSilver"
        } else {
            "Luna Silver von ${child.name} manuell angepasst: $oldSilver → $safeNewSilver ($comment)"
        }

        val log = LogEntry(
            id = uuid(),
            timestamp = nowText(),
            childId = childId,
            type = LogType.SYSTEM,
            text = logText,
            silverChange = silverDelta
        )

        val optimisticChild = child.copy(
            silver = safeNewSilver
        )

        val optimisticData = sortChildrenInData(
            currentData.copy(
                children = currentData.children.map { currentChild ->
                    if (currentChild.id == childId) {
                        optimisticChild
                    } else {
                        currentChild
                    }
                },
                logs = addLogToList(log, currentData.logs)
            )
        )

        return SetChildSilverOperation(
            originalData = currentData,
            optimisticData = optimisticData,
            childId = childId,
            newSilver = safeNewSilver,
            log = log
        )
    }

    suspend fun persistSetChildSilver(
        operation: SetChildSilverOperation
    ): Long {
        val realSilverValue = repository.setChildSilver(
            childId = operation.childId,
            silver = operation.newSilver
        )

        repository.saveLog(operation.log)

        return realSilverValue
    }

    fun applyRealSilverValue(
        currentData: LunaCoinData,
        childId: String,
        realSilverValue: Long
    ): LunaCoinData {
        return sortChildrenInData(
            currentData.copy(
                children = currentData.children.map { child ->
                    if (child.id == childId) {
                        child.copy(
                            silver = realSilverValue.coerceAtLeast(0L)
                        )
                    } else {
                        child
                    }
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

data class SetChildSilverOperation(
    val originalData: LunaCoinData,
    val optimisticData: LunaCoinData,
    val childId: String,
    val newSilver: Long,
    val log: LogEntry
)
