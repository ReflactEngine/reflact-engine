package net.reflact.engine.spells;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.particle.Particle;
import net.reflact.common.attribute.RpgAttributes;
import net.reflact.engine.ReflactEngine;
import net.reflact.engine.data.ReflactPlayer;

public class FireballSpell implements Spell {

    @Override
    public String getId() {
        return "fireball";
    }

    @Override
    public String getName() {
        return "Fireball";
    }

    @Override
    public long getCooldownMillis() {
        return 1000;
    }

    @Override
    public double getManaCost() {
        return 10;
    }

    @Override
    public void onCast(Player caster) {
        Instance instance = caster.getInstance();
        if (instance == null) return;

        ReflactPlayer data = ReflactEngine.getPlayerManager().getPlayer(caster.getUuid());
        double intel = data != null ? data.getAttributes().getValue(RpgAttributes.INTELLIGENCE) : 0;
        double damage = 5.0 + (intel * 0.2);

        Pos startPos = caster.getPosition().add(0, caster.getEyeHeight(), 0);
        Vec direction = startPos.direction();

        Entity fireball = new Entity(EntityType.FIREBALL);
        fireball.setNoGravity(true); // Fix "weird physics" / dropping
        fireball.setInstance(instance, startPos);
        
        // Set velocity for client-side interpolation smoothness
        fireball.setVelocity(direction.mul(20)); // 20 blocks/sec speed estimate for client
        
        // Manual Projectile Logic
        long spawnTime = System.currentTimeMillis();
        net.minestom.server.MinecraftServer.getSchedulerManager().submitTask(() -> {
            if (fireball.isRemoved()) return net.minestom.server.timer.TaskSchedule.stop();
            
            // 1. Despawn after 5 seconds
            if (System.currentTimeMillis() - spawnTime > 5000) {
                fireball.remove();
                return net.minestom.server.timer.TaskSchedule.stop();
            }

            // 2. Move
            double speed = 1.0; // Blocks per tick (20 blocks/sec)
            Pos current = fireball.getPosition();
            Pos next = current.add(direction.mul(speed));
            
            // Update velocity for client to keep predicting correctly
            fireball.setVelocity(direction.mul(20)); 

            // 3. Collision Check (Simple Raycast logic)
            // Check for entities nearby
            var nearby = instance.getNearbyEntities(next, 1.5); // Increased radius slightly
            for (Entity target : nearby) {
                if (target != caster && target != fireball && target instanceof net.minestom.server.entity.LivingEntity living) {
                    // Hit!
                    living.damage(new net.minestom.server.entity.damage.Damage(net.minestom.server.entity.damage.DamageType.ON_FIRE, fireball, caster, null, (float)damage));
                    fireball.remove();
                    
                    // Explosion Effect
                    instance.sendGroupedPacket(new net.minestom.server.network.packet.server.play.ParticlePacket(
                        Particle.EXPLOSION,
                        next.x(), next.y(), next.z(),
                        0.5f, 0.5f, 0.5f, 0f, 10
                    ));
                    
                    caster.sendMessage(net.kyori.adventure.text.Component.text("Hit " + ((net.minestom.server.entity.LivingEntity)target).getEntityType().name().toLowerCase() + " for " + (int)damage + " damage!", net.kyori.adventure.text.format.NamedTextColor.GREEN));
                    
                    return net.minestom.server.timer.TaskSchedule.stop();
                }
            }
            
            // Check for block collision
            if (instance.getBlock(next).isSolid()) {
                 fireball.remove();
                 instance.sendGroupedPacket(new net.minestom.server.network.packet.server.play.ParticlePacket(
                        Particle.EXPLOSION,
                        next.x(), next.y(), next.z(),
                        0.5f, 0.5f, 0.5f, 0f, 10
                 ));
                 return net.minestom.server.timer.TaskSchedule.stop();
            }

            fireball.teleport(next);
            return net.minestom.server.timer.TaskSchedule.tick(1);
        });

        caster.sendMessage(net.kyori.adventure.text.Component.text("You cast Fireball! (Est. Damage: " + String.format("%.1f", damage) + " [Intel: " + (int)intel + "])", net.kyori.adventure.text.format.NamedTextColor.GOLD));
    }
}
