package net.reflact.engine.spells

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.minestom.server.entity.LivingEntity
import net.minestom.server.entity.Player
import net.minestom.server.entity.damage.Damage
import net.minestom.server.entity.damage.DamageType
import net.minestom.server.particle.Particle
import net.reflact.common.attribute.RpgAttributes
import net.reflact.engine.ReflactEngine

class BashSpell : AbstractSpell("bash", "Bash", 3000, 15.0) {

    override fun onCast(caster: Player) {
        val instance = caster.instance ?: return
        val data = ReflactEngine.getPlayerManager().getPlayer(caster.uuid)
        val strength = data?.attributes?.getValue(RpgAttributes.ATTACK_DAMAGE) ?: 5.0
        val damage = strength * 1.5

        val target = getTarget(caster, 3.0)
        
        if (target != null && target is LivingEntity) {
            target.damage(Damage(DamageType.PLAYER_ATTACK, caster, caster, null, damage.toFloat()))
            
            // Knockback
            val dir = caster.position.direction()
            target.takeKnockback(0.5f, -Math.sin(caster.position.yaw * Math.PI / 180.0), Math.cos(caster.position.yaw * Math.PI / 180.0))
            
            // Particle
            instance.sendGroupedPacket(
                net.minestom.server.network.packet.server.play.ParticlePacket(
                    Particle.CRIT,
                    target.position.x(), target.position.y() + 1, target.position.z(),
                    0.5f, 0.5f, 0.5f, 0.1f, 10
                )
            )

            sendMessage(caster, "§aYou bashed " + (if (target is Player) target.username else target.entityType.name()) + " for " + damage.toInt() + " damage!")
        } else {
            sendMessage(caster, "§cNo target in range!")
            // Refund mana? logic not here yet
        }
    }

    private fun getTarget(player: Player, range: Double): net.minestom.server.entity.Entity? {
        val start = player.position.add(0.0, player.eyeHeight, 0.0)
        val dir = start.direction()
        val instance = player.instance ?: return null
        
        // Simple raycast for entities
        var current = start
        val step = 0.5
        var dist = 0.0
        
        while (dist < range) {
            current = current.add(dir.mul(step))
            dist += step
            
            val entities = instance.getNearbyEntities(current, 0.8)
            for (e in entities) {
                if (e != player && e is LivingEntity) {
                    return e
                }
            }
        }
        return null
    }
}
