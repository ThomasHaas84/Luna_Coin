package de.meson_labs.luna_coin.manager

import de.meson_labs.luna_coin.models.Child
import de.meson_labs.luna_coin.models.DogPlanData
import de.meson_labs.luna_coin.models.DogPlanShift
import de.meson_labs.luna_coin.models.DogPlanTaskCompletion
import de.meson_labs.luna_coin.models.DogPlanTaskTemplate
import de.meson_labs.luna_coin.models.DogPlanTaskType
import de.meson_labs.luna_coin.models.UserRole
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

data class DogPlanCompleteTaskResult(
    val dogPlanData: DogPlanData,
    val completion: DogPlanTaskCompletion,
    val rewardCoins: Int,
    val isEditing: Boolean
)

data class DogPlanSaveTemplateResult(
    val dogPlanData: DogPlanData,
    val template: DogPlanTaskTemplate
)

data class DogPlanSaveShiftResult(
    val dogPlanData: DogPlanData,
    val shift: DogPlanShift
)

class DogPlanManager {

    fun canEditDogPlan(user: Child?): Boolean {
        return user?.role == UserRole.PARENT ||
                user?.role == UserRole.ADMIN
    }

    fun canCompleteDogPlanTask(user: Child?): Boolean {
        return user != null
    }

    fun canAssignDogPlanShift(user: Child?): Boolean {
        return user != null
    }

    fun getActiveTemplates(
        dogPlanData: DogPlanData
    ): List<DogPlanTaskTemplate> {
        return dogPlanData.templates
            .filter { it.isActive }
            .sortedWith(
                compareBy<DogPlanTaskTemplate> { it.sortOrder }
                    .thenBy { it.time }
                    .thenBy { it.title }
            )
    }

    fun getAllTemplatesSorted(
        dogPlanData: DogPlanData
    ): List<DogPlanTaskTemplate> {
        return dogPlanData.templates
            .sortedWith(
                compareBy<DogPlanTaskTemplate> { it.sortOrder }
                    .thenBy { it.time }
                    .thenBy { it.title }
            )
    }

    fun getWalkTemplates(
        dogPlanData: DogPlanData
    ): List<DogPlanTaskTemplate> {
        return getActiveTemplates(dogPlanData)
            .filter { it.type == DogPlanTaskType.WALK }
    }

    fun getFeedingTemplates(
        dogPlanData: DogPlanData
    ): List<DogPlanTaskTemplate> {
        return getActiveTemplates(dogPlanData)
            .filter {
                it.type == DogPlanTaskType.FEEDING_EARLY ||
                        it.type == DogPlanTaskType.FEEDING_LATE
            }
    }

    fun getOtherTemplates(
        dogPlanData: DogPlanData
    ): List<DogPlanTaskTemplate> {
        return getActiveTemplates(dogPlanData)
            .filter { it.type == DogPlanTaskType.OTHER }
    }

    fun getCompletionsForDate(
        dogPlanData: DogPlanData,
        date: String
    ): List<DogPlanTaskCompletion> {
        return dogPlanData.completions
            .filter { it.date == date }
            .sortedBy { it.completedAt }
    }

    fun getCompletionForTemplateAndDate(
        dogPlanData: DogPlanData,
        templateId: String,
        date: String
    ): DogPlanTaskCompletion? {
        return dogPlanData.completions.firstOrNull {
            it.templateId == templateId && it.date == date
        }
    }

    fun isTaskCompletedForDate(
        dogPlanData: DogPlanData,
        templateId: String,
        date: String
    ): Boolean {
        return getCompletionForTemplateAndDate(
            dogPlanData = dogPlanData,
            templateId = templateId,
            date = date
        ) != null
    }

    fun getShiftForDate(
        dogPlanData: DogPlanData,
        date: String
    ): DogPlanShift? {
        return dogPlanData.shifts.firstOrNull { it.date == date }
    }

    fun prepareSaveTemplate(
        currentDogPlanData: DogPlanData,
        template: DogPlanTaskTemplate,
        currentUser: Child?
    ): DogPlanSaveTemplateResult? {
        if (!canEditDogPlan(currentUser)) {
            return null
        }

        val safeTemplate = template.copy(
            title = template.title.trim(),
            time = template.time.trim(),
            rewardCoins = template.rewardCoins.coerceAtLeast(0),
            sortOrder = template.sortOrder.coerceAtLeast(0)
        )

        if (safeTemplate.title.isBlank()) {
            return null
        }

        val finalTemplate = if (safeTemplate.id.isBlank()) {
            safeTemplate.copy(
                id = UUID.randomUUID().toString()
            )
        } else {
            safeTemplate
        }

        val updatedTemplates = currentDogPlanData.templates
            .filterNot { it.id == finalTemplate.id } + finalTemplate

        return DogPlanSaveTemplateResult(
            dogPlanData = currentDogPlanData.copy(
                templates = updatedTemplates
            ),
            template = finalTemplate
        )
    }

    fun prepareDeleteTemplate(
        currentDogPlanData: DogPlanData,
        templateId: String,
        currentUser: Child?
    ): DogPlanData? {
        if (!canEditDogPlan(currentUser)) {
            return null
        }

        return currentDogPlanData.copy(
            templates = currentDogPlanData.templates.filterNot { it.id == templateId },
            completions = currentDogPlanData.completions.filterNot { it.templateId == templateId }
        )
    }

