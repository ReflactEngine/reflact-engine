package net.reflact.engine.spells;

import net.minestom.server.entity.Player;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.reflact.engine.ReflactEngine;
import net.reflact.engine.data.ReflactPlayer;
import net.reflact.engine.networking.packet.CastSpellPacket;
import net.reflact.engine.networking.packet.ManaUpdatePacket;
import net.reflact.engine.registry.ReflactRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SpellManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(SpellManager.class);
    
    private final ReflactRegistry<Spell> spellRegistry = new ReflactRegistry<>("Spells");
    
    // Combo -> Spell ID
    private final Map<List<ClickType>, String> combos = new HashMap<>();
    
    // Player -> Current Combo Buffer
    private final Map<UUID, List<ClickType>> playerBuffers = new ConcurrentHashMap<>();
    private final Map<UUID, Long> lastClickTime = new ConcurrentHashMap<>();
    
    // Cooldowns
    private final Map<UUID, Map<String, Long>> cooldowns = new ConcurrentHashMap<>();

    public void init() {
        ReflactEngine.getNetworkManager().registerHandler("cast_spell", (player, packet) -> {
            if (packet instanceof CastSpellPacket castPacket) {
                cast(player, castPacket.getSpellId());
            }
        });
    }

    public void register(Spell spell, List<ClickType> combo) {
        spellRegistry.register(spell.getId(), spell);
        if (combo != null && !combo.isEmpty()) {
            combos.put(combo, spell.getId());
            LOGGER.info("Registered spell: {} with combo {}", spell.getName(), combo);
        } else {
            LOGGER.info("Registered spell: {} (No Combo)", spell.getName());
        }
    }
    
    public void processClick(Player player, ClickType click) {
        UUID uuid = player.getUuid();
        long now = System.currentTimeMillis();
        
        if (now - lastClickTime.getOrDefault(uuid, 0L) > 1500) {
            playerBuffers.remove(uuid);
        }
        lastClickTime.put(uuid, now);
        
        List<ClickType> buffer = playerBuffers.computeIfAbsent(uuid, k -> new ArrayList<>());
        buffer.add(click);
        
        if (combos.containsKey(buffer)) {
            String spellId = combos.get(buffer);
            boolean success = cast(player, spellId);
            if (success || !isOnCooldown(uuid, spellId)) { 
                playerBuffers.remove(uuid);
            }
        } else {
            boolean possible = false;
            for (List<ClickType> key : combos.keySet()) {
                if (key.size() >= buffer.size() && key.subList(0, buffer.size()).equals(buffer)) {
                    possible = true;
                    break;
                }
            }
            if (!possible) {
                playerBuffers.remove(uuid);
            }
        }
    }

    public Optional<Spell> getSpell(String id) {
        return spellRegistry.get(id);
    }

    public boolean cast(Player player, String spellId) {
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
            
            // Send Mana Update using new Packet system
            ReflactEngine.getNetworkManager().sendPacket(player, new ManaUpdatePacket(reflactPlayer.getCurrentMana()));
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