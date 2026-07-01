package de.meson_labs.luna_coin.data

import de.meson_labs.luna_coin.models.Child
import de.meson_labs.luna_coin.models.DayOfWeekName
import de.meson_labs.luna_coin.models.DogScheduleItem
import de.meson_labs.luna_coin.models.LunaCoinData
import de.meson_labs.luna_coin.models.ShopItem
import de.meson_labs.luna_coin.models.TaskAssignmentType
import de.meson_labs.luna_coin.models.TaskCompletionMode
import de.meson_labs.luna_coin.models.TaskItem
import de.meson_labs.luna_coin.models.TaskRepeatType
import de.meson_labs.luna_coin.models.UserRole
import java.time.LocalDate
import java.util.Date
import java.util.UUID

object DemoData {

    private const val FAMILY_ID = "haas_family_demo"

    fun create(): LunaCoinData {
        val today = LocalDate.now()
        val todayText = today.toString()
        val now = Date()

        val clara = Child(
            id = "child_clara",
            familyId = FAMILY_ID,
            name = "Clara",
            coins = 0,
            role = UserRole.CHILD,
            age = 2,
            passwordRequired = false,
            allowRememberLogin = false,
            isBuiltInAdmin = false,
            createdAt = now,
            updatedAt = now
        )

        val jakob = Child(
            id = "child_jakob",
            familyId = FAMILY_ID,
            name = "Jakob",
            coins = 3,
            role = UserRole.CHILD,
            age = 7,
            passwordRequired = false,
            allowRememberLogin = false,
            isBuiltInAdmin = false,
            createdAt = now,
            updatedAt = now
        )

        val lukas = Child(
            id = "child_lukas",
            familyId = FAMILY_ID,
            name = "Lukas",
            coins = 4,
            role = UserRole.CHILD,
            age = 8,
            passwordRequired = false,
            allowRememberLogin = false,
            isBuiltInAdmin = false,
            createdAt = now,
            updatedAt = now
        )

        val noah = Child(
            id = "child_noah",
            familyId = FAMILY_ID,
            name = "Noah",
            coins = 5,
            role = UserRole.CHILD,
            age = 9,
            passwordRequired = false,
            allowRememberLogin = false,
            isBuiltInAdmin = false,
            createdAt = now,
            updatedAt = now
        )

        val max = Child(
            id = "child_max",
            familyId = FAMILY_ID,
            name = "Max",
            coins = 6,
            role = UserRole.CHILD,
            age = 10,
            passwordRequired = false,
            allowRememberLogin = false,
            isBuiltInAdmin = false,
            createdAt = now,
            updatedAt = now
        )

        val felix = Child(
            id = "child_felix",
            familyId = FAMILY_ID,
            name = "Felix",
            coins = 7,
            role = UserRole.CHILD,
            age = 11,
            passwordRequired = false,
            allowRememberLogin = false,
            isBuiltInAdmin = false,
            createdAt = now,
            updatedAt = now
        )

        val marie = Child(
            id = "child_marie",
            familyId = FAMILY_ID,
            name = "Marie",
            coins = 8,
            role = UserRole.CHILD,
            age = 12,
            passwordRequired = false,
            allowRememberLogin = false,
            isBuiltInAdmin = false,
            createdAt = now,
            updatedAt = now
        )

        val lisa = Child(
            id = "parent_lisa",
            familyId = FAMILY_ID,
            name = "Lisa",
            coins = 9,
            role = UserRole.PARENT,
            password = "6511",
            age = 40,
            passwordRequired = true,
            allowRememberLogin = true,
            isBuiltInAdmin = false,
            createdAt = now,
            updatedAt = now
        )

        val thomas = Child(
            id = "built_in_admin",
            familyId = FAMILY_ID,
            name = "Thomas",
            coins = 10,
            role = UserRole.ADMIN,
            password = "5761",
            age = 41,
            passwordRequired = true,
            allowRememberLogin = true,
            isBuiltInAdmin = true,
            createdAt = now,
            updatedAt = now
        )

        val children = listOf(
            clara,
            jakob,
            lukas,
            noah,
            max,
            felix,
            marie,
            lisa,
            thomas
        )

        val tasks = listOf(
            TaskItem(
                id = uuid(),
                familyId = FAMILY_ID,
                title = "Tisch decken",
                description = "Vor dem Essen Teller und Besteck auf den Tisch legen.",
                rewardCoins = 2,
                assignmentType = TaskAssignmentType.FREE_FOR_ALL,
                completionMode = TaskCompletionMode.EACH_PERSON,
                repeatType = TaskRepeatType.DAILY,
                startDate = todayText,
                createdAt = now,
                updatedAt = now
            ),
            TaskItem(
                id = uuid(),
                familyId = FAMILY_ID,
                title = "Spülmaschine ausräumen",
                description = "Sauberes Geschirr einsortieren.",
                rewardCoins = 4,
                assignmentType = TaskAssignmentType.FREE_FOR_ALL,
                completionMode = TaskCompletionMode.EACH_PERSON,
                repeatType = TaskRepeatType.DAILY,
                startDate = todayText,
                createdAt = now,
                updatedAt = now
            ),
            TaskItem(
                id = uuid(),
                familyId = FAMILY_ID,
                title = "Müll rausbringen",
                description = "Mülleimer leeren.",
                rewardCoins = 3,
                assignmentType = TaskAssignmentType.FREE_FOR_ALL,
                completionMode = TaskCompletionMode.EACH_PERSON,
                repeatType = TaskRepeatType.DAILY,
                startDate = todayText,
                createdAt = now,
                updatedAt = now
            ),
            TaskItem(
                id = uuid(),
                familyId = FAMILY_ID,
                title = "Straße kehren",
                description = "Straße oder Gehweg sauber kehren.",
                rewardCoins = 8,
                assignmentType = TaskAssignmentType.FREE_FOR_ALL,
                completionMode = TaskCompletionMode.ONCE_TOTAL,
                repeatType = TaskRepeatType.WEEKLY,
                startDate = todayText,
                weeklyDay = DayOfWeekName.SATURDAY,
                createdAt = now,
                updatedAt = now
            ),
            TaskItem(
                id = uuid(),
                familyId = FAMILY_ID,
                title = "Bad putzen",
                description = "Waschbecken, Toilette und Boden sauber machen.",
                rewardCoins = 12,
                assignmentType = TaskAssignmentType.ASSIGNED,
                completionMode = TaskCompletionMode.EACH_PERSON,
                assignedChildId = felix.id,
                repeatType = TaskRepeatType.WEEKLY,
                startDate = todayText,
                weeklyDay = DayOfWeekName.SATURDAY,
                createdAt = now,
                updatedAt = now
            ),
            TaskItem(
                id = uuid(),
                familyId = FAMILY_ID,
                title = "Treppe kehren",
                description = "Die Treppe gründlich kehren.",
                rewardCoins = 10,
                assignmentType = TaskAssignmentType.ASSIGNED,
                completionMode = TaskCompletionMode.EACH_PERSON,
                assignedChildId = max.id,
                repeatType = TaskRepeatType.WEEKLY,
                startDate = todayText,
                weeklyDay = DayOfWeekName.SATURDAY,
                createdAt = now,
                updatedAt = now
            ),
            TaskItem(
                id = uuid(),
                familyId = FAMILY_ID,
                title = "Beispiel mit Frist",
                description = "Diese Aufgabe zeigt, wie eine Aufgabe mit Fälligkeitsdatum aussieht.",
                rewardCoins = 6,
                assignmentType = TaskAssignmentType.FREE_FOR_ALL,
                completionMode = TaskCompletionMode.ONCE_TOTAL,
                repeatType = TaskRepeatType.WEEKLY,
                startDate = todayText,
                dueDate = "2026-07-12",
                weeklyDay = DayOfWeekName.SATURDAY,
                createdAt = now,
                updatedAt = now
            )
        )

        val shopItems = listOf(
            ShopItem(
                id = uuid(),
                familyId = FAMILY_ID,
                title = "Gib mir Zucker!",
                description = "Du darfst dir eins aus dem Glas nehmen.",
                priceCoins = 2,
                createdAt = now,
                updatedAt = now
            ),
            ShopItem(
                id = uuid(),
                familyId = FAMILY_ID,
                title = "30 Minuten Tablet-Zeit",
                description = "Zusätzliche Spielzeit am Tablet.",
                priceCoins = 10,
                createdAt = now,
                updatedAt = now
            ),
            ShopItem(
                id = uuid(),
                familyId = FAMILY_ID,
                title = "Filmabend aussuchen",
                description = "Du darfst den Film für den nächsten Filmabend auswählen.",
                priceCoins = 20,
                createdAt = now,
                updatedAt = now
            )
        )

        val dogSchedule = listOf(
            DogScheduleItem(
                id = uuid(),
                familyId = FAMILY_ID,
                childId = clara.id,
                dayOfWeek = DayOfWeekName.MONDAY,
                careStartTime = "08:00",
                careEndTime = "16:00",
                feedingTime = "07:30",
                walkTime = "18:00",
                createdAt = now,
                updatedAt = now
            ),
            DogScheduleItem(
                id = uuid(),
                familyId = FAMILY_ID,
                childId = jakob.id,
                dayOfWeek = DayOfWeekName.TUESDAY,
                careStartTime = "08:00",
                careEndTime = "16:00",
                feedingTime = "07:30",
                walkTime = "18:00",
                createdAt = now,
                updatedAt = now
            ),
            DogScheduleItem(
                id = uuid(),
                familyId = FAMILY_ID,
                childId = lukas.id,
                dayOfWeek = DayOfWeekName.WEDNESDAY,
                careStartTime = "08:00",
                careEndTime = "16:00",
                feedingTime = "07:30",
                walkTime = "18:00",
                createdAt = now,
                updatedAt = now
            ),
            DogScheduleItem(
                id = uuid(),
                familyId = FAMILY_ID,
                childId = noah.id,
                dayOfWeek = DayOfWeekName.THURSDAY,
                careStartTime = "08:00",
                careEndTime = "16:00",
                feedingTime = "07:30",
                walkTime = "18:00",
                createdAt = now,
                updatedAt = now
            ),
            DogScheduleItem(
                id = uuid(),
                familyId = FAMILY_ID,
                childId = max.id,
                dayOfWeek = DayOfWeekName.FRIDAY,
                careStartTime = "08:00",
                careEndTime = "16:00",
                feedingTime = "07:30",
                walkTime = "18:00",
                createdAt = now,
                updatedAt = now
            ),
            DogScheduleItem(
                id = uuid(),
                familyId = FAMILY_ID,
                childId = felix.id,
                dayOfWeek = DayOfWeekName.SATURDAY,
                careStartTime = "09:00",
                careEndTime = "17:00",
                feedingTime = "08:00",
                walkTime = "18:30",
                createdAt = now,
                updatedAt = now
            ),
            DogScheduleItem(
                id = uuid(),
                familyId = FAMILY_ID,
                childId = marie.id,
                dayOfWeek = DayOfWeekName.SUNDAY,
                careStartTime = "09:00",
                careEndTime = "17:00",
                feedingTime = "08:00",
                walkTime = "18:30",
                createdAt = now,
                updatedAt = now
            )
        )

        return LunaCoinData(
            children = children,
            tasks = tasks,
            shopItems = shopItems,
            dogSchedule = dogSchedule,
            logs = emptyList(),
            luckyWheelUsage = emptyList(),
            gameHighscores = emptyList()
        )
    }

    private fun uuid(): String {
        return UUID.randomUUID().toString()
    }
}