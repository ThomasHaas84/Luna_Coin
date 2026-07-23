package de.meson_labs.luna_coin.lunarim.models

data class LunarimCalculatedStats(
    val maxHealth: Int,
    val maxMana: Int,
    val maxActionPoints: Int,
    val actionPointsPerTurn: Int,
    val carryCapacityGrams: Int,
    val armor: Int,
    val dodgePercent: Int,
    val criticalChancePercent: Int,
    val physicalDamageBonusPercent: Int,
    val rangedDamageBonusPercent: Int,
    val magicDamageBonusPercent: Int,
    val resistances: LunarimResistances,
    val immunities: LunarimImmunities
)
