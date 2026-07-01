package de.meson_labs.luna_coin.manager

import de.meson_labs.luna_coin.data.repository.DataRepository
import de.meson_labs.luna_coin.models.Child
import de.meson_labs.luna_coin.models.LogEntry
import de.meson_labs.luna_coin.models.LogType
import de.meson_labs.luna_coin.models.LunaCoinData
import de.meson_labs.luna_coin.models.TaskAssignmentType
import de.meson_labs.luna_coin.models.TaskItem
import de.meson_labs.luna_coin.models.UserRole

class UserManager(
    private val repository: DataRepository
) {

    fun prepareAddChild(
        currentData: LunaCoinData,
        name: String,
        role: UserRole,
        password: String,
        age: Int,
        coins: Int,
        passwordRequired: Boolean,
        allowRememberLogin: Boolean
    ): AddChildPrepareResult {
        val trimmedName = name.trim()

        if (trimmedName.isBlank()) {
            return AddChildPrepareResult(
                operation = null,
                errorMessage = "❌ Name darf nicht leer sein"
            )
        }

        val safeCoins = coins.coerceAtLeast(0)

        val newChild = Child(
            id = uuid(),
            name = trimmedName,
            role = role,
            password = password,
            age = age,
            coins = safeCoins,
            passwordRequired = passwordRequired,
            allowRememberLogin = allowRememberLogin,
            isBuiltInAdmin = false
        )

        val log = LogEntry(
            id = uuid(),
            timestamp = nowText(),
            childId = newChild.id,
            type = LogType.SYSTEM,
            text = "Benutzer angelegt: ${newChild.name} (${newChild.role})",
            coinChange = 0
        )

        val optimisticData = sortChildrenInData(
            currentData.copy(
                children = currentData.children + newChild,
                logs = addLogToList(log, currentData.logs)
            )
        )

        return AddChildPrepareResult(
            operation = AddChildOperation(
                originalData = currentData,
                optimisticData = optimisticData,
                child = newChild,
                log = log
            ),
            errorMessage = null
        )
    }

    fun prepareUpdateChild(
        currentData: LunaCoinData,
        updatedChild: Child
    ): UpdateChildOperation? {
        val existingChild = currentData.children.firstOrNull { it.id == updatedChild.id } ?: return null
        val adminCount = currentData.children.count { it.role == UserRole.ADMIN }

        var warningMessage: String? = null

        val safeRole = when {
            existingChild.isBuiltInAdmin -> UserRole.ADMIN
            existingChild.role == UserRole.ADMIN &&
                    updatedChild.role != UserRole.ADMIN &&
                    adminCount <= 1 -> {
                warningMessage = "❌ Der letzte Admin darf seine Admin-Rechte nicht verlieren"
                UserRole.ADMIN
            }

            else -> updatedChild.role
        }

        val safeUpdatedChild = updatedChild.copy(
            id = existingChild.id,
            coins = existingChild.coins,
            role = safeRole,
            isBuiltInAdmin = existingChild.isBuiltInAdmin,
            passwordRequired = if (existingChild.isBuiltInAdmin) true else updatedChild.passwordRequired,
            allowRememberLogin = updatedChild.allowRememberLogin
        )

        val optimisticData = sortChildrenInData(
            currentData.copy(
                children = currentData.children.map { child ->
                    if (child.id == safeUpdatedChild.id) safeUpdatedChild else child
                }
            )
        )

        return UpdateChildOperation(
            originalData = currentData,
            optimisticData = optimisticData,
            child = safeUpdatedChild,
            warningMessage = warningMessage
        )
    }

    fun prepareDeleteChild(
        currentData: LunaCoinData,
        childId: String
    ): DeleteChildPrepareResult {
        val child = currentData.children.firstOrNull { it.id == childId }
            ?: return DeleteChildPrepareResult(null, null)

        if (child.isBuiltInAdmin) {
            return DeleteChildPrepareResult(
                operation = null,
                errorMessage = "❌ Der Standard-Admin kann nicht gelöscht werden"
            )
        }

        if (child.role == UserRole.ADMIN) {
            val adminCount = currentData.children.count { it.role == UserRole.ADMIN }

            if (adminCount <= 1) {
                return DeleteChildPrepareResult(
                    operation = null,
                    errorMessage = "❌ Der letzte Admin kann nicht gelöscht werden"
                )
            }
        }

        val log = LogEntry(
            id = uuid(),
            timestamp = nowText(),
            childId = childId,
            type = LogType.SYSTEM,
            text = "Benutzer gelöscht: ${child.name}",
            coinChange = 0
        )

        val tasksToUpdate = currentData.tasks
            .filter { it.assignedChildId == childId }
            .map { task ->
                task.copy(
                    assignmentType = TaskAssignmentType.FREE_FOR_ALL,
                    assignedChildId = null
                )
            }

        val optimisticTasks = currentData.tasks.map { task ->
            if (task.assignedChildId == childId) {
                task.copy(
                    assignmentType = TaskAssignmentType.FREE_FOR_ALL,
                    assignedChildId = null
                )
            } else {
                task
            }
        }

        val optimisticData = sortChildrenInData(
            currentData.copy(
                children = currentData.children.filterNot { it.id == childId },
                tasks = optimisticTasks,
                dogSchedule = currentData.dogSchedule.filterNot { it.childId == childId },
                luckyWheelUsage = currentData.luckyWheelUsage.filterNot { it.childId == childId },
                gameHighscores = currentData.gameHighscores.filterNot { it.childId == childId },
                logs = addLogToList(log, currentData.logs)
            )
        )

        return DeleteChildPrepareResult(
            operation = DeleteChildOperation(
                originalData = currentData,
                optimisticData = optimisticData,
                childId = childId,
                tasksToUpdate = tasksToUpdate,
                dogScheduleItemIdsToDelete = currentData.dogSchedule
                    .filter { it.childId == childId }
                    .map { it.id },
                luckyWheelUsageIdsToDelete = currentData.luckyWheelUsage
                    .filter { it.childId == childId }
                    .map { it.id },
                gameHighscoreIdsToDelete = currentData.gameHighscores
                    .filter { it.childId == childId }
                    .map { it.id },
                log = log
            ),
            errorMessage = null
        )
    }

    suspend fun persistAddChild(operation: AddChildOperation) {
        repository.saveChild(operation.child)
        repository.saveLog(operation.log)
    }

    suspend fun persistUpdateChild(operation: UpdateChildOperation) {
        repository.updateChildProfile(
            childId = operation.child.id,
            name = operation.child.name,
            role = operation.child.role,
            password = operation.child.password,
            age = operation.child.age,
            passwordRequired = operation.child.passwordRequired,
            allowRememberLogin = operation.child.allowRememberLogin,
            isBuiltInAdmin = operation.child.isBuiltInAdmin
        )

        repository.updateChildInventory(
            childId = operation.child.id,
            inventory = operation.child.inventory,
            equippedItem = operation.child.equippedItem,
            profileImageItem = operation.child.profileImageItem,
            hasProfileImage = operation.child.hasProfileImage
        )
    }

    suspend fun persistDeleteChild(operation: DeleteChildOperation) {
        repository.deleteChild(operation.childId)

        operation.tasksToUpdate.forEach { task ->
            repository.saveTask(task)
        }

        operation.dogScheduleItemIdsToDelete.forEach { itemId ->
            repository.deleteDogScheduleItem(itemId)
        }

        operation.luckyWheelUsageIdsToDelete.forEach { usageId ->
            repository.deleteLuckyWheelUsage(usageId)
        }

        operation.gameHighscoreIdsToDelete.forEach { highscoreId ->
            repository.deleteGameHighscore(highscoreId)
        }

        repository.saveLog(operation.log)
    }
}

data class AddChildPrepareResult(
    val operation: AddChildOperation?,
    val errorMessage: String?
)

data class AddChildOperation(
    val originalData: LunaCoinData,
    val optimisticData: LunaCoinData,
    val child: Child,
    val log: LogEntry
)

data class UpdateChildOperation(
    val originalData: LunaCoinData,
    val optimisticData: LunaCoinData,
    val child: Child,
    val warningMessage: String?
)

data class DeleteChildPrepareResult(
    val operation: DeleteChildOperation?,
    val errorMessage: String?
)

data class DeleteChildOperation(
    val originalData: LunaCoinData,
    val optimisticData: LunaCoinData,
    val childId: String,
    val tasksToUpdate: List<TaskItem>,
    val dogScheduleItemIdsToDelete: List<String>,
    val luckyWheelUsageIdsToDelete: List<String>,
    val gameHighscoreIdsToDelete: List<String>,
    val log: LogEntry
)
