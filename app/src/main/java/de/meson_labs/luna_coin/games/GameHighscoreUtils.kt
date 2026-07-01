package de.meson_labs.luna_coin.games

import de.meson_labs.luna_coin.models.Child
import de.meson_labs.luna_coin.models.GameHighscore
import de.meson_labs.luna_coin.models.LunaGameLevel
import de.meson_labs.luna_coin.models.LunaGameScoreType
import de.meson_labs.luna_coin.models.LunaGameType

fun List<GameHighscore>.upsertHighscore(
    newHighscore: GameHighscore
): List<GameHighscore> {
    val existing = firstOrNull {
        it.game == newHighscore.game &&
                it.childId == newHighscore.childId &&
                it.scoreType == newHighscore.scoreType &&
                it.level == newHighscore.level
    }

    if (existing != null && existing.value <= newHighscore.value) {
        return this
    }

    return filterNot {
        it.game == newHighscore.game &&
                it.childId == newHighscore.childId &&
                it.scoreType == newHighscore.scoreType &&
                it.level == newHighscore.level
    } + newHighscore
}

fun List<GameHighscore>.bestEntry(
    childId: String?,
    game: LunaGameType,
    scoreType: LunaGameScoreType,
    level: LunaGameLevel
): GameHighscore? {
    return filter {
        it.game == game &&
                it.scoreType == scoreType &&
                it.level == level &&
                (childId == null || it.childId == childId)
    }.minByOrNull { it.value }
}

fun childName(
    childId: String,
    children: List<Child>
): String {
    return children.firstOrNull { it.id == childId }?.name ?: "Unbekannt"
}