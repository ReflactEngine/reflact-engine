package net.reflact.engine.data;

public enum Rank {
    OWNER(4, "Owner", "<red>"),
    ADMIN(3, "Admin", "<red>"),
    MODERATOR(2, "Mod", "<green>"),
    MEMBER(1, "Member", "<gray>");

    private final int level;
    private final String display;
    private final String colorCode;

    Rank(int level, String display, String colorCode) {
        this.level = level;
        this.display = display;
        this.colorCode = colorCode;
    }

    public int getLevel() {
        return level;
    }

    public String getDisplay() {
        return display;
    }
    
    public String getColorCode() {
        return colorCode;
    }

    public boolean hasPermission(Rank required) {
        return this.level >= required.level;
    }
}
