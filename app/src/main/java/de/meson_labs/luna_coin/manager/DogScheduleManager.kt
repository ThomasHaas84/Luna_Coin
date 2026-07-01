package de.meson_labs.luna_coin.manager

import de.meson_labs.luna_coin.data.repository.DataRepository
import de.meson_labs.luna_coin.models.DayOfWeekName
import de.meson_labs.luna_coin.models.DogScheduleItem
import de.meson_labs.luna_coin.models.LunaCoinData

class DogScheduleManager(
    private val repository: DataRepository
) {

    fun prepareAddDogSchedule(
        currentData: LunaCoinData,
        childId: String,
        dayOfWeek: DayOfWeekName,
        careStartTime: String,
        careEndTime: String,
        feedingTime: String,
        walkTime: String
    ): DogScheduleItemOperation {
        val newItem = DogScheduleItem(
            id = uuid(),
            childId = childId,
            dayOfWeek = dayOfWeek,
            careStartTime = careStartTime,
            careEndTime = careEndTime,
            feedingTime = feedingTime,
            walkTime = walkTime
        )

        return DogScheduleItemOperation(
            originalData = currentData,
            optimisticData = currentData.copy(
                dogSchedule = currentData.dogSchedule + newItem
            ),
            item = newItem
        )
    }

    fun prepareUpdateDogSchedule(
        currentData: LunaCoinData,
        scheduleId: String,
        childId: String,
        dayOfWeek: DayOfWeekName,
        careStartTime: String,
        careEndTime: String,
        feedingTime: String,
        walkTime: String
    ): DogScheduleItemOperation? {
        var updatedItem: DogScheduleItem? = null

        val updatedSchedule = currentData.dogSchedule.map { item ->
            if (item.id == scheduleId) {
                item.copy(
                    childId = childId,
                    dayOfWeek = dayOfWeek,
                    careStartTime = careStartTime,
                    careEndTime = careEndTime,
                    feedingTime = feedingTime,
                    walkTime = walkTime
                ).also {
                    updatedItem = it
                }
            } else {
                item
            }
        }

        val safeUpdatedItem = updatedItem ?: return null

        return DogScheduleItemOperation(
            originalData = currentData,
            optimisticData = currentData.copy(
                dogSchedule = updatedSchedule
            ),
            item = safeUpdatedItem
        )
    }

    fun prepareDeleteDogSchedule(
        currentData: LunaCoinData,
        scheduleId: String
    ): DeleteDogScheduleOperation {
        return DeleteDogScheduleOperation(
            originalData = currentData,
            optimisticData = currentData.copy(
                dogSchedule = currentData.dogSchedule.filterNot { it.id == scheduleId }
            ),
            scheduleId = scheduleId
        )
    }

    suspend fun persistAddDogSchedule(operation: DogScheduleItemOperation) {
        repository.saveDogScheduleItem(operation.item)
    }

    suspend fun persistUpdateDogSchedule(operation: DogScheduleItemOperation) {
        repository.saveDogScheduleItem(operation.item)
    }

    suspend fun persistDeleteDogSchedule(operation: DeleteDogScheduleOperation) {
        repository.deleteDogScheduleItem(operation.scheduleId)
    }
}

data class DogScheduleItemOperation(
    val originalData: LunaCoinData,
    val optimisticData: LunaCoinData,
    val item: DogScheduleItem
)

data class DeleteDogScheduleOperation(
    val originalData: LunaCoinData,
    val optimisticData: LunaCoinData,
    val scheduleId: String
)
