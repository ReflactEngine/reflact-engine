package net.reflact.engine.spells

import net.minestom.server.entity.Player
import net.minestom.server.particle.Particle

class DashSpell : AbstractSpell("charge", "Charge", 5000, 20.0) {

    override fun onCast(caster: Player) {
        val dir = caster.position.direction()
        caster.velocity = dir.mul(25.0) // Boost
        
        sendMessage(caster, "§eCharge!")
        
        // Trail
        caster.instance?.sendGroupedPacket(
            net.minestom.server.network.packet.server.play.ParticlePacket(
                Particle.CLOUD,
                caster.position.x(), caster.position.y(), caster.position.z(),
                0.5f, 0.5f, 0.5f, 0.1f, 20
            )
        )
    }
}
