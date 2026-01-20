package net.reflact.engine.spells.dynamic

import com.google.gson.JsonObject
import net.minestom.server.entity.LivingEntity
import net.reflact.common.attribute.RpgAttributes
import net.reflact.engine.ReflactEngine

class DamageEffect : SpellEffect {
    private var baseDamage: Double = 0.0
    private var scaling: Double = 0.0

    override fun execute(context: CastContext) {
        val target = context.target
        if (target is LivingEntity) {
            var damage = baseDamage

            // Apply Intelligence Scaling
            val casterData = ReflactEngine.getPlayerManager().getPlayer(context.caster.uuid)
            if (casterData != null) {
                val intel = casterData.attributes.getValue(RpgAttributes.INTELLIGENCE)
                damage += (intel * scaling)
            }

            target.damage(net.minestom.server.entity.damage.DamageType.MAGIC, damage.toFloat())
        }
    }

    override fun load(config: JsonObject) {
        this.baseDamage = if (config.has("damage")) config.get("damage").asDouble else 5.0
        this.scaling = if (config.has("scaling")) config.get("scaling").asDouble else 0.0
    }
}
