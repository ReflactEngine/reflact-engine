package net.reflact.engine.spells

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.minestom.server.MinecraftServer
import net.minestom.server.entity.Entity
import net.minestom.server.entity.EntityType
import net.minestom.server.entity.LivingEntity
import net.minestom.server.entity.Player
import net.minestom.server.entity.damage.Damage
import net.minestom.server.entity.damage.DamageType
import net.minestom.server.network.packet.server.play.ParticlePacket
import net.minestom.server.particle.Particle
import net.minestom.server.timer.TaskSchedule
import net.reflact.common.attribute.RpgAttributes
import net.reflact.engine.ReflactEngine

class FireballSpell : Spell {
    override val id: String = "fireball"
    override val name: String = "Fireball"
    override val cooldownMillis: Long = 1000
    override val manaCost: Double = 10.0

    override fun onCast(caster: Player) {
        val instance = caster.instance ?: return

        val data = ReflactEngine.getPlayerManager().getPlayer(caster.uuid)
        val intel = data?.attributes?.getValue(RpgAttributes.INTELLIGENCE) ?: 0.0
        val damage = 5.0 + (intel * 0.2)

        val startPos = caster.position.add(0.0, caster.eyeHeight, 0.0)
        val direction = startPos.direction()

        val fireball = Entity(EntityType.FIREBALL)
        fireball.setNoGravity(true) // Fix "weird physics" / dropping
        fireball.setInstance(instance, startPos)

        // Set velocity for client-side interpolation smoothness
        fireball.velocity = direction.mul(20.0) // 20 blocks/sec speed estimate for client

        // Manual Projectile Logic
        val spawnTime = System.currentTimeMillis()
        MinecraftServer.getSchedulerManager().submitTask {
            if (fireball.isRemoved) return@submitTask TaskSchedule.stop()

            // 1. Despawn after 5 seconds
            if (System.currentTimeMillis() - spawnTime > 5000) {
                fireball.remove()
                return@submitTask TaskSchedule.stop()
            }

            // 2. Move
            val speed = 1.0 // Blocks per tick (20 blocks/sec)
            val current = fireball.position
            val next = current.add(direction.mul(speed))

            // Update velocity for client to keep predicting correctly
            fireball.velocity = direction.mul(20.0)

            // 3. Collision Check (Simple Raycast logic)
            // Check for entities nearby
            val nearby = instance.getNearbyEntities(next, 1.5) // Increased radius slightly
            for (target in nearby) {
                if (target !== caster && target !== fireball && target is LivingEntity) {
                    // Hit!
                    target.damage(
                        Damage(
                            DamageType.ON_FIRE,
                            fireball,
                            caster,
                            null,
                            damage.toFloat()
                        )
                    )
                    fireball.remove()

                    // Explosion Effect
                    instance.sendGroupedPacket(
                        ParticlePacket(
                            Particle.EXPLOSION,
                            next.x(), next.y(), next.z(),
                            0.5f, 0.5f, 0.5f, 0f, 10
                        )
                    )

                    caster.sendMessage(
                        Component.text(
                            "Hit " + target.entityType.name().lowercase() + " for " + damage.toInt() + " damage!",
                            NamedTextColor.GREEN
                        )
                    )

                    return@submitTask TaskSchedule.stop()
                }
            }

            // Check for block collision
            if (instance.getBlock(next).isSolid) {
                fireball.remove()
                instance.sendGroupedPacket(
                    ParticlePacket(
                        Particle.EXPLOSION,
                        next.x(), next.y(), next.z(),
                        0.5f, 0.5f, 0.5f, 0f, 10
                    )
                )
                return@submitTask TaskSchedule.stop()
            }

            fireball.teleport(next)
            TaskSchedule.tick(1)
        }

        caster.sendMessage(
            Component.text(
                "You cast Fireball! (Est. Damage: " + String.format("%.1f", damage) + " [Intel: " + intel.toInt() + "])",
                NamedTextColor.GOLD
            )
        )
    }
}
