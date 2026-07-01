package de.meson_labs.luna_coin.manager

import de.meson_labs.luna_coin.data.repository.DataRepository
import de.meson_labs.luna_coin.games.upsertHighscore
import de.meson_labs.luna_coin.models.GameHighscore
import de.meson_labs.luna_coin.models.LunaCoinData
import de.meson_labs.luna_coin.models.LunaGameLevel
import de.meson_labs.luna_coin.models.LunaGameScoreType
import de.meson_labs.luna_coin.models.LunaGameType

class GameHighscoreManager(
    private val repository: DataRepository
) {

    fun prepareSaveHighscore(
        currentData: LunaCoinData,
        game: LunaGameType,
        childId: String,
        scoreType: LunaGameScoreType,
        level: LunaGameLevel,
        value: Int
    ): SaveGameHighscoreOperation? {
        if (childId.isBlank()) return null
        if (value < 0) return null

        val newHighscore = GameHighscore(
            id = "${game}_${childId}_${level}_${scoreType}",
            game = game,
            childId = childId,
            scoreType = scoreType,
            level = level,
            value = value,
            timestamp = System.currentTimeMillis().toString()
        )

        val updatedHighscores = currentData.gameHighscores.upsertHighscore(newHighscore)

        if (updatedHighscores == currentData.gameHighscores) {
            return null
        }

        val optimisticData = sortChildrenInData(
            currentData.copy(
                gameHighscores = updatedHighscores
            )
        )

        return SaveGameHighscoreOperation(
            originalData = currentData,
            optimisticData = optimisticData,
            highscore = newHighscore
        )
    }

    suspend fun persistHighscore(operation: SaveGameHighscoreOperation) {
        repository.saveGameHighscore(operation.highscore)
    }
}

data class SaveGameHighscoreOperation(
    val originalData: LunaCoinData,
    val optimisticData: LunaCoinData,
    val highscore: GameHighscore
)
