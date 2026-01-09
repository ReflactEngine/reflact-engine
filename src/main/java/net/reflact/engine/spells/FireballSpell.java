package net.reflact.engine.spells;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;

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

        Pos startPos = caster.getPosition().add(0, caster.getEyeHeight(), 0);
        Vec direction = startPos.direction();

        Entity fireball = new Entity(EntityType.FIREBALL);
        // FireballMeta meta = (FireballMeta) fireball.getEntityMeta();
        // meta.setItem(ItemStack.of(Material.FIRE_CHARGE)); // Optional visual

        fireball.setInstance(instance, startPos.add(direction));
        fireball.setVelocity(direction.mul(20)); // Speed

        // Logic for collision would go here (Tick listener or collision event)
        // For this basic example, we just spawn it.
        caster.sendMessage("You cast Fireball!");
    }
}
