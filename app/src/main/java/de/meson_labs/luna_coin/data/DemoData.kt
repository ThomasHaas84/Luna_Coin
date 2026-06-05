package de.meson_labs.luna_coin.data

import de.meson_labs.luna_coin.models.Child
import de.meson_labs.luna_coin.models.DayOfWeekName
import de.meson_labs.luna_coin.models.DogScheduleItem
import de.meson_labs.luna_coin.models.LunaCoinData
import de.meson_labs.luna_coin.models.ShopItem
import de.meson_labs.luna_coin.models.TaskItem
import de.meson_labs.luna_coin.models.UserRole
import java.time.LocalDate
import java.util.UUID

object DemoData {

    fun create(): LunaCoinData {
        val today = LocalDate.now()
        val dateText = today.toString()

        val clara = Child(
            id = "child_clara",
            name = "Clara",
            coins = 0,
            role = UserRole.CHILD
        )

        val jakob = Child(
            id = "child_jakob",
            name = "Jakob",
            coins = 0,
            role = UserRole.CHILD
        )

        val lukas = Child(
            id = "child_lukas",
            name = "Lukas",
            coins = 0,
            role = UserRole.CHILD
        )

        val noah = Child(
            id = "child_noah",
            name = "Noah",
            coins = 0,
            role = UserRole.CHILD
        )

        val max = Child(
            id = "child_max",
            name = "Max",
            coins = 0,
            role = UserRole.CHILD
        )

        val felix = Child(
            id = "child_felix",
            name = "Felix",
            coins = 0,
            role = UserRole.CHILD
        )

        val marie = Child(
            id = "child_marie",
            name = "Marie",
            coins = 0,
            role = UserRole.CHILD
        )

        val lisa = Child(
            id = "parent_lisa",
            name = "Lisa",
            coins = 0,
            role = UserRole.PARENT,
            password = "6511"
        )

        val thomas = Child(
            id = "admin_thomas",
            name = "Thomas",
            coins = 0,
            role = UserRole.ADMIN,
            password = "5761"
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
                title = "Tisch decken",
                description = "Vor dem Essen Teller und Besteck auf den Tisch legen.",
                rewardCoins = 2,
                assignedChildId = null,
                date = dateText
            ),
            TaskItem(
                id = uuid(),
                title = "Spülmaschine ausräumen",
                description = "Sauberes Geschirr einsortieren.",
                rewardCoins = 4,
                assignedChildId = null,
                date = dateText
            ),
            TaskItem(
                id = uuid(),
                title = "Zimmer aufräumen",
                description = "Boden frei räumen und Spielsachen einsortieren.",
                rewardCoins = 5,
                assignedChildId = null,
                date = dateText
            ),
            TaskItem(
                id = uuid(),
                title = "Müll rausbringen",
                description = "Mülleimer leeren.",
                rewardCoins = 3,
                assignedChildId = null,
                date = dateText
            )
        )

        val shopItems = listOf(
            ShopItem(
                id = uuid(),
                title = "Gib mir Zucker!",
                description = "Du darfst dir eins aus dem Glas nehmen.",
                priceCoins = 2
            ),
            ShopItem(
                id = uuid(),
                title = "30 Minuten Tablet-Zeit",
                description = "Zusätzliche Spielzeit am Tablet.",
                priceCoins = 10
            ),
            ShopItem(
                id = uuid(),
                title = "Filmabend aussuchen",
                description = "Du darfst den Film für den nächsten Filmabend auswählen.",
                priceCoins = 20
            ),
            ShopItem(
                id = uuid(),
                title = "Kleines Spielzeug",
                description = "Ein kleines Spielzeug oder eine kleine Überraschung.",
                priceCoins = 40
            )
        )

        val dogSchedule = listOf(
            DogScheduleItem(
                id = uuid(),
                childId = clara.id,
                dayOfWeek = DayOfWeekName.MONDAY,
                careStartTime = "08:00",
                careEndTime = "16:00",
                feedingTime = "07:30",
                walkTime = "18:00"
            ),
            DogScheduleItem(
                id = uuid(),
                childId = jakob.id,
                dayOfWeek = DayOfWeekName.TUESDAY,
                careStartTime = "08:00",
                careEndTime = "16:00",
                feedingTime = "07:30",
                walkTime = "18:00"
            ),
            DogScheduleItem(
                id = uuid(),
                childId = lukas.id,
                dayOfWeek = DayOfWeekName.WEDNESDAY,
                careStartTime = "08:00",
                careEndTime = "16:00",
                feedingTime = "07:30",
                walkTime = "18:00"
            ),
            DogScheduleItem(
                id = uuid(),
                childId = noah.id,
                dayOfWeek = DayOfWeekName.THURSDAY,
                careStartTime = "08:00",
                careEndTime = "16:00",
                feedingTime = "07:30",
                walkTime = "18:00"
            ),
            DogScheduleItem(
                id = uuid(),
                childId = max.id,
                dayOfWeek = DayOfWeekName.FRIDAY,
                careStartTime = "08:00",
                careEndTime = "16:00",
                feedingTime = "07:30",
                walkTime = "18:00"
            ),
            DogScheduleItem(
                id = uuid(),
                childId = felix.id,
                dayOfWeek = DayOfWeekName.SATURDAY,
                careStartTime = "09:00",
                careEndTime = "17:00",
                feedingTime = "08:00",
                walkTime = "18:30"
            ),
            DogScheduleItem(
                id = uuid(),
                childId = marie.id,
                dayOfWeek = DayOfWeekName.SUNDAY,
                careStartTime = "09:00",
                careEndTime = "17:00",
                feedingTime = "08:00",
                walkTime = "18:30"
            )
        )

        return LunaCoinData(
            children = children,
            tasks = tasks,
            shopItems = shopItems,
            dogSchedule = dogSchedule,
            logs = emptyList()
        )
    }

    private fun uuid(): String {
        return UUID.randomUUID().toString()
    }
}