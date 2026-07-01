package de.meson_labs.luna_coin.manager

import de.meson_labs.luna_coin.models.Child
import de.meson_labs.luna_coin.models.LogEntry
import de.meson_labs.luna_coin.models.LunaCoinData
import de.meson_labs.luna_coin.models.UserRole
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

private const val MAX_ACTIVE_LOGS = 2000
private const val BUILT_IN_ADMIN_ID = "built_in_admin"

fun addLogToList(log: LogEntry, currentLogs: List<LogEntry>): List<LogEntry> {
    return (listOf(log) + currentLogs).take(MAX_ACTIVE_LOGS)
}

fun sortChildrenInData(data: LunaCoinData): LunaCoinData {
    return data.copy(
        children = data.children.sortedBy { it.age }
    )
}

fun ensureBuiltInAdmin(data: LunaCoinData): LunaCoinData {
    val children = data.children

    val fixedChildren = when {
        children.any { it.isBuiltInAdmin || it.id == BUILT_IN_ADMIN_ID } -> {
            children.map { child ->
                if (child.isBuiltInAdmin || child.id == BUILT_IN_ADMIN_ID) {
                    child.copy(
                        id = BUILT_IN_ADMIN_ID,
                        role = UserRole.ADMIN,
                        passwordRequired = true,
                        allowRememberLogin = true,
                        isBuiltInAdmin = true
                    )
                } else {
                    child
                }
            }
        }

        children.any { it.role == UserRole.ADMIN } -> children

        else -> {
            children + Child(
                id = BUILT_IN_ADMIN_ID,
                name = "Thomas",
                coins = 0,
                role = UserRole.ADMIN,
                password = "",
                age = 99,
                passwordRequired = true,
                allowRememberLogin = true,
                isBuiltInAdmin = true
            )
        }
    }

    return data.copy(children = fixedChildren.sortedBy { it.age })
}

fun nowText(): String {
    return LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"))
}

fun uuid(): String {
    return UUID.randomUUID().toString()
}

fun String.toLocalDateOrNull(): LocalDate? {
    return try {
        LocalDate.parse(this)
    } catch (_: Exception) {
        null
    }
}
