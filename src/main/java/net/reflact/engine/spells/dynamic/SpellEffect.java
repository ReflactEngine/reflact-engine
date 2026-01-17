package net.reflact.engine.spells.dynamic;

import com.google.gson.JsonObject;

public interface SpellEffect {
    /**
     * Executes the effect.
     * @param context The context of the cast.
     */
    void execute(CastContext context);

    /**
     * Loads configuration from JSON.
     */
    void load(JsonObject config);
}
