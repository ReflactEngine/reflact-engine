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

    public ReflactPlayer(UUID uuid, String username) {
        this.uuid = uuid;
        this.username = username;
        this.rank = Rank.MEMBER;
        this.buildMode = false;
        this.attributes = new AttributeContainer();
        this.currentMana = 100.0; // Default, should be loaded or calculated
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
