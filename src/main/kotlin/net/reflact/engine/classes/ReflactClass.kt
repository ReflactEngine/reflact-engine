package net.reflact.engine.classes

enum class ReflactClass(
    val displayName: String,
    val baseStats: Map<String, Double>
) {
    WARRIOR("Warrior",
        mapOf("health" to 150.0, "defense" to 20.0, "damage" to 5.0)
    ),
    ARCHER("Archer",
        mapOf("health" to 100.0, "defense" to 5.0, "walk_speed" to 0.15)
    ),
    MAGE("Mage",
        mapOf("health" to 80.0, "intelligence" to 20.0, "mana" to 150.0)
    ),
    ASSASSIN("Assassin",
        mapOf("health" to 90.0, "crit_chance" to 10.0, "damage" to 10.0)
    ),
    SHAMAN("Shaman",
        mapOf("health" to 120.0, "defense" to 10.0, "damage" to 8.0)
    );

    companion object {
        fun fromString(name: String): ReflactClass {
            return try {
                valueOf(name.uppercase())
            } catch (e: Exception) {
                WARRIOR // Default
            }
        }
    }
}
