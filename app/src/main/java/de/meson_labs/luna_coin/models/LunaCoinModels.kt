package de.meson_labs.luna_coin.models

import de.meson_labs.luna_coin.data.models.FirebaseModel
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.util.Date

@Serializable
data class LunaCoinData(
    val children: List<Child> = emptyList(),
    val tasks: List<TaskItem> = emptyList(),
    val shopItems: List<ShopItem> = emptyList(),
    val dogSchedule: List<DogScheduleItem> = emptyList(),
    val logs: List<LogEntry> = emptyList(),
    val luckyWheelUsage: List<LuckyWheelUsage> = emptyList(),
    val gameHighscores: List<GameHighscore> = emptyList()
)

@Serializable
data class Child(
    override var id: String = "",
    override var familyId: String = "",
    val name: String = "",
    var coins: Int = 0,
    val role: UserRole = UserRole.CHILD,
    var password: String = "",
    val age: Int = 0,

    val inventory: List<LunaInventoryItem> = emptyList(),
    val equippedItem: LunaInventoryItem? = null,
    val profileImageItem: LunaInventoryItem? = null,
    val hasProfileImage: Boolean = false,

    @Contextual override var createdAt: Date? = null,
    @Contextual override var updatedAt: Date? = null
) : FirebaseModel()

@Serializable
data class TaskItem(
    override var id: String = "",
    override var familyId: String = "",
    val title: String = "",
    val description: String = "",
    val rewardCoins: Int = 0,
    val assignmentType: TaskAssignmentType = TaskAssignmentType.FREE_FOR_ALL,
    val completionMode: TaskCompletionMode = TaskCompletionMode.EACH_PERSON,
    val assignedChildId: String? = null,
    val repeatType: TaskRepeatType = TaskRepeatType.DAILY,
    val startDate: String = "",
    val dueDate: String? = null,
    val weeklyDay: DayOfWeekName? = null,
    val completions: List<TaskCompletion> = emptyList(),
    val isWatchlist: Boolean = false,

    @Contextual override var createdAt: Date? = null,
    @Contextual override var updatedAt: Date? = null
) : FirebaseModel()

@Serializable
data class ShopItem(
    override var id: String = "",
    override var familyId: String = "",
    val title: String = "",
    val description: String = "",
    val priceCoins: Int = 0,

    @Contextual override var createdAt: Date? = null,
    @Contextual override var updatedAt: Date? = null
) : FirebaseModel()

@Serializable
data class LogEntry(
    override var id: String = "",
    override var familyId: String = "",
    val timestamp: String = "",
    val childId: String = "",
    val type: LogType = LogType.SYSTEM,
    val text: String = "",
    val coinChange: Int = 0,

    @Contextual override var createdAt: Date? = null,
    @Contextual override var updatedAt: Date? = null
) : FirebaseModel()

@Serializable
data class DogScheduleItem(
    override var id: String = "",
    override var familyId: String = "",
    val childId: String = "",
    val dayOfWeek: DayOfWeekName = DayOfWeekName.MONDAY,
    val careStartTime: String = "",
    val careEndTime: String = "",
    val feedingTime: String = "",
    val walkTime: String = "",

    @Contextual override var createdAt: Date? = null,
    @Contextual override var updatedAt: Date? = null
) : FirebaseModel()

@Serializable
data class LuckyWheelUsage(
    override var id: String = "",
    override var familyId: String = "",
    val childId: String = "",
    val date: String = "",
    val freeSpinUsed: Boolean = false,
    val skinWon: Boolean = false,

    @Contextual override var createdAt: Date? = null,
    @Contextual override var updatedAt: Date? = null
) : FirebaseModel()

@Serializable
data class GameHighscore(
    override var id: String = "",
    override var familyId: String = "",
    val game: LunaGameType = LunaGameType.MEMORY,
    val childId: String = "",
    val scoreType: LunaGameScoreType = LunaGameScoreType.ATTEMPTS,
    val level: LunaGameLevel = LunaGameLevel.DEFAULT,
    val value: Int = 0,
    val timestamp: String = "",

    @Contextual override var createdAt: Date? = null,
    @Contextual override var updatedAt: Date? = null
) : FirebaseModel()

@Serializable
data class TaskCompletion(
    val childId: String = "",
    val date: String = "",
    val timestamp: String = ""
)

@Serializable
enum class UserRole { CHILD, PARENT, ADMIN }

@Serializable
enum class LunaInventoryItem {
    SUNGLASSES_1, SUNGLASSES_2, jacke_1, halstuch_1, kappe_1, huffel_1, chase_1,
    flash_1, iron_1, ballett_1, lunacraft_1, gandalf_1, slytherin, ravenclaw,
    griffindor, warhammer_1, pirat_1, cowboy_1, diabetis_1, knight_1, marshall_1,
    tau_1, ork_1, dark_knight_1, batman_1, gow_1, gow_2, wurst_1, shit_1, judo_1,
    kleid_1, talahoon_1, engel_1, cowboy_2, hase_1, schmucker_1, esel_1, zuma_1,
    jedi_1, hotdog_1, plume_1, greenlantern_1, greenarrow_1, unicorn_1, deadpool_1,
    spiderman_1, krypto_1, captainunderpants_1, cyber_1, cyber_2, perry_1, perry_2,
    ca_1, ca_2, fledermaus_1, rochen_1, toolshed_1, deutschland_1, niederlande_1,
    portugal_1, virgil_1, dante_1, blade_1
}

@Serializable
enum class TaskAssignmentType { FREE_FOR_ALL, ASSIGNED }

@Serializable
enum class TaskCompletionMode { EACH_PERSON, ONCE_TOTAL }

@Serializable
enum class TaskRepeatType {
    DAILY, WEEKDAYS, WEEKEND, WEEKLY, BIWEEKLY, MONTHLY, YEARLY, EVERY_TWO_YEARS
}

@Serializable
enum class DayOfWeekName {
    MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY
}

@Serializable
enum class LogType { TASK_DONE, SHOP_BUY, SYSTEM }

@Serializable
enum class LunaGameType { MEMORY, NUMBER_GUESS, MULTIPLICATION }

@Serializable
enum class LunaGameScoreType { ATTEMPTS, TIME_SECONDS }

@Serializable
enum class LunaGameLevel { DEFAULT, EASY, HARD }