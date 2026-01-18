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
    private transient double currentMana;
    private final java.util.Map<Integer, String> spellSlots = new java.util.HashMap<>();

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
}
