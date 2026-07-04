package de.meson_labs.luna_coin.models

import de.meson_labs.luna_coin.data.models.FirebaseModel
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.util.Date

@Serializable
data class DogPlanData(
    val templates: List<DogPlanTaskTemplate> = emptyList(),
    val completions: List<DogPlanTaskCompletion> = emptyList(),
    val shifts: List<DogPlanShift> = emptyList()
)

@Serializable
data class DogPlanTaskTemplate(
    override var id: String = "",
    override var familyId: String = "",
    val title: String = "",
    val type: DogPlanTaskType = DogPlanTaskType.WALK,
    val time: String = "",
    val rewardCoins: Int = 0,
    val isActive: Boolean = true,
    val sortOrder: Int = 0,
    val requiresWalkDetails: Boolean = false,
    val requiresComment: Boolean = false,
    @Contextual override var createdAt: Date? = null,
    @Contextual override var updatedAt: Date? = null
) : FirebaseModel()

@Serializable
data class DogPlanTaskCompletion(
    override var id: String = "",
    override var familyId: String = "",
    val templateId: String = "",
    val date: String = "",
    val completedByChildId: String = "",
    val completedAt: String = "",
    val rewardCoins: Int = 0,
    val peed: Boolean = false,
    val pooped: Boolean = false,
    val diarrhea: Boolean = false,
    val comment: String = "",
    @Contextual override var createdAt: Date? = null,
    @Contextual override var updatedAt: Date? = null
) : FirebaseModel()

@Serializable
data class DogPlanShift(
    override var id: String = "",
    override var familyId: String = "",
    val date: String = "",
    val earlyShiftChildId: String? = null,
    val lateShiftChildId: String? = null,
    @Contextual override var createdAt: Date? = null,
    @Contextual override var updatedAt: Date? = null
) : FirebaseModel()

@Serializable
enum class DogPlanTaskType {
    WALK,
    FEEDING_EARLY,
    FEEDING_LATE,
    OTHER
}