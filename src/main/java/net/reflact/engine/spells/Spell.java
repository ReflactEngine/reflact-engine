package net.reflact.engine.spells;

import net.minestom.server.entity.Player;

public interface Spell {
    String getId();
    String getName();
    long getCooldownMillis();
    double getManaCost();

    /**
     * Called when the spell is successfully cast (cooldown and cost checks passed).
     */
    void onCast(Player caster);
}
