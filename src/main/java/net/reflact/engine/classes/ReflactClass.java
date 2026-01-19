package net.reflact.engine.classes;

import net.reflact.common.attribute.Attribute;
import java.util.Map;
import java.util.HashMap;

public enum ReflactClass {
    WARRIOR("Warrior", 
        Map.of("health", 150.0, "defense", 20.0, "damage", 5.0)
    ),
    ARCHER("Archer", 
        Map.of("health", 100.0, "defense", 5.0, "walk_speed", 0.15)
    ),
    MAGE("Mage", 
        Map.of("health", 80.0, "intelligence", 20.0, "mana", 150.0)
    ),
    ASSASSIN("Assassin", 
        Map.of("health", 90.0, "crit_chance", 10.0, "damage", 10.0)
    ),
    SHAMAN("Shaman", 
        Map.of("health", 120.0, "defense", 10.0, "damage", 8.0)
    );

    private final String displayName;
    private final Map<String, Double> baseStats;

    ReflactClass(String displayName, Map<String, Double> baseStats) {
        this.displayName = displayName;
        this.baseStats = baseStats;
    }
    
    public String getDisplayName() { return displayName; }
    public Map<String, Double> getBaseStats() { return baseStats; }
    
    public static ReflactClass fromString(String name) {
        try {
            return valueOf(name.toUpperCase());
        } catch (Exception e) {
            return WARRIOR; // Default
        }
    }
}
