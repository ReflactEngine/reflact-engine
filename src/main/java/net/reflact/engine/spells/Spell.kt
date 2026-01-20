package net.reflact.engine.spells

import net.minestom.server.entity.Player

interface Spell {
    val id: String
    val name: String
    val cooldownMillis: Long
    val manaCost: Double

    /**
     * Called when the spell is successfully cast (cooldown and cost checks passed).
     */
    fun onCast(caster: Player)
}
