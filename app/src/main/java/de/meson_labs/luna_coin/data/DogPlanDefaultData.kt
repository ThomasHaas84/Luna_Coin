package de.meson_labs.luna_coin.data

import de.meson_labs.luna_coin.models.DogPlanData
import de.meson_labs.luna_coin.models.DogPlanTaskTemplate
import de.meson_labs.luna_coin.models.DogPlanTaskType

object DogPlanDefaultData {

    fun create(): DogPlanData {
        return DogPlanData(
            templates = createDefaultTemplates(),
            completions = emptyList(),
            shifts = emptyList()
        )
    }

    fun useDefaultsIfEmpty(dogPlan: DogPlanData): DogPlanData {
        if (dogPlan.templates.isNotEmpty()) {
            return dogPlan
        }

        return dogPlan.copy(
            templates = createDefaultTemplates()
        )
    }

    private fun createDefaultTemplates(): List<DogPlanTaskTemplate> {
        return listOf(
            walk("dog_walk_0700", "Gassi 07:00", "07:00", 2, 10),
            walk("dog_walk_1300", "Gassi 13:00", "13:00", 2, 20),
            walk("dog_walk_1500", "Gassi 15:00", "15:00", 2, 30),
            walk("dog_walk_1600", "Gassi 16:00", "16:00", 2, 40),
            walk("dog_walk_1800", "Gassi 18:00", "18:00", 2, 50),
            walk("dog_walk_2100", "Gassi 21:00", "21:00", 2, 60),
            walk("dog_walk_0000", "Gassi 00:00", "00:00", 3, 70),
            feedingEarly(),
            feedingLate()
        )
    }

    private fun walk(
        id: String,
        title: String,
        time: String,
        rewardCoins: Int,
        sortOrder: Int
    ): DogPlanTaskTemplate {
        return DogPlanTaskTemplate(
            id = id,
            title = title,
            type = DogPlanTaskType.WALK,
            time = time,
            rewardCoins = rewardCoins,
            isActive = true,
            sortOrder = sortOrder,
            requiresWalkDetails = true,
            requiresComment = false
        )
    }

    private fun feedingEarly(): DogPlanTaskTemplate {
        return DogPlanTaskTemplate(
            id = "dog_feeding_early",
            title = "Füttern Frühschicht",
            type = DogPlanTaskType.FEEDING_EARLY,
            time = "",
            rewardCoins = 1,
            isActive = true,
            sortOrder = 80,
            requiresWalkDetails = false,
            requiresComment = false
        )
    }

    private fun feedingLate(): DogPlanTaskTemplate {
        return DogPlanTaskTemplate(
            id = "dog_feeding_late",
            title = "Füttern Spätschicht",
            type = DogPlanTaskType.FEEDING_LATE,
            time = "",
            rewardCoins = 1,
            isActive = true,
            sortOrder = 90,
            requiresWalkDetails = false,
            requiresComment = false
        )
    }
}