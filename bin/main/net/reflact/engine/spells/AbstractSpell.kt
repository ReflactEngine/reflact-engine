package net.reflact.engine.spells

import net.minestom.server.entity.Player
import net.kyori.adventure.sound.Sound
import net.minestom.server.sound.SoundEvent

abstract class AbstractSpell(
    override val id: String,
    override val name: String,
    override val cooldownMillis: Long,
    override val manaCost: Double
) : Spell {

    // Helper methods for subclasses
    protected fun sendMessage(player: Player, message: String) {
        player.sendMessage(message)
    }
}
