package de.meson_labs.luna_coin.manager

import de.meson_labs.luna_coin.data.repository.DataRepository
import de.meson_labs.luna_coin.models.LogEntry
import de.meson_labs.luna_coin.models.LunaCoinData

class LogManager(
    private val repository: DataRepository
) {

    fun prepareUndoLog(
        currentData: LunaCoinData,
        logId: String
    ): UndoLogOperation? {
        val log = currentData.logs.firstOrNull { it.id == logId } ?: return null
        val child = currentData.children.firstOrNull { it.id == log.childId } ?: return null

        val coinDelta = -log.coinChange

        val optimisticChild = child.copy(
            coins = child.coins + coinDelta
        )

        val optimisticData = sortChildrenInData(
            currentData.copy(
                children = currentData.children.map { currentChild ->
                    if (currentChild.id == log.childId) optimisticChild else currentChild
                },
                logs = currentData.logs.filterNot { it.id == logId }
            )
        )

        return UndoLogOperation(
            originalData = currentData,
            optimisticData = optimisticData,
            log = log,
            childId = log.childId,
            coinDelta = coinDelta
        )
    }

    suspend fun persistUndoLog(operation: UndoLogOperation): Int {
        val realCoinValue = repository.changeChildCoins(
            childId = operation.childId,
            coinDelta = operation.coinDelta
        )

        repository.deleteLog(operation.log.id)

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

data class UndoLogOperation(
    val originalData: LunaCoinData,
    val optimisticData: LunaCoinData,
    val log: LogEntry,
    val childId: String,
    val coinDelta: Int
)
