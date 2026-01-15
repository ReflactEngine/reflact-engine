package net.reflact.engine.attributes;

public class RpgAttributes {
    public static final Attribute HEALTH = new Attribute("health", 20.0);
    public static final Attribute HEALTH_REGEN = new Attribute("health_regen", 1.0);
    public static final Attribute MANA = new Attribute("mana", 100.0);
    public static final Attribute MANA_REGEN = new Attribute("mana_regen", 2.0);
    
    public static final Attribute STRENGTH = new Attribute("strength", 0.0);
    public static final Attribute DEXTERITY = new Attribute("dexterity", 0.0);
    public static final Attribute INTELLIGENCE = new Attribute("intelligence", 0.0);
    public static final Attribute DEFENSE = new Attribute("defense", 0.0);
    public static final Attribute AGILITY = new Attribute("agility", 0.0);
    
    public static final Attribute WALK_SPEED = new Attribute("walk_speed", 0.1); // Minecraft default is roughly 0.1
    public static final Attribute ATTACK_SPEED = new Attribute("attack_speed", 4.0);
    public static final Attribute ATTACK_DAMAGE = new Attribute("attack_damage", 1.0);
    
    public static final Attribute MANA_STEAL = new Attribute("mana_steal", 0.0);
    public static final Attribute LIFE_STEAL = new Attribute("life_steal", 0.0);
    
    public static void registerAll() {
        AttributeRegistry.register(HEALTH);
        AttributeRegistry.register(HEALTH_REGEN);
        AttributeRegistry.register(MANA);
        AttributeRegistry.register(MANA_REGEN);
        AttributeRegistry.register(STRENGTH);
        AttributeRegistry.register(DEXTERITY);
        AttributeRegistry.register(INTELLIGENCE);
        AttributeRegistry.register(DEFENSE);
        AttributeRegistry.register(AGILITY);
        AttributeRegistry.register(WALK_SPEED);
        AttributeRegistry.register(ATTACK_SPEED);
        AttributeRegistry.register(ATTACK_DAMAGE);
        AttributeRegistry.register(MANA_STEAL);
        AttributeRegistry.register(LIFE_STEAL);
    }
}
