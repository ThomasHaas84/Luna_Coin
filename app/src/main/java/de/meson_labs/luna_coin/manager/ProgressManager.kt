package de.meson_labs.luna_coin.manager

import de.meson_labs.luna_coin.models.Child
import de.meson_labs.luna_coin.models.LunaCoinData

object ProgressManager {

    const val MIN_LEVEL = 1
    const val MAX_LEVEL = 100

    const val MIN_SKILL_VALUE = 1
    const val MAX_SKILL_VALUE = 100

    const val EXPERIENCE_PER_GAME_FINISHED = 1
    const val EXPERIENCE_PER_NEW_HIGHSCORE = 10

    fun experienceNeededForNextLevel(level: Int): Int {
        val safeLevel = level.coerceIn(MIN_LEVEL, MAX_LEVEL)

        if (safeLevel >= MAX_LEVEL) {
            return Int.MAX_VALUE
        }

        return 10 + ((safeLevel - 1) * 5)
    }

    fun experienceNeededForCurrentLevel(level: Int): Int {
        var total = 0

        for (currentLevel in MIN_LEVEL until level.coerceIn(MIN_LEVEL, MAX_LEVEL)) {
            total += experienceNeededForNextLevel(currentLevel)
        }

        return total
    }

    fun experienceNeededForLevel(level: Int): Int {
        return experienceNeededForCurrentLevel(level)
    }

    fun experienceProgressInCurrentLevel(child: Child): Int {
        val totalForCurrentLevel = experienceNeededForCurrentLevel(child.level)
        return (child.experience - totalForCurrentLevel).coerceAtLeast(0)
    }

    fun experienceNeededInCurrentLevel(child: Child): Int {
        return experienceNeededForNextLevel(child.level)
    }

    fun experienceUntilNextLevel(child: Child): Int {
        if (child.level >= MAX_LEVEL) return 0

        val currentProgress = experienceProgressInCurrentLevel(child)
        return (experienceNeededInCurrentLevel(child) - currentProgress).coerceAtLeast(0)
    }

    fun progressFraction(child: Child): Float {
        if (child.level >= MAX_LEVEL) return 1f

        val needed = experienceNeededInCurrentLevel(child)
        if (needed <= 0) return 0f

        return (experienceProgressInCurrentLevel(child).toFloat() / needed.toFloat())
            .coerceIn(0f, 1f)
    }

    fun addExperience(
        child: Child,
        experienceDelta: Int
    ): Child {
        if (experienceDelta <= 0) return sanitizeProgress(child)

        val sanitizedChild = sanitizeProgress(child)

        if (sanitizedChild.level >= MAX_LEVEL) {
            return sanitizedChild.copy(
                level = MAX_LEVEL,
                experience = sanitizedChild.experience.coerceAtLeast(0)
            )
        }

        var newExperience = sanitizedChild.experience + experienceDelta
        var newLevel = sanitizedChild.level
        var newSkillPoints = sanitizedChild.availableSkillPoints

        while (
            newLevel < MAX_LEVEL &&
            newExperience >= experienceNeededForCurrentLevel(newLevel + 1)
        ) {
            newLevel += 1
            newSkillPoints += 1
        }

        return sanitizedChild.copy(
            level = newLevel.coerceIn(MIN_LEVEL, MAX_LEVEL),
            experience = newExperience.coerceAtLeast(0),
            availableSkillPoints = newSkillPoints.coerceAtLeast(0)
        )
    }

    fun addCoinsAndExperience(
        child: Child,
        coinDelta: Int,
        experienceDelta: Int
    ): Child {
        val childWithCoins = child.copy(
            coins = (child.coins + coinDelta).coerceAtLeast(0)
        )

        return addExperience(
            child = childWithCoins,
            experienceDelta = experienceDelta
        )
    }

    fun addTaskReward(
        child: Child,
        rewardCoins: Int
    ): Child {
        return addCoinsAndExperience(
            child = child,
            coinDelta = rewardCoins,
            experienceDelta = rewardCoins.coerceAtLeast(0)
        )
    }

    fun addGameFinishedExperience(
        child: Child
    ): Child {
        return addExperience(
            child = child,
            experienceDelta = EXPERIENCE_PER_GAME_FINISHED
        )
    }

    fun addNewHighscoreExperience(
        child: Child
    ): Child {
        return addExperience(
            child = child,
            experienceDelta = EXPERIENCE_PER_NEW_HIGHSCORE
        )
    }

    fun increaseIntelligence(child: Child): Child {
        return increaseSkill(child, ProgressSkill.INTELLIGENCE)
    }

    fun increaseStrength(child: Child): Child {
        return increaseSkill(child, ProgressSkill.STRENGTH)
    }

    fun increaseAgility(child: Child): Child {
        return increaseSkill(child, ProgressSkill.AGILITY)
    }

    fun increaseSkill(
        child: Child,
        skill: ProgressSkill
    ): Child {
        val sanitizedChild = sanitizeProgress(child)

        if (sanitizedChild.availableSkillPoints <= 0) return sanitizedChild

        return when (skill) {
            ProgressSkill.INTELLIGENCE -> {
                if (sanitizedChild.intelligence >= MAX_SKILL_VALUE) {
                    sanitizedChild
                } else {
                    sanitizedChild.copy(
                        intelligence = sanitizedChild.intelligence + 1,
                        availableSkillPoints = sanitizedChild.availableSkillPoints - 1
                    )
                }
            }

            ProgressSkill.STRENGTH -> {
                if (sanitizedChild.strength >= MAX_SKILL_VALUE) {
                    sanitizedChild
                } else {
                    sanitizedChild.copy(
                        strength = sanitizedChild.strength + 1,
                        availableSkillPoints = sanitizedChild.availableSkillPoints - 1
                    )
                }
            }

            ProgressSkill.AGILITY -> {
                if (sanitizedChild.agility >= MAX_SKILL_VALUE) {
                    sanitizedChild
                } else {
                    sanitizedChild.copy(
                        agility = sanitizedChild.agility + 1,
                        availableSkillPoints = sanitizedChild.availableSkillPoints - 1
                    )
                }
            }
        }
    }

    fun replaceChild(
        currentData: LunaCoinData,
        updatedChild: Child
    ): LunaCoinData {
        return sortChildrenInData(
            currentData.copy(
                children = currentData.children.map { child ->
                    if (child.id == updatedChild.id) updatedChild else child
                }
            )
        )
    }

    fun sanitizeProgress(child: Child): Child {
        return child.copy(
            level = child.level.coerceIn(MIN_LEVEL, MAX_LEVEL),
            experience = child.experience.coerceAtLeast(0),
            availableSkillPoints = child.availableSkillPoints.coerceAtLeast(0),
            intelligence = child.intelligence.coerceIn(MIN_SKILL_VALUE, MAX_SKILL_VALUE),
            strength = child.strength.coerceIn(MIN_SKILL_VALUE, MAX_SKILL_VALUE),
            agility = child.agility.coerceIn(MIN_SKILL_VALUE, MAX_SKILL_VALUE)
        )
    }
}

enum class ProgressSkill {
    INTELLIGENCE,
    STRENGTH,
    AGILITY
}
