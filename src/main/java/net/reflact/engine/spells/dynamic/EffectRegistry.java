package net.reflact.engine.spells.dynamic;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class EffectRegistry {
    private static final Map<String, Supplier<SpellEffect>> REGISTRY = new HashMap<>();

    static {
        register("damage", DamageEffect::new);
        register("projectile", ProjectileEffect::new);
    }

    public static void register(String type, Supplier<SpellEffect> factory) {
        REGISTRY.put(type, factory);
    }

    public static SpellEffect create(String type) {
        Supplier<SpellEffect> supplier = REGISTRY.get(type);
        return supplier != null ? supplier.get() : null;
    }
}
