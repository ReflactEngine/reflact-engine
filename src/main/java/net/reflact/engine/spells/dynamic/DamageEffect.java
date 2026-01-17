package net.reflact.engine.spells.dynamic;

import com.google.gson.JsonObject;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.reflact.common.attribute.RpgAttributes;
import net.reflact.engine.ReflactEngine;
import net.reflact.engine.data.ReflactPlayer;

public class DamageEffect implements SpellEffect {
    private double baseDamage;
    private double scaling;

    @Override
    public void execute(CastContext context) {
        Entity target = context.getTarget();
        if (target instanceof LivingEntity living) {
            double damage = baseDamage;
            
            // Apply Intelligence Scaling
            ReflactPlayer casterData = ReflactEngine.getPlayerManager().getPlayer(context.getCaster().getUuid());
            if (casterData != null) {
                double intel = casterData.getAttributes().getValue(RpgAttributes.INTELLIGENCE);
                damage += (intel * scaling);
            }
            
            living.damage(net.minestom.server.entity.damage.DamageType.MAGIC, (float) damage);
        }
    }

    @Override
    public void load(JsonObject config) {
        this.baseDamage = config.has("damage") ? config.get("damage").getAsDouble() : 5.0;
        this.scaling = config.has("scaling") ? config.get("scaling").getAsDouble() : 0.0;
    }
}
