package de.meson_labs.luna_coin.manager

import de.meson_labs.luna_coin.models.Child

enum class LunaSkillType {
    INTELLIGENCE,
    STRENGTH,
    AGILITY
}

object LevelManager {

    const val MIN_LEVEL = 1
    const val MAX_LEVEL = 100
    const val MIN_SKILL_VALUE = 1
    const val MAX_SKILL_VALUE = 100

    fun addExperience(
        child: Child,
        experienceToAdd: Int
    ): Child {
        if (experienceToAdd <= 0) return normalize(child)

        val normalizedChild = normalize(child)
        val newExperience = normalizedChild.experience + experienceToAdd
        val newLevel = calculateLevelForExperience(newExperience)
        val gainedLevels = (newLevel - normalizedChild.level).coerceAtLeast(0)

        return normalizedChild.copy(
            experience = newExperience,
            level = newLevel,
            availableSkillPoints = normalizedChild.availableSkillPoints + gainedLevels
        )
    }

    fun increaseSkill(
        child: Child,
        skillType: LunaSkillType
    ): Child {
        val normalizedChild = normalize(child)

        if (normalizedChild.availableSkillPoints <= 0) return normalizedChild

        val currentSkillValue = getSkillValue(normalizedChild, skillType)

        if (currentSkillValue >= MAX_SKILL_VALUE) return normalizedChild

        return when (skillType) {
            LunaSkillType.INTELLIGENCE -> normalizedChild.copy(
                intelligence = currentSkillValue + 1,
                availableSkillPoints = normalizedChild.availableSkillPoints - 1
            )

            LunaSkillType.STRENGTH -> normalizedChild.copy(
                strength = currentSkillValue + 1,
                availableSkillPoints = normalizedChild.availableSkillPoints - 1
            )

            LunaSkillType.AGILITY -> normalizedChild.copy(
                agility = currentSkillValue + 1,
                availableSkillPoints = normalizedChild.availableSkillPoints - 1
            )
        }
    }

    fun normalize(child: Child): Child {
        val safeExperience = child.experience.coerceAtLeast(0)
        val calculatedLevel = calculateLevelForExperience(safeExperience)
        val safeLevel = child.level.coerceIn(MIN_LEVEL, MAX_LEVEL).coerceAtLeast(calculatedLevel)

        return child.copy(
            level = safeLevel,
            experience = safeExperience,
            availableSkillPoints = child.availableSkillPoints.coerceAtLeast(0),
            intelligence = child.intelligence.coerceIn(MIN_SKILL_VALUE, MAX_SKILL_VALUE),
            strength = child.strength.coerceIn(MIN_SKILL_VALUE, MAX_SKILL_VALUE),
            agility = child.agility.coerceIn(MIN_SKILL_VALUE, MAX_SKILL_VALUE)
        )
    }

    fun getExperienceForLevel(level: Int): Int {
        val safeLevel = level.coerceIn(MIN_LEVEL, MAX_LEVEL)
        if (safeLevel <= MIN_LEVEL) return 0

        var totalExperience = 0

        for (nextLevel in 2..safeLevel) {
            totalExperience += getRequiredExperienceForLevelUp(nextLevel - 1)
        }

        return totalExperience
    }

    fun getRequiredExperienceForLevelUp(currentLevel: Int): Int {
        val safeLevel = currentLevel.coerceIn(MIN_LEVEL, MAX_LEVEL)
        if (safeLevel >= MAX_LEVEL) return 0

        return 5 + (safeLevel * 5)
    }

    fun getExperienceProgressInCurrentLevel(child: Child): Int {
        val normalizedChild = normalize(child)
        if (normalizedChild.level >= MAX_LEVEL) return 0

        return normalizedChild.experience - getExperienceForLevel(normalizedChild.level)
    }

    fun getExperienceNeededForNextLevel(child: Child): Int {
        val normalizedChild = normalize(child)
        if (normalizedChild.level >= MAX_LEVEL) return 0

        val nextLevelExperience = getExperienceForLevel(normalizedChild.level + 1)
        return (nextLevelExperience - normalizedChild.experience).coerceAtLeast(0)
    }

    fun getRequiredExperienceForCurrentLevel(child: Child): Int {
        val normalizedChild = normalize(child)
        if (normalizedChild.level >= MAX_LEVEL) return 0

        return getRequiredExperienceForLevelUp(normalizedChild.level)
    }

    fun calculateLevelForExperience(experience: Int): Int {
        val safeExperience = experience.coerceAtLeast(0)
        var level = MIN_LEVEL

        while (level < MAX_LEVEL && safeExperience >= getExperienceForLevel(level + 1)) {
            level++
        }

        return level
    }

    private fun getSkillValue(
        child: Child,
        skillType: LunaSkillType
    ): Int {
        return when (skillType) {
            LunaSkillType.INTELLIGENCE -> child.intelligence
            LunaSkillType.STRENGTH -> child.strength
            LunaSkillType.AGILITY -> child.agility
        }
    }
}
