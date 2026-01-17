package net.reflact.engine.spells.dynamic;

import com.google.gson.JsonObject;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;

public class ProjectileEffect implements SpellEffect {
    private double speed;
    private String type; // fireball, snowball, etc.

    @Override
    public void execute(CastContext context) {
        EntityType entityType = EntityType.FIREBALL;
        if ("snowball".equalsIgnoreCase(type)) entityType = EntityType.SNOWBALL;
        
        Entity projectile = new Entity(entityType);
        projectile.setInstance(context.getInstance(), context.getOrigin());
        
        Vec direction = context.getCaster().getPosition().direction();
        projectile.setVelocity(direction.mul(speed * 20)); // Minestom velocity is per tick?
        
        // In a real system, we would register this projectile to a ProjectileManager 
        // to handle collision -> trigger subsequent effects.
        // For now, we launch it.
    }

    @Override
    public void load(JsonObject config) {
        this.speed = config.has("speed") ? config.get("speed").getAsDouble() : 1.0;
        this.type = config.has("entity_type") ? config.get("entity_type").getAsString() : "fireball";
    }
}
