package net.reflact.engine.spells.dynamic

import com.google.gson.JsonObject

class TeleportEffect : SpellEffect {
    private var range = 10.0

    override fun load(config: JsonObject) {
        if (config.has("range")) {
            this.range = config.get("range").asDouble
        }
    }

    override fun execute(context: CastContext) {
        // Raycast to find target location
        // ...

        // Using context caster
        val target = context.caster.position.add(context.caster.position.direction().mul(range))
        context.caster.teleport(target)
        context.caster.sendMessage("Teleported!")
    }
}
