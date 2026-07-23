package de.meson_labs.luna_coin.lunarim.manager

import de.meson_labs.luna_coin.lunarim.data.LunarimEffects
import de.meson_labs.luna_coin.lunarim.data.LunarimItems
import de.meson_labs.luna_coin.lunarim.models.*

object LunarimCharacterStatsCalculator {

    fun calculate(
        attributes: LunarimAttributes,
        character: LunarimCharacterState
    ): LunarimCalculatedStats {
        val equippedItems = character.equipment.equippedInstanceIds.values.mapNotNull { instanceId ->
            val instance = character.inventory.firstOrNull { it.instanceId == instanceId }
            instance?.let { LunarimItems.getById(it.itemId) }
        }

        val activeDefinitions = character.activeEffects.mapNotNull { active ->
            LunarimEffects.getById(active.effectId)?.let { active to it }
        }

        val itemArmor = equippedItems.sumOf { it.armor }
        val itemResistances = equippedItems.fold(LunarimResistances()) { total, item ->
            total + item.resistances
        }
        val itemImmunities = equippedItems.fold(LunarimImmunities()) { total, item ->
            total + item.immunities
        }

        var effectArmorFlat = 0
        var effectArmorPercent = 0
        var effectMaxHealth = 0
        var effectMaxMana = 0
        var effectMaxActionPoints = 0
        var effectActionPointsPerTurn = 0
        var effectCarryCapacity = 0
        var effectDodge = 0
        var effectCrit = 0
        var effectPhysicalDamage = 0
        var effectRangedDamage = 0
        var effectMagicDamage = 0
        var effectResistances = LunarimResistances()
        var effectImmunities = LunarimImmunities()

        activeDefinitions.forEach { (active, definition) ->
            val multiplier = (active.stacks.coerceAtLeast(1) * active.strength).coerceAtLeast(0f)
            val m = definition.modifiers

            effectArmorFlat += (m.armorFlat * multiplier).toInt()
            effectArmorPercent += (m.armorPercent * multiplier).toInt()
            effectMaxHealth += (m.maxHealthFlat * multiplier).toInt()
            effectMaxMana += (m.maxManaFlat * multiplier).toInt()
            effectMaxActionPoints += (m.maxActionPointsFlat * multiplier).toInt()
            effectActionPointsPerTurn += (m.actionPointsPerTurnFlat * multiplier).toInt()
            effectCarryCapacity += (m.carryCapacityGramsFlat * multiplier).toInt()
            effectDodge += (m.dodgePercent * multiplier).toInt()
            effectCrit += (m.criticalChancePercent * multiplier).toInt()
            effectPhysicalDamage += (m.physicalDamagePercent * multiplier).toInt()
            effectRangedDamage += (m.rangedDamagePercent * multiplier).toInt()
            effectMagicDamage += (m.magicDamagePercent * multiplier).toInt()
            effectResistances += m.resistances
            effectImmunities += m.immunities
        }

        val baseArmor = itemArmor + attributes.endurance
        val armor = (baseArmor * (100 + effectArmorPercent) / 100) + effectArmorFlat

        return LunarimCalculatedStats(
            maxHealth = (75 + attributes.endurance * 10 + effectMaxHealth).coerceAtLeast(1),
            maxMana = (20 + attributes.intelligence * 8 + effectMaxMana).coerceAtLeast(0),
            maxActionPoints = (3 + attributes.agility / 4 + effectMaxActionPoints).coerceAtLeast(1),
            actionPointsPerTurn = (2 + attributes.agility / 6 + effectActionPointsPerTurn).coerceAtLeast(0),
            carryCapacityGrams = (
                    10_000 + attributes.strength * 2_500 + effectCarryCapacity
                    ).coerceAtLeast(0),
            armor = armor.coerceAtLeast(0),
            dodgePercent = (attributes.agility + attributes.luck / 2 + effectDodge)
                .coerceIn(0, 75),
            criticalChancePercent = (5 + attributes.luck + attributes.perception / 2 + effectCrit)
                .coerceIn(0, 95),
            physicalDamageBonusPercent = (
                    attributes.strength * 2 +
                            character.skills.combat.closeCombat +
                            effectPhysicalDamage
                    ).coerceAtLeast(0),
            rangedDamageBonusPercent = (
                    attributes.perception * 2 +
                            character.skills.combat.longRangedWeapon +
                            effectRangedDamage
                    ).coerceAtLeast(0),
            magicDamageBonusPercent = (
                    attributes.intelligence * 2 +
                            effectMagicDamage
                    ).coerceAtLeast(0),
            resistances = (itemResistances + effectResistances).clamped(),
            immunities = itemImmunities + effectImmunities
        )
    }
}
