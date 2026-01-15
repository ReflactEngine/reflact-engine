package net.reflact.engine.spells;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.reflact.engine.ReflactEngine;
import net.reflact.engine.attributes.RpgAttributes;
import net.reflact.engine.data.ReflactPlayer;

public class FireballSpell implements Spell {

    @Override
    public String getId() {
        return "fireball";
    }

    @Override
    public String getName() {
        return "Fireball";
    }

    @Override
    public long getCooldownMillis() {
        return 1000;
    }

    @Override
    public double getManaCost() {
        return 10;
    }

    @Override
    public void onCast(Player caster) {
        Instance instance = caster.getInstance();
        if (instance == null) return;

        ReflactPlayer data = ReflactEngine.getPlayerManager().getPlayer(caster.getUuid());
        double intel = data != null ? data.getAttributes().getValue(RpgAttributes.INTELLIGENCE) : 0;
        double damage = 5.0 + (intel * 0.2);

        Pos startPos = caster.getPosition().add(0, caster.getEyeHeight(), 0);
        Vec direction = startPos.direction();

        Entity fireball = new Entity(EntityType.FIREBALL);
        fireball.setInstance(instance, startPos.add(direction));
        fireball.setVelocity(direction.mul(20)); // Speed

        // Logic for collision would go here (Tick listener or collision event)
        // For this basic example, we just spawn it.
        caster.sendMessage("You cast Fireball! (Est. Damage: " + String.format("%.1f", damage) + " [Intel: " + (int)intel + "])");
    }
}
