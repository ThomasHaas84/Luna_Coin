package de.meson_labs.luna_coin.lunarim.data

import de.meson_labs.luna_coin.lunarim.models.*

object LunarimSpells {

    val fireball = LunarimSpell(
        id = "fireball",
        name = "Feuerball",
        description = "Verursacht Feuerschaden und kann Brennen auslösen.",
        school = LunarimSpellSchool.ELEMENTAL,
        type = LunarimSpellType.DAMAGE,
        manaCost = 12,
        requiredSkillLevel = 5,
        target = LunarimSpellTarget.ENEMY,
        damage = LunarimDamageProfile(
            parts = listOf(
                LunarimDamagePart(LunarimDamageType.FIRE, 24)
            ),
            penetration = LunarimPenetration(fire = 5)
        ),
        effects = listOf(
            LunarimSpellEffect(LunarimEffects.BURNING_ID, chance = 0.30f)
        )
    )

    val frostLance = LunarimSpell(
        id = "frost_lance",
        name = "Frostlanze",
        description = "Verursacht Kälteschaden und kann verlangsamen.",
        school = LunarimSpellSchool.ELEMENTAL,
        type = LunarimSpellType.DAMAGE,
        manaCost = 10,
        requiredSkillLevel = 4,
        target = LunarimSpellTarget.ENEMY,
        damage = LunarimDamageProfile(
            parts = listOf(
                LunarimDamagePart(LunarimDamageType.COLD, 18)
            ),
            penetration = LunarimPenetration(cold = 5)
        ),
        effects = listOf(
            LunarimSpellEffect(LunarimEffects.COLD_SLOW_ID, chance = 0.35f),
            LunarimSpellEffect(LunarimEffects.STUNNED_ID, chance = 0.08f)
        )
    )

    val poisonCloud = LunarimSpell(
        id = "poison_cloud",
        name = "Giftwolke",
        description = "Verursacht Giftschaden und kann alle Ziele im Gebiet vergiften.",
        school = LunarimSpellSchool.BLACK_MAGIC,
        type = LunarimSpellType.DEBUFF,
        manaCost = 14,
        requiredSkillLevel = 6,
        target = LunarimSpellTarget.AREA,
        duration = LunarimEffectDuration(
            outsideCombatDurationMillis = 180_000L,
            combatDurationTurns = 4
        ),
        damage = LunarimDamageProfile(
            parts = listOf(
                LunarimDamagePart(LunarimDamageType.TOXIN, 8)
            ),
            penetration = LunarimPenetration(toxin = 10)
        ),
        effects = listOf(
            LunarimSpellEffect(LunarimEffects.POISONED_ID, chance = 0.75f)
        )
    )

    val chainLightning = LunarimSpell(
        id = "chain_lightning",
        name = "Kettenblitz",
        description = "Lässt Blitze zwischen mehreren Gegnern überspringen.",
        school = LunarimSpellSchool.ELEMENTAL,
        type = LunarimSpellType.DAMAGE,
        manaCost = 16,
        requiredSkillLevel = 8,
        target = LunarimSpellTarget.ALL_ENEMIES,
        damage = LunarimDamageProfile(
            parts = listOf(
                LunarimDamagePart(LunarimDamageType.LIGHTNING, 28)
            ),
            penetration = LunarimPenetration(lightning = 12)
        ),
        effects = listOf(
            LunarimSpellEffect(LunarimEffects.SHOCKED_ID, chance = 0.40f),
            LunarimSpellEffect(LunarimEffects.STUNNED_ID, chance = 0.08f)
        )
    )

    val majorHeal = LunarimSpell(
        id = "major_heal",
        name = "Große Heilung",
        description = "Heilt einen Verbündeten sofort.",
        school = LunarimSpellSchool.LIGHT,
        type = LunarimSpellType.HEAL,
        manaCost = 18,
        requiredSkillLevel = 6,
        target = LunarimSpellTarget.ALLY,
        instantHeal = 80
    )

    val arcaneShield = LunarimSpell(
        id = "arcane_shield",
        name = "Arkanschild",
        description = "Erhöht Rüstung und arkane Resistenz.",
        school = LunarimSpellSchool.ARCANE,
        type = LunarimSpellType.PROTECTION,
        manaCost = 14,
        requiredSkillLevel = 6,
        target = LunarimSpellTarget.SELF,
        castTimeMillis = 1_000L,
        cooldownMillis = 30_000L,
        duration = LunarimEffectDuration(
            outsideCombatDurationMillis = 300_000L,
            combatDurationTurns = 5
        ),
        effects = listOf(
            LunarimSpellEffect(LunarimEffects.ARCANE_SHIELD_ID)
        )
    )

    val holyProtection = LunarimSpell(
        id = "holy_protection",
        name = "Göttlicher Schutz",
        description = "Schützt die gesamte Gruppe vor heiligem und Schatten-Schaden.",
        school = LunarimSpellSchool.HOLY,
        type = LunarimSpellType.PROTECTION,
        manaCost = 22,
        requiredSkillLevel = 10,
        target = LunarimSpellTarget.ALL_ALLIES,
        castTimeMillis = 1_500L,
        cooldownMillis = 60_000L,
        duration = LunarimEffectDuration(
            outsideCombatDurationMillis = 600_000L,
            combatDurationTurns = 6
        ),
        effects = listOf(
            LunarimSpellEffect(LunarimEffects.HOLY_PROTECTION_ID)
        )
    )

    val all = listOf(
        fireball,
        frostLance,
        poisonCloud,
        chainLightning,
        majorHeal,
        arcaneShield,
        holyProtection
    )

    private val byId = all.associateBy { it.id }

    fun getById(id: String): LunarimSpell? = byId[id]
}
