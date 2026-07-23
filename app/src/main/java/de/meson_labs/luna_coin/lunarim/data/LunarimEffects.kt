package de.meson_labs.luna_coin.lunarim.data

import de.meson_labs.luna_coin.lunarim.models.*

object LunarimEffects {

    const val BURNING_ID = "burning"
    const val COLD_SLOW_ID = "cold_slow"
    const val POISONED_ID = "poisoned"
    const val BLEEDING_ID = "bleeding"
    const val STUNNED_ID = "stunned"
    const val SHOCKED_ID = "shocked"
    const val ARCANE_INSTABILITY_ID = "arcane_instability"
    const val ARCANE_SHIELD_ID = "arcane_shield"
    const val HOLY_PROTECTION_ID = "holy_protection"
    const val STRENGTHENED_ID = "strengthened"
    const val WELL_RESTED_ID = "well_rested"

    const val VAMPIRISM_ID = "disease_vampirism"
    const val SYPHILIS_ID = "disease_syphilis"
    const val HERPES_ID = "disease_herpes"
    const val LEPROSY_ID = "disease_leprosy"

    val burning = LunarimEffect(
        id = BURNING_ID,
        name = "Brennend",
        description = "Verursacht Feuerschaden über Zeit.",
        category = LunarimEffectCategory.FIRE,
        defaultDuration = LunarimEffectDuration(90_000L, 3),
        stackable = true,
        maxStacks = 3,
        tickType = LunarimEffectTickType.DAMAGE,
        tickAmount = 4
    )

    val coldSlow = LunarimEffect(
        id = COLD_SLOW_ID,
        name = "Unterkühlt",
        description = "Verringert die Aktionspunkte pro Zug.",
        category = LunarimEffectCategory.COLD,
        defaultDuration = LunarimEffectDuration(120_000L, 2),
        modifiers = LunarimEffectModifiers(actionPointsPerTurnFlat = -1)
    )

    val poisoned = LunarimEffect(
        id = POISONED_ID,
        name = "Vergiftet",
        description = "Verursacht Giftschaden über Zeit.",
        category = LunarimEffectCategory.POISON,
        defaultDuration = LunarimEffectDuration(180_000L, 4),
        stackable = true,
        maxStacks = 5,
        tickType = LunarimEffectTickType.DAMAGE,
        tickAmount = 3
    )

    val bleeding = LunarimEffect(
        id = BLEEDING_ID,
        name = "Blutung",
        description = "Verursacht Blutungsschaden über Zeit.",
        category = LunarimEffectCategory.BLEEDING,
        defaultDuration = LunarimEffectDuration(120_000L, 3),
        stackable = true,
        maxStacks = 4,
        tickType = LunarimEffectTickType.DAMAGE,
        tickAmount = 4
    )

    val stunned = LunarimEffect(
        id = STUNNED_ID,
        name = "Betäubt",
        description = "Verhindert kurzfristig Aktionen.",
        category = LunarimEffectCategory.STUN,
        defaultDuration = LunarimEffectDuration(10_000L, 1),
        modifiers = LunarimEffectModifiers(actionPointsPerTurnFlat = -99)
    )

    val shocked = LunarimEffect(
        id = SHOCKED_ID,
        name = "Elektrisiert",
        description = "Verringert Aktionspunkte und Blitzresistenz.",
        category = LunarimEffectCategory.LIGHTNING,
        defaultDuration = LunarimEffectDuration(60_000L, 2),
        modifiers = LunarimEffectModifiers(
            actionPointsPerTurnFlat = -1,
            resistances = LunarimResistances(lightning = -10)
        )
    )

    val arcaneInstability = LunarimEffect(
        id = ARCANE_INSTABILITY_ID,
        name = "Arkane Instabilität",
        description = "Senkt die arkane Resistenz.",
        category = LunarimEffectCategory.ARCANE,
        defaultDuration = LunarimEffectDuration(120_000L, 3),
        modifiers = LunarimEffectModifiers(
            resistances = LunarimResistances(arcane = -15)
        )
    )

    val arcaneShield = LunarimEffect(
        id = ARCANE_SHIELD_ID,
        name = "Arkanschild",
        description = "Erhöht Rüstung und arkane Resistenz.",
        category = LunarimEffectCategory.BUFF,
        defaultDuration = LunarimEffectDuration(300_000L, 5),
        modifiers = LunarimEffectModifiers(
            armorFlat = 20,
            resistances = LunarimResistances(arcane = 30)
        )
    )

    val holyProtection = LunarimEffect(
        id = HOLY_PROTECTION_ID,
        name = "Göttlicher Schutz",
        description = "Schützt vor heiligem und Schatten-Schaden.",
        category = LunarimEffectCategory.BLESSING,
        defaultDuration = LunarimEffectDuration(600_000L, 6),
        modifiers = LunarimEffectModifiers(
            armorFlat = 10,
            resistances = LunarimResistances(holy = 25, shadow = 25)
        )
    )

    val strengthened = LunarimEffect(
        id = STRENGTHENED_ID,
        name = "Gestärkt",
        description = "Erhöht den physischen Schaden.",
        category = LunarimEffectCategory.BUFF,
        defaultDuration = LunarimEffectDuration(300_000L, 5),
        modifiers = LunarimEffectModifiers(physicalDamagePercent = 15)
    )

    val wellRested = LunarimEffect(
        id = WELL_RESTED_ID,
        name = "Ausgeruht",
        description = "Erhöht Gesundheit und Aktionspunkte.",
        category = LunarimEffectCategory.BUFF,
        defaultDuration = LunarimEffectDuration(1_800_000L, 8),
        modifiers = LunarimEffectModifiers(
            maxHealthFlat = 15,
            maxActionPointsFlat = 1
        )
    )

    val vampirism = LunarimEffect(
        id = VAMPIRISM_ID,
        name = "Vampirismus",
        description = "Eine schwere, dauerhafte Krankheit.",
        category = LunarimEffectCategory.DISEASE,
        disease = LunarimDisease.VAMPIRISM
    )

    val syphilis = LunarimEffect(
        id = SYPHILIS_ID,
        name = "Syphilis",
        description = "Eine Krankheit mit langfristigen Auswirkungen.",
        category = LunarimEffectCategory.DISEASE,
        disease = LunarimDisease.SYPHILIS
    )

    val herpes = LunarimEffect(
        id = HERPES_ID,
        name = "Herpes",
        description = "Eine wiederkehrende Krankheit.",
        category = LunarimEffectCategory.DISEASE,
        disease = LunarimDisease.HERPES
    )

    val leprosy = LunarimEffect(
        id = LEPROSY_ID,
        name = "Lepra",
        description = "Eine schwere Krankheit.",
        category = LunarimEffectCategory.DISEASE,
        disease = LunarimDisease.LEPROSY
    )

    val all: List<LunarimEffect> = listOf(
        burning, coldSlow, poisoned, bleeding, stunned, shocked,
        arcaneInstability, arcaneShield, holyProtection,
        strengthened, wellRested, vampirism, syphilis, herpes, leprosy
    )

    private val byId = all.associateBy { it.id }

    fun getById(id: String): LunarimEffect? = byId[id]
}
