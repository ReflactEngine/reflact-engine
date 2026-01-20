package net.reflact.engine.spells.dynamic

import com.google.gson.JsonObject

interface SpellEffect {
    /**
     * Executes the effect.
     * @param context The context of the cast.
     */
    fun execute(context: CastContext)

    /**
     * Loads configuration from JSON.
     */
    fun load(config: JsonObject)
}
