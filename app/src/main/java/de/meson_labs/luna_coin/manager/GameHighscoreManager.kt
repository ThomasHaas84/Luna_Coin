package de.meson_labs.luna_coin.manager

import de.meson_labs.luna_coin.data.repository.DataRepository
import de.meson_labs.luna_coin.games.upsertHighscore
import de.meson_labs.luna_coin.models.Child
import de.meson_labs.luna_coin.models.GameDailyReward
import de.meson_labs.luna_coin.models.GameHighscore
import de.meson_labs.luna_coin.models.LunaCoinData
import de.meson_labs.luna_coin.models.LunaGameLevel
import de.meson_labs.luna_coin.models.LunaGameScoreType
import de.meson_labs.luna_coin.models.LunaGameType
import java.time.LocalDate

class GameHighscoreManager(
    private val repository: DataRepository
) {

    fun prepareFinishGame(
        currentData: LunaCoinData,
        game: LunaGameType,
        childId: String,
        level: LunaGameLevel,
        scores: List<GameResultScore>
    ): FinishGameOperation? {
        if (childId.isBlank()) return null
        if (scores.isEmpty()) return null
        if (scores.any { it.value < 0 }) return null

        val child = currentData.children.firstOrNull { it.id == childId } ?: return null

        var updatedHighscores = currentData.gameHighscores
        val newHighscores = mutableListOf<GameHighscore>()

        scores.distinctBy { it.scoreType }.forEach { score ->
            val newHighscore = GameHighscore(
                id = "${game}_${childId}_${level}_${score.scoreType}",
                game = game,
                childId = childId,
                scoreType = score.scoreType,
                level = level,
                value = score.value,
                timestamp = System.currentTimeMillis().toString()
            )

            val nextHighscores = updatedHighscores.upsertHighscore(newHighscore)

            if (nextHighscores != updatedHighscores) {
                updatedHighscores = nextHighscores
                newHighscores += newHighscore
            }
        }

        val today = LocalDate.now().toString()

        val hasAlreadyReceivedDailyGameExperience =
            currentData.gameDailyRewards.any { reward ->
                reward.childId == childId &&
                        reward.game == game &&
                        reward.date == today
            }

        val dailyReward = if (hasAlreadyReceivedDailyGameExperience) {
            null
        } else {
            GameDailyReward(
                id = "${game}_${childId}_$today",
                childId = childId,
                game = game,
                date = today,
                timestamp = System.currentTimeMillis().toString()
            )
        }

        val hasNewHighscore = newHighscores.isNotEmpty()

        val gameFinishedExperience = if (dailyReward != null) {
            ProgressManager.EXPERIENCE_PER_GAME_FINISHED
        } else {
            0
        }

        val highscoreExperience = if (hasNewHighscore) {
            ProgressManager.EXPERIENCE_PER_NEW_HIGHSCORE
        } else {
            0
        }

        val experienceDelta = gameFinishedExperience + highscoreExperience

        val optimisticChild = ProgressManager.addExperience(
            child = child,
            experienceDelta = experienceDelta
        )

        val optimisticData = sortChildrenInData(
            currentData.copy(
                children = currentData.children.map { currentChild ->
                    if (currentChild.id == childId) optimisticChild else currentChild
                },
                gameHighscores = updatedHighscores,
                gameDailyRewards = if (dailyReward != null) {
                    currentData.gameDailyRewards + dailyReward
                } else {
                    currentData.gameDailyRewards
                }
            )
        )

        return FinishGameOperation(
            originalData = currentData,
            optimisticData = optimisticData,
            childId = childId,
            newHighscores = newHighscores,
            dailyReward = dailyReward,
            experienceDelta = experienceDelta
        )
    }

    suspend fun persistFinishGame(operation: FinishGameOperation): Child {
        operation.newHighscores.forEach { highscore ->
            repository.saveGameHighscore(highscore)
        }

        operation.dailyReward?.let { reward ->
            repository.saveGameDailyReward(reward)
        }

        return repository.changeChildCoinsAndExperience(
            childId = operation.childId,
            coinDelta = 0,
            experienceDelta = operation.experienceDelta
        )
    }

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

        val child = currentData.children.firstOrNull { it.id == childId } ?: return null

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

        val updatedChild = ProgressManager.addNewHighscoreExperience(child)

        val optimisticData = sortChildrenInData(
            currentData.copy(
                children = currentData.children.map { currentChild ->
                    if (currentChild.id == childId) updatedChild else currentChild
                },
                gameHighscores = updatedHighscores
            )
        )

        return SaveGameHighscoreOperation(
            originalData = currentData,
            optimisticData = optimisticData,
            highscore = newHighscore,
            updatedChild = updatedChild
        )
    }

    suspend fun persistHighscore(operation: SaveGameHighscoreOperation): Child {
        repository.saveGameHighscore(operation.highscore)

        return repository.changeChildCoinsAndExperience(
            childId = operation.updatedChild.id,
            coinDelta = 0,
            experienceDelta = ProgressManager.EXPERIENCE_PER_NEW_HIGHSCORE
        )
    }

    fun applyPersistedChildProgress(
        currentData: LunaCoinData,
        persistedChild: Child
    ): LunaCoinData {
        return sortChildrenInData(
            currentData.copy(
                children = currentData.children.map { child ->
                    if (child.id == persistedChild.id) {
                        child.copy(
                            coins = persistedChild.coins,
                            level = persistedChild.level,
                            experience = persistedChild.experience,
                            availableSkillPoints = persistedChild.availableSkillPoints,
                            intelligence = persistedChild.intelligence,
                            strength = persistedChild.strength,
                            agility = persistedChild.agility
                        )
                    } else {
                        child
                    }
                }
            )
        )
    }
}

data class GameResultScore(
    val scoreType: LunaGameScoreType,
    val value: Int
)

data class FinishGameOperation(
    val originalData: LunaCoinData,
    val optimisticData: LunaCoinData,
    val childId: String,
    val newHighscores: List<GameHighscore>,
    val dailyReward: GameDailyReward?,
    val experienceDelta: Int
)

data class SaveGameHighscoreOperation(
    val originalData: LunaCoinData,
    val optimisticData: LunaCoinData,
    val highscore: GameHighscore,
    val updatedChild: Child
)
