package net.reflact.engine.spells;

import net.minestom.server.entity.Player;
import net.minestom.server.particle.Particle;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.reflact.common.attribute.RpgAttributes;
import net.reflact.engine.ReflactEngine;
import net.reflact.engine.data.ReflactPlayer;

public class HealSpell implements Spell {

    @Override
    public String getId() {
        return "heal";
    }

    @Override
    public String getName() {
        return "Minor Heal";
    }

    @Override
    public long getCooldownMillis() {
        return 5000;
    }

    @Override
    public double getManaCost() {
        return 25;
    }

    @Override
    public void onCast(Player caster) {
        ReflactPlayer data = ReflactEngine.getPlayerManager().getPlayer(caster.getUuid());
        if (data == null) return;

        double intel = data.getAttributes().getValue(RpgAttributes.INTELLIGENCE);
        double healAmount = 10.0 + (intel * 0.5);

        double maxHealth = caster.getAttribute(net.minestom.server.entity.attribute.Attribute.MAX_HEALTH).getValue();
        caster.setHealth(Math.min(caster.getHealth() + (float)healAmount, (float)maxHealth));
        
        // Visuals
        caster.sendPacketToViewersAndSelf(new ParticlePacket(
                Particle.HEART,
                caster.getPosition().x(), caster.getPosition().y() + 2, caster.getPosition().z(),
                0.5f, 0.5f, 0.5f,
                0f, 10
        ));

        caster.sendMessage("§aYou healed yourself for " + String.format("%.1f", healAmount) + " HP!");
    }
}