    fun prepareCompleteTask(
        currentDogPlanData: DogPlanData,
        templateId: String,
        date: String,
        completedByChild: Child?,
        peed: Boolean,
        pooped: Boolean,
        diarrhea: Boolean,
        comment: String
    ): DogPlanCompleteTaskResult? {
        if (!canCompleteDogPlanTask(completedByChild)) {
            return null
        }

        val child = completedByChild ?: return null

        val template = currentDogPlanData.templates.firstOrNull { it.id == templateId }
            ?: return null

        if (!template.isActive) {
            return null
        }

        val safeComment = comment.trim()

        if (template.requiresComment && safeComment.isBlank()) {
            return null
        }

        val existingCompletion = getCompletionForTemplateAndDate(
            dogPlanData = currentDogPlanData,
            templateId = templateId,
            date = date
        )

        val isEditing = existingCompletion != null

        val completion = if (existingCompletion != null) {
            existingCompletion.copy(
                peed = if (template.requiresWalkDetails) peed else false,
                pooped = if (template.requiresWalkDetails) pooped else false,
                diarrhea = if (template.requiresWalkDetails) diarrhea else false,
                comment = safeComment
            )
        } else {
            DogPlanTaskCompletion(
                id = createCompletionId(
                    date = date,
                    templateId = template.id
                ),
                familyId = template.familyId,
                templateId = template.id,
                date = date,
                completedByChildId = child.id,
                completedAt = LocalDateTime.now().toString(),
                rewardCoins = template.rewardCoins.coerceAtLeast(0),
                peed = if (template.requiresWalkDetails) peed else false,
                pooped = if (template.requiresWalkDetails) pooped else false,
                diarrhea = if (template.requiresWalkDetails) diarrhea else false,
                comment = safeComment
            )
        }

        val updatedCompletions = currentDogPlanData.completions
            .filterNot { it.id == completion.id } + completion

        return DogPlanCompleteTaskResult(
            dogPlanData = currentDogPlanData.copy(
                completions = updatedCompletions
            ),
            completion = completion,
            rewardCoins = if (isEditing) 0 else completion.rewardCoins,
            isEditing = isEditing
        )
    }

    fun prepareDeleteCompletion(
        currentDogPlanData: DogPlanData,
        completionId: String,
        currentUser: Child?
    ): DogPlanData? {
        if (!canEditDogPlan(currentUser)) {
            return null
        }

        return currentDogPlanData.copy(
            completions = currentDogPlanData.completions.filterNot { it.id == completionId }
        )
    }

    fun prepareAssignEarlyShift(
        currentDogPlanData: DogPlanData,
        date: String,
        childId: String,
        currentUser: Child?
    ): DogPlanSaveShiftResult? {
        if (!canAssignDogPlanShift(currentUser)) {
            return null
        }

        val currentShift = getOrCreateShiftForDate(
            dogPlanData = currentDogPlanData,
            date = date
        )

        val updatedShift = currentShift.copy(
            earlyShiftChildId = childId
        )

        return createSaveShiftResult(
            currentDogPlanData = currentDogPlanData,
            updatedShift = updatedShift
        )
    }

    fun prepareAssignLateShift(
        currentDogPlanData: DogPlanData,
        date: String,
        childId: String,
        currentUser: Child?
    ): DogPlanSaveShiftResult? {
        if (!canAssignDogPlanShift(currentUser)) {
            return null
        }

        val currentShift = getOrCreateShiftForDate(
            dogPlanData = currentDogPlanData,
            date = date
        )

        val updatedShift = currentShift.copy(
            lateShiftChildId = childId
        )

        return createSaveShiftResult(
            currentDogPlanData = currentDogPlanData,
            updatedShift = updatedShift
        )
    }

    fun prepareClearEarlyShift(
        currentDogPlanData: DogPlanData,
        date: String,
        currentUser: Child?
    ): DogPlanSaveShiftResult? {
        if (!canAssignDogPlanShift(currentUser)) {
            return null
        }

        val currentShift = getShiftForDate(
            dogPlanData = currentDogPlanData,
            date = date
        ) ?: return null

        val updatedShift = currentShift.copy(
            earlyShiftChildId = null
        )

        return createSaveShiftResult(
            currentDogPlanData = currentDogPlanData,
            updatedShift = updatedShift
        )
    }

    fun prepareClearLateShift(
        currentDogPlanData: DogPlanData,
        date: String,
        currentUser: Child?
    ): DogPlanSaveShiftResult? {
        if (!canAssignDogPlanShift(currentUser)) {
            return null
        }

        val currentShift = getShiftForDate(
            dogPlanData = currentDogPlanData,
            date = date
        ) ?: return null

        val updatedShift = currentShift.copy(
            lateShiftChildId = null
        )

        return createSaveShiftResult(
            currentDogPlanData = currentDogPlanData,
            updatedShift = updatedShift
        )
    }

    fun todayText(): String {
        return LocalDate.now().toString()
    }

    private fun getOrCreateShiftForDate(
        dogPlanData: DogPlanData,
        date: String
    ): DogPlanShift {
        return getShiftForDate(
            dogPlanData = dogPlanData,
            date = date
        ) ?: DogPlanShift(
            id = createShiftId(date),
            date = date
        )
    }

    private fun createSaveShiftResult(
        currentDogPlanData: DogPlanData,
        updatedShift: DogPlanShift
    ): DogPlanSaveShiftResult {
        val updatedShifts = currentDogPlanData.shifts
            .filterNot { it.id == updatedShift.id } + updatedShift

        return DogPlanSaveShiftResult(
            dogPlanData = currentDogPlanData.copy(
                shifts = updatedShifts
            ),
            shift = updatedShift
        )
    }

    private fun createCompletionId(
        date: String,
        templateId: String
    ): String {
        return "dog_completion_${date}_$templateId"
            .replace(":", "")
            .replace(".", "")
            .replace("/", "_")
            .replace(" ", "_")
    }

    private fun createShiftId(date: String): String {
        return "dog_shift_$date"
            .replace(":", "")
            .replace(".", "")
            .replace("/", "_")
            .replace(" ", "_")
    }
}
