package net.reflact.engine.spells;

import net.minestom.server.entity.Player;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.reflact.engine.ReflactEngine;
import net.reflact.engine.data.ReflactPlayer;
import net.reflact.common.attribute.RpgAttributes;
import net.reflact.common.network.packet.CastSpellPacket;
import net.reflact.common.network.packet.ManaUpdatePacket;
import net.reflact.engine.registry.ReflactRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import net.reflact.common.network.packet.CastSlotPacket;

public class SpellManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(SpellManager.class);
    
    private final ReflactRegistry<Spell> spellRegistry = new ReflactRegistry<>("Spells");
    
    // Cooldowns
    private final Map<UUID, Map<String, Long>> cooldowns = new ConcurrentHashMap<>();

    public void init() {
        ReflactEngine.getNetworkManager().registerHandler("cast_spell", (player, packet) -> {
            if (packet instanceof CastSpellPacket castPacket) {
                cast(player, castPacket.spellId());
            }
        });
        
        ReflactEngine.getNetworkManager().registerHandler("cast_slot", (player, packet) -> {
            if (packet instanceof CastSlotPacket slotPacket) {
                ReflactPlayer data = ReflactEngine.getPlayerManager().getPlayer(player.getUuid());
                if (data != null) {
                    String spellId = data.getSpellInSlot(slotPacket.slotIndex());
                    if (spellId != null) {
                        cast(player, spellId);
                    } else {
                        player.sendActionBar(Component.text("No spell in slot " + slotPacket.slotIndex(), NamedTextColor.RED));
                    }
                }
            }
        });
    }

    public void register(Spell spell) {
        spellRegistry.register(spell.getId(), spell);
        LOGGER.info("Registered spell: {}", spell.getName());
    }
    
    public Optional<Spell> getSpell(String id) {
        return spellRegistry.get(id);
    }

    public boolean cast(Player player, String spellId) {
        LOGGER.info("Attempting to cast spell: {} for player {}", spellId, player.getUsername());
        Optional<Spell> spellOpt = spellRegistry.get(spellId);
        if (spellOpt.isEmpty()) {
            LOGGER.warn("Player {} tried to cast unknown spell {}", player.getUsername(), spellId);
            return false;
        }
        Spell spell = spellOpt.get();
        UUID uuid = player.getUuid();
        long now = System.currentTimeMillis();

        if (isOnCooldown(uuid, spellId)) {
            long remaining = (cooldowns.get(uuid).get(spellId) - now) / 1000;
            player.sendActionBar(Component.text("Cooldown: " + remaining + "s", NamedTextColor.RED));
            return false;
        }

        ReflactPlayer reflactPlayer = ReflactEngine.getPlayerManager().getPlayer(uuid);
        if (reflactPlayer != null) {
            if (reflactPlayer.getCurrentMana() < spell.getManaCost()) {
                player.sendActionBar(Component.text("Not enough Mana!", NamedTextColor.BLUE));
                return false;
            }
            reflactPlayer.setCurrentMana(reflactPlayer.getCurrentMana() - spell.getManaCost());
            
            ReflactEngine.getNetworkManager().sendPacket(player, new ManaUpdatePacket(reflactPlayer.getCurrentMana(), reflactPlayer.getAttributes().getValue(RpgAttributes.MANA)));
        }

        cooldowns.computeIfAbsent(uuid, k -> new ConcurrentHashMap<>())
                 .put(spellId, now + spell.getCooldownMillis());

        spell.onCast(player);
        return true;
    }

    private boolean isOnCooldown(UUID uuid, String spellId) {
        if (!cooldowns.containsKey(uuid)) return false;
        Long end = cooldowns.get(uuid).get(spellId);
        if (end == null) return false;
        return end > System.currentTimeMillis();
    }
}
