package net.reflact.engine.spells.dynamic

import com.google.gson.JsonObject
import net.minestom.server.entity.Entity
import net.minestom.server.entity.EntityType

class ProjectileEffect : SpellEffect {
    private var speed: Double = 0.0
    private var type: String = "" // fireball, snowball, etc.

    override fun execute(context: CastContext) {
        var entityType = EntityType.FIREBALL
        if ("snowball".equals(type, ignoreCase = true)) entityType = EntityType.SNOWBALL

        val projectile = Entity(entityType)
        projectile.setInstance(context.instance, context.origin)

        val direction = context.caster.position.direction()
        projectile.velocity = direction.mul(speed * 20) // Minestom velocity is per tick?

        // In a real system, we would register this projectile to a ProjectileManager 
        // to handle collision -> trigger subsequent effects.
        // For now, we launch it.
    }

    override fun load(config: JsonObject) {
        this.speed = if (config.has("speed")) config.get("speed").asDouble else 1.0
        this.type = if (config.has("entity_type")) config.get("entity_type").asString else "fireball"
    }
}
