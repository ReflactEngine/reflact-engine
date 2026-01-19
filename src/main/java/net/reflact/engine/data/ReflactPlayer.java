package net.reflact.engine.data;

import net.minestom.server.entity.Player;
import net.reflact.common.attribute.AttributeContainer;

import java.util.UUID;

public class ReflactPlayer {
    private final UUID uuid;
    private final String username;
    private Rank rank;
    private transient boolean buildMode; // Not saved to disk
    private transient AttributeContainer attributes;
    private double currentMana;
    private final java.util.Map<Integer, String> spellSlots = new java.util.HashMap<>();
    private final java.util.Map<String, Integer> questProgress = new java.util.HashMap<>();
    
    // Leveling System
    private String playerClass = "WARRIOR";
    private int level = 1;
    private long xp = 0;
    
    // Accessories (Slot Index -> CustomItem ID)
    // 0-4: Rings (5)
    // 5-7: Bracelets (3)
    // 8-9: Necklaces (2)
    // 10-11: Braces (2)
    private final java.util.Map<Integer, String> accessories = new java.util.HashMap<>();

    public ReflactPlayer(UUID uuid, String username) {
        this.uuid = uuid;
        this.username = username;
        this.rank = Rank.MEMBER;
        this.buildMode = false;
        this.attributes = new AttributeContainer();
        this.currentMana = 100.0; // Default, should be loaded or calculated
        
        // Default Loadout
        spellSlots.put(1, "fireball");
        spellSlots.put(2, "heal");
    }
    
    public void addXp(long amount) {
        this.xp += amount;
        checkLevelUp();
    }
    
    public void checkLevelUp() {
        // Simple curve: Level^2 * 100
        long req = (long)(Math.pow(level, 2) * 100);
        while (this.xp >= req && level < 200) {
            this.xp -= req;
            this.level++;
            req = (long)(Math.pow(level, 2) * 100);
            // Notify player handled by PlayerManager or listener typically
        }
    }
    
    public String getPlayerClass() { return playerClass; }
    public void setPlayerClass(String playerClass) { this.playerClass = playerClass; }
    
    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }
    
    public long getXp() { return xp; }
    public void setXp(long xp) { this.xp = xp; }
    
    public boolean hasQuest(String questId) {
        return getQuestProgress().containsKey(questId);
    }
    
    public int getQuestStage(String questId) {
        return getQuestProgress().getOrDefault(questId, -1);
    }
    
    public void setQuestStage(String questId, int stage) {
        getQuestProgress().put(questId, stage);
    }
    
    public void removeQuest(String questId) {
        getQuestProgress().remove(questId);
    }
    
    public java.util.Set<String> getActiveQuests() {
        return getQuestProgress().keySet();
    }
    
    private java.util.Map<String, Integer> getQuestProgress() {
        if (questProgress == null) {
            // Should happen if deserialized without this field
            try {
                java.lang.reflect.Field field = this.getClass().getDeclaredField("questProgress");
                field.setAccessible(true);
                field.set(this, new java.util.HashMap<String, Integer>());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return questProgress;
    }
    
    public String getSpellInSlot(int slot) {
        if (spellSlots == null) {
            // Lazy init for loaded data that might be missing this field
            try {
                java.lang.reflect.Field field = this.getClass().getDeclaredField("spellSlots");
                field.setAccessible(true);
                field.set(this, new java.util.HashMap<Integer, String>());
                // Set defaults if empty
                this.spellSlots.put(1, "fireball");
                this.spellSlots.put(2, "heal");
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        if (spellSlots.isEmpty()) {
             this.spellSlots.put(1, "fireball");
             this.spellSlots.put(2, "heal");
        }
        return spellSlots.get(slot);
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getUsername() {
        return username;
    }

    public Rank getRank() {
        return rank;
    }

    public void setRank(Rank rank) {
        this.rank = rank;
    }

    public boolean isBuildMode() {
        return buildMode;
    }

    public void setBuildMode(boolean buildMode) {
        this.buildMode = buildMode;
    }
    
    public AttributeContainer getAttributes() {
        if (attributes == null) attributes = new AttributeContainer(); // Handle Gson deserialization where transient is null
        return attributes;
    }

    public double getCurrentMana() {
        return currentMana;
    }

    public void setCurrentMana(double currentMana) {
        this.currentMana = currentMana;
    }
    
    public java.util.Map<Integer, String> getAccessories() {
        if (accessories == null) {
            try {
                java.lang.reflect.Field field = this.getClass().getDeclaredField("accessories");
                field.setAccessible(true);
                field.set(this, new java.util.HashMap<Integer, String>());
            } catch (Exception e) { e.printStackTrace(); }
        }
        return accessories;
    }
}
