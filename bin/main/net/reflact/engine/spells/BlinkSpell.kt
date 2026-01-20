package net.reflact.engine.spells

import net.minestom.server.coordinate.Pos
import net.minestom.server.entity.Player
import net.minestom.server.particle.Particle

class BlinkSpell : AbstractSpell("blink", "Blink", 8000, 30.0) {

    override fun onCast(caster: Player) {
        val instance = caster.instance ?: return
        val start = caster.position
        val dir = start.direction()
        
        var targetPos = start
        val distance = 8.0
        
        // Check for blocks
        // Simple check: end point
        val end = start.add(dir.mul(distance))
        
        // Ideally raycast for blocks to not clip into wall
        // For now, simple teleport
        
        caster.teleport(end)
        
        instance.sendGroupedPacket(
            net.minestom.server.network.packet.server.play.ParticlePacket(
                Particle.PORTAL,
                start.x(), start.y(), start.z(),
                0.5f, 1.0f, 0.5f, 0.5f, 30
            )
        )
        
        instance.sendGroupedPacket(
            net.minestom.server.network.packet.server.play.ParticlePacket(
                Particle.PORTAL,
                end.x(), end.y(), end.z(),
                0.5f, 1.0f, 0.5f, 0.5f, 30
            )
        )
        
        sendMessage(caster, "§d*Blink*")
    }
}
