package de.meson_labs.luna_coin.lunarim.manager

import de.meson_labs.luna_coin.lunarim.data.LunarimEffects
import de.meson_labs.luna_coin.lunarim.data.LunarimItems
import de.meson_labs.luna_coin.lunarim.models.*
import kotlin.random.Random

class LunarimGameManager {

    fun createNewGame(childId: String): LunarimGameState =
        LunarimGameState.newGame(childId)

    fun resetGame(childId: String): LunarimGameState =
        LunarimGameState.resetForChild(childId)

    fun addItem(
        state: LunarimGameState,
        itemId: String,
        amount: Int = 1,
        instanceId: String
    ): LunarimGameState {
        if (amount <= 0 || LunarimItems.getById(itemId) == null) return state

        val item = LunarimItems.getById(itemId) ?: return state
        val inventory = state.character.inventory.toMutableList()

        if (item.stackable) {
            val index = inventory.indexOfFirst { it.itemId == itemId && it.amount < item.maxStackSize }
            if (index >= 0) {
                val current = inventory[index]
                inventory[index] = current.copy(
                    amount = (current.amount + amount).coerceAtMost(item.maxStackSize)
                )
            } else {
                inventory += LunarimItemInstance(
                    instanceId = instanceId,
                    itemId = itemId,
                    amount = amount.coerceAtMost(item.maxStackSize)
                )
            }
        } else {
            repeat(amount) { index ->
                inventory += LunarimItemInstance(
                    instanceId = if (index == 0) instanceId else "${instanceId}_$index",
                    itemId = itemId
                )
            }
        }

        return state.copy(
            character = state.character.copy(inventory = inventory)
        )
    }

    fun removeItemInstance(
        state: LunarimGameState,
        instanceId: String
    ): LunarimGameState {
        val newInventory = state.character.inventory.filterNot { it.instanceId == instanceId }
        val newEquipment = LunarimEquipment(
            equippedInstanceIds = state.character.equipment.equippedInstanceIds
                .filterValues { it != instanceId }
        )

        return state.copy(
            character = state.character.copy(
                inventory = newInventory,
                equipment = newEquipment
            )
        )
    }

    fun equip(
        state: LunarimGameState,
        slot: LunarimEquipmentSlot,
        instanceId: String
    ): LunarimGameState {
        if (state.character.inventory.none { it.instanceId == instanceId }) return state

        return state.copy(
            character = state.character.copy(
                equipment = state.character.equipment.equip(slot, instanceId)
            )
        )
    }

    fun calculateInventoryWeightGrams(state: LunarimGameState): Int =
        state.character.inventory.sumOf { instance ->
            (LunarimItems.getById(instance.itemId)?.weightGrams ?: 0) * instance.amount
        }

    fun applyEffect(
        state: LunarimGameState,
        effectId: String,
        nowEpochMillis: Long,
        durationOverride: LunarimEffectDuration? = null,
        strength: Float = 1f
    ): LunarimGameState {
        val definition = LunarimEffects.getById(effectId) ?: return state
        val duration = durationOverride ?: definition.defaultDuration
        val current = state.character.activeEffects.firstOrNull { it.effectId == effectId }

        val expiresAt = duration.outsideCombatDurationMillis?.let(nowEpochMillis::plus)
        val turns = duration.combatDurationTurns

        val updated = if (current == null) {
            state.character.activeEffects + LunarimActiveEffect(
                effectId = effectId,
                appliedAtEpochMillis = nowEpochMillis,
                expiresAtEpochMillis = expiresAt,
                remainingCombatTurns = turns,
                stacks = 1,
                strength = strength
            )
        } else if (definition.stackable) {
            state.character.activeEffects.map { active ->
                if (active.effectId == effectId) {
                    active.copy(
                        appliedAtEpochMillis = nowEpochMillis,
                        expiresAtEpochMillis = expiresAt,
                        remainingCombatTurns = turns,
                        stacks = (active.stacks + 1).coerceAtMost(definition.maxStacks),
                        strength = maxOf(active.strength, strength)
                    )
                } else {
                    active
                }
            }
        } else {
            state.character.activeEffects.map { active ->
                if (active.effectId == effectId) {
                    active.copy(
                        appliedAtEpochMillis = nowEpochMillis,
                        expiresAtEpochMillis = expiresAt,
                        remainingCombatTurns = turns,
                        strength = strength
                    )
                } else {
                    active
                }
            }
        }

        return state.copy(
            character = state.character.copy(activeEffects = updated)
        )
    }

    fun applySpellEffects(
        state: LunarimGameState,
        spell: LunarimSpell,
        nowEpochMillis: Long,
        random: Random = Random.Default
    ): LunarimGameState {
        return spell.effects.fold(state) { currentState, spellEffect ->
            if (random.nextFloat() <= spellEffect.chance) {
                applyEffect(
                    state = currentState,
                    effectId = spellEffect.effectId,
                    nowEpochMillis = nowEpochMillis,
                    durationOverride = spell.duration,
                    strength = spellEffect.strength
                )
            } else {
                currentState
            }
        }
    }

    fun removeExpiredOutsideCombatEffects(
        state: LunarimGameState,
        nowEpochMillis: Long
    ): LunarimGameState =
        state.copy(
            character = state.character.copy(
                activeEffects = state.character.activeEffects.filterNot {
                    it.isExpiredOutsideCombat(nowEpochMillis)
                }
            )
        )

    fun advanceCombatTurn(state: LunarimGameState): LunarimGameState {
        val updated = state.character.activeEffects
            .map { active ->
                active.copy(
                    remainingCombatTurns = active.remainingCombatTurns?.minus(1)
                )
            }
            .filterNot { it.isExpiredInCombat() }

        return state.copy(
            character = state.character.copy(activeEffects = updated)
        )
    }
}
