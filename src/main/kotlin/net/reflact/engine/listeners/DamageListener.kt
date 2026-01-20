package net.reflact.engine.listeners

import net.minestom.server.entity.LivingEntity
import net.minestom.server.entity.Player
import net.minestom.server.entity.damage.Damage
import net.minestom.server.entity.damage.DamageType
import net.minestom.server.event.GlobalEventHandler
import net.minestom.server.event.entity.EntityAttackEvent
import net.reflact.common.attribute.RpgAttributes
import net.reflact.common.network.packet.DamageIndicatorPacket
import net.reflact.engine.ReflactEngine
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.PI

object DamageListener {
    fun register(handler: GlobalEventHandler) {
        handler.addListener(EntityAttackEvent::class.java) { event ->
            if (event.entity !is Player) return@addListener
            val attacker = event.entity as Player
            
            if (event.target !is LivingEntity) return@addListener
            val target = event.target as LivingEntity

            // Cancel vanilla attack to override damage calculation
            // Note: This removes vanilla knockback. We can re-add it if needed.
            // event.setCancelled(true); 
            
            val attackerData = ReflactEngine.getPlayerManager().getPlayer(attacker.uuid) ?: return@addListener

            // 1. Calculate Damage
            val baseDamage = attackerData.attributes.getValue(RpgAttributes.ATTACK_DAMAGE)
            val strength = attackerData.attributes.getValue(RpgAttributes.STRENGTH)

            // Simple formula: Damage = Base + (Strength * 0.1)
            var finalDamage = baseDamage + (strength * 0.1)

            // 2. Apply Defense (if target is player)
            if (target is Player) {
                val targetData = ReflactEngine.getPlayerManager().getPlayer(target.uuid)
                if (targetData != null) {
                    val defense = targetData.attributes.getValue(RpgAttributes.DEFENSE)
                    val reduction = defense / (defense + 100.0)
                    finalDamage *= (1.0 - reduction)
                }
            }

            // 3. Apply Damage
            target.damage(Damage(DamageType.PLAYER_ATTACK, attacker, attacker, null, finalDamage.toFloat()))

            // 4. Send Damage Indicator Packet
            val packet = DamageIndicatorPacket(target.entityId, finalDamage, false)

            val viewers = HashSet(target.viewers)
            viewers.add(attacker)
            if (target is Player) viewers.add(target)

            for (p in viewers) {
                ReflactEngine.getNetworkManager().sendPacket(p, packet)
            }

            // Apply small Knockback
            target.takeKnockback(0.4f, sin(attacker.position.yaw * PI / 180), -cos(attacker.position.yaw * PI / 180))
        }
    }
}
