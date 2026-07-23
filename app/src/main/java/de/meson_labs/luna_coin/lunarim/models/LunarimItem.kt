package de.meson_labs.luna_coin.lunarim.models

import kotlinx.serialization.Serializable

@Serializable
enum class LunarimItemType {
    WEAPON,
    AMMUNITION,
    ARMOR,
    SHIELD,
    CONSUMABLE,
    MATERIAL,
    QUEST_ITEM
}

@Serializable
enum class LunarimWeaponType {
    NONE,
    SWORD,
    AXE,
    HAMMER,
    SPEAR,
    DAGGER,
    BOW,
    CROSSBOW,
    STAFF,
    WAND,
    THROWING
}

@Serializable
enum class LunarimItemRarity {
    COMMON,
    UNCOMMON,
    RARE,
    EPIC,
    LEGENDARY
}

@Serializable
data class LunarimItem(
    val id: String,
    val name: String,
    val description: String,
    val type: LunarimItemType,
    val weightGrams: Int,
    val buyPriceSilver: Int,
    val sellPriceSilver: Int,
    val stackable: Boolean = false,
    val maxStackSize: Int = 1,
    val weaponType: LunarimWeaponType = LunarimWeaponType.NONE,
    val damage: LunarimDamageProfile? = null,
    val armor: Int = 0,
    val resistances: LunarimResistances = LunarimResistances(),
    val immunities: LunarimImmunities = LunarimImmunities(),
    val useEffectIds: List<String> = emptyList()
)

@Serializable
data class LunarimItemInstance(
    val instanceId: String,
    val itemId: String,
    val amount: Int = 1,
    val durability: Int = 100,
    val maxDurability: Int = 100,
    val rarity: LunarimItemRarity = LunarimItemRarity.COMMON,
    val enchantmentIds: List<String> = emptyList(),
    val runeIds: List<String> = emptyList(),
    val customName: String? = null
)
