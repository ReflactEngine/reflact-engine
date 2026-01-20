package net.reflact.engine.data

enum class Rank(val level: Int, val display: String, val colorCode: String) {
    OWNER(4, "Owner", "<red>"),
    ADMIN(3, "Admin", "<red>"),
    MODERATOR(2, "Mod", "<green>"),
    MEMBER(1, "Member", "<gray>");

    fun hasPermission(required: Rank): Boolean {
        return this.level >= required.level
    }
}
