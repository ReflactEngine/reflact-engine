package net.reflact.engine.item;

public enum ItemTier {
    NORMAL("Normal", 0xFFFFFF),
    UNIQUE("Unique", 0xFFFF55),
    RARE("Rare", 0xFF55FF),
    LEGENDARY("Legendary", 0x55FFFF),
    MYTHIC("Mythic", 0xAA00AA);

    private final String displayName;
    private final int color;

    ItemTier(String displayName, int color) {
        this.displayName = displayName;
        this.color = color;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getColor() {
        return color;
    }
}
