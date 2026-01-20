package net.reflact.engine.spells

import net.minestom.server.entity.Player
import net.minestom.server.entity.attribute.Attribute
import net.minestom.server.network.packet.server.play.ParticlePacket
import net.minestom.server.particle.Particle
import net.reflact.common.attribute.RpgAttributes
import net.reflact.engine.ReflactEngine

class HealSpell : Spell {
    override val id: String = "heal"
    override val name: String = "Minor Heal"
    override val cooldownMillis: Long = 5000
    override val manaCost: Double = 25.0

    override fun onCast(caster: Player) {
        val data = ReflactEngine.getPlayerManager().getPlayer(caster.uuid) ?: return

        val intel = data.attributes.getValue(RpgAttributes.INTELLIGENCE)
        val healAmount = 10.0 + (intel * 0.5)

        val maxHealth = caster.getAttribute(Attribute.MAX_HEALTH).value
        caster.health = (caster.health + healAmount.toFloat()).coerceAtMost(maxHealth.toFloat())

        // Visuals
        caster.sendPacketToViewersAndSelf(
            ParticlePacket(
                Particle.HEART,
                caster.position.x, caster.position.y + 2, caster.position.z,
                0.5f, 0.5f, 0.5f,
                0f, 10
            )
        )

        caster.sendMessage("§aYou healed yourself for " + String.format("%.1f", healAmount) + " HP!")
    }
}
