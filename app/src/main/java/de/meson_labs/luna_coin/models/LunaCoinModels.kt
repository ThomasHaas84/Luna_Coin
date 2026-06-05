package de.meson_labs.luna_coin.models

import kotlinx.serialization.Serializable

@Serializable
data class LunaCoinData(
    val children: List<Child> = emptyList(),
    val tasks: List<TaskItem> = emptyList(),
    val shopItems: List<ShopItem> = emptyList(),
    val dogSchedule: List<DogScheduleItem> = emptyList(),
    val logs: List<LogEntry> = emptyList()
)

@Serializable
data class Child(
    val id: String,
    val name: String,
    val coins: Int = 0,
    val role: UserRole = UserRole.CHILD,
    val password: String = ""
)

@Serializable
enum class UserRole {
    CHILD,
    PARENT,
    ADMIN
}

@Serializable
enum class TaskAssignmentType {
    FREE_FOR_ALL,
    ASSIGNED
}

@Serializable
enum class TaskCompletionMode {
    EACH_PERSON,
    ONCE_TOTAL
}

@Serializable
enum class TaskRepeatType {
    DAILY,
    WEEKLY,
    BIWEEKLY,
    YEARLY,
    EVERY_TWO_YEARS
}

@Serializable
data class TaskCompletion(
    val childId: String,
    val date: String,
    val timestamp: String
)

@Serializable
data class TaskItem(
    val id: String,
    val title: String,
    val description: String = "",
    val rewardCoins: Int,
    val assignmentType: TaskAssignmentType = TaskAssignmentType.FREE_FOR_ALL,
    val completionMode: TaskCompletionMode = TaskCompletionMode.EACH_PERSON,
    val assignedChildId: String? = null,
    val repeatType: TaskRepeatType = TaskRepeatType.DAILY,
    val startDate: String = "",
    val dueDate: String? = null,
    val weeklyDay: DayOfWeekName? = null,
    val completions: List<TaskCompletion> = emptyList()
)

@Serializable
data class ShopItem(
    val id: String,
    val title: String,
    val description: String = "",
    val priceCoins: Int
)

@Serializable
data class DogScheduleItem(
    val id: String,
    val childId: String,
    val dayOfWeek: DayOfWeekName,
    val careStartTime: String,
    val careEndTime: String,
    val feedingTime: String,
    val walkTime: String
)

@Serializable
enum class DayOfWeekName {
    MONDAY,
    TUESDAY,
    WEDNESDAY,
    THURSDAY,
    FRIDAY,
    SATURDAY,
    SUNDAY
}

@Serializable
data class LogEntry(
    val id: String,
    val timestamp: String,
    val childId: String,
    val type: LogType,
    val text: String,
    val coinChange: Int
)

@Serializable
enum class LogType {
    TASK_DONE,
    SHOP_BUY,
    SYSTEM
}