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
    val password: String = "",

    val inventory: List<LunaInventoryItem> = emptyList(),

    val equippedItem: LunaInventoryItem? = null,

    val profileImageItem: LunaInventoryItem? = null,

    val hasProfileImage: Boolean = false
)

@Serializable
enum class UserRole {
    CHILD,
    PARENT,
    ADMIN
}

@Serializable
enum class LunaInventoryItem {
    SUNGLASSES_1,
    SUNGLASSES_2,
    jacke_1,
    halstuch_1,
    kappe_1,
    huffel_1,
    chase_1,
    flash_1,
    iron_1,
    ballett_1,
    lunacraft_1,
    gandalf_1,
    slytherin,
    ravenclaw,
    griffindor,
    warhammer_1,
    pirat_1,
    cowboy_1,
    diabetis_1,
    knight_1,
    marshall_1,
    tau_1,
    ork_1,
    dark_knight_1,
    batman_1,
    gow_1,
    gow_2,
    wurst_1,
    shit_1,
    judo_1,
    kleid_1,
    talahoon_1,
    engel_1,
    cowboy_2,
    hase_1,
    schmucker_1,
    esel_1,
    zuma_1,
    jedi_1,
    hotdog_1,
    plume_1,
    greenlantern_1,
    greenarrow_1,
    unicorn_1,
    deadpool_1,
    spiderman_1,
    krypto_1,
    captainunderpants_1,
    cyber_1,
    cyber_2,
    perry_1,
    perry_2,
    ca_1,
    ca_2,


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
    WEEKDAYS,
    WEEKEND,
    WEEKLY,
    BIWEEKLY,
    MONTHLY,
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
    val completions: List<TaskCompletion> = emptyList(),
    val isWatchlist: Boolean = false
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