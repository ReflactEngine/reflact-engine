package net.reflact.engine.listeners;

import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.entity.EntityAttackEvent;
import net.reflact.common.attribute.RpgAttributes;
import net.reflact.engine.ReflactEngine;
import net.reflact.engine.data.ReflactPlayer;

public class DamageListener {
    public static void register(GlobalEventHandler handler) {
        handler.addListener(EntityAttackEvent.class, event -> {
            if (!(event.getEntity() instanceof Player attacker)) return;
            if (!(event.getTarget() instanceof LivingEntity target)) return;
            
            // Cancel vanilla attack to override damage calculation
            // Note: This removes vanilla knockback. We can re-add it if needed.
            // event.setCancelled(true); // Minestom: Cancelling this might stop the attack packet? 
            // Actually, in Minestom, EntityAttackEvent is purely informational/cancellable for the 'attack' action. 
            // It does NOT automatically deal damage in all versions. 
            // But to be safe and ensure we don't double damage, we rely on our manual damage call.
            
            ReflactPlayer attackerData = ReflactEngine.getPlayerManager().getPlayer(attacker.getUuid());
            if (attackerData == null) return;
            
            // 1. Calculate Damage
            double baseDamage = attackerData.getAttributes().getValue(RpgAttributes.ATTACK_DAMAGE);
            double strength = attackerData.getAttributes().getValue(RpgAttributes.STRENGTH);
            
            // Simple formula: Damage = Base + (Strength * 0.1)
            double finalDamage = baseDamage + (strength * 0.1);
            
            // 2. Apply Defense (if target is player)
            if (target instanceof Player targetPlayer) {
                ReflactPlayer targetData = ReflactEngine.getPlayerManager().getPlayer(targetPlayer.getUuid());
                if (targetData != null) {
                    double defense = targetData.getAttributes().getValue(RpgAttributes.DEFENSE);
                    double reduction = defense / (defense + 100.0);
                    finalDamage *= (1.0 - reduction);
                }
            }
            
            // 3. Apply Damage
            target.damage(new Damage(DamageType.PLAYER_ATTACK, attacker, attacker, null, (float) finalDamage));
            
            // Apply small Knockback (manually, since we might be cancelling vanilla logic or Minestom doesn't do it auto for custom damage)
            target.takeKnockback(0.4f, Math.sin(attacker.getPosition().yaw() * Math.PI / 180), -Math.cos(attacker.getPosition().yaw() * Math.PI / 180));
        });
    }
}
