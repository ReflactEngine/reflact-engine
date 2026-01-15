package net.reflact.engine.item;

import net.reflact.engine.attributes.Attribute;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class RpgItem {
    private String id;
    private String displayName;
    private List<String> lore;
    private ItemType type;
    private ItemTier tier;
    private int levelRequirement;
    private String classRequirement; // e.g., "Warrior", "Mage"
    
    // Base stats (built-in to the item)
    private Map<String, Double> attributes = new HashMap<>();
    
    // For unique instances (UUID)
    private UUID uuid;

    public RpgItem(String id, String displayName, ItemType type, ItemTier tier) {
        this.id = id;
        this.displayName = displayName;
        this.type = type;
        this.tier = tier;
        this.uuid = UUID.randomUUID();
    }
    
    public void setAttribute(String attributeId, double value) {
        attributes.put(attributeId, value);
    }
    
    public double getAttribute(String attributeId) {
        return attributes.getOrDefault(attributeId, 0.0);
    }

    // Getters and Setters
    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public List<String> getLore() { return lore; }
    public void setLore(List<String> lore) { this.lore = lore; }
    public ItemType getType() { return type; }
    public ItemTier getTier() { return tier; }
    public int getLevelRequirement() { return levelRequirement; }
    public void setLevelRequirement(int levelRequirement) { this.levelRequirement = levelRequirement; }
    public String getClassRequirement() { return classRequirement; }
    public void setClassRequirement(String classRequirement) { this.classRequirement = classRequirement; }
    public Map<String, Double> getAttributes() { return attributes; }
    public UUID getUuid() { return uuid; }
}
