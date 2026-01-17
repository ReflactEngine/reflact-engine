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
            // Minestom 1.21 Damage API
            target.damage(new Damage(DamageType.PLAYER_ATTACK, attacker, attacker, null, (float) finalDamage));
            
            // Debug message
            // attacker.sendMessage("Dealt " + String.format("%.1f", finalDamage) + " damage!");
        });
    }
}
