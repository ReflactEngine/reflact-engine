package net.reflact.engine.spells;

import net.minestom.server.entity.Player;
import net.reflact.engine.ReflactEngine;
import net.reflact.engine.data.ReflactPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SpellManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(SpellManager.class);
    private final Map<String, Spell> spells = new HashMap<>();
    // Combo -> Spell ID
    private final Map<List<ClickType>, String> combos = new HashMap<>();
    
    // Player -> Current Combo Buffer
    private final Map<UUID, List<ClickType>> playerBuffers = new ConcurrentHashMap<>();
    private final Map<UUID, Long> lastClickTime = new ConcurrentHashMap<>();
    
    // Cooldowns
    private final Map<UUID, Map<String, Long>> cooldowns = new ConcurrentHashMap<>();

    public void register(Spell spell, List<ClickType> combo) {
        spells.put(spell.getId(), spell);
        if (combo != null && !combo.isEmpty()) {
            combos.put(combo, spell.getId());
            LOGGER.info("Registered spell: " + spell.getName() + " with combo " + combo);
        } else {
            LOGGER.info("Registered spell: " + spell.getName() + " (No Combo)");
        }
    }
    
    // Method to handle a click input
    public void processClick(Player player, ClickType click) {
        UUID uuid = player.getUuid();
        long now = System.currentTimeMillis();
        
        // Reset buffer if too slow (e.g. > 1.5 seconds)
        if (now - lastClickTime.getOrDefault(uuid, 0L) > 1500) {
            playerBuffers.remove(uuid);
        }
        lastClickTime.put(uuid, now);
        
        List<ClickType> buffer = playerBuffers.computeIfAbsent(uuid, k -> new ArrayList<>());
        buffer.add(click);
        
        // Check if this matches a combo
        // We match exact combos. If R-L-R is a spell, and they did R-L-R, cast it and clear.
        if (combos.containsKey(buffer)) {
            String spellId = combos.get(buffer);
            boolean success = cast(player, spellId);
            if (success) {
                // Clear buffer on success
                playerBuffers.remove(uuid);
            } else {
                // If failed (cooldown/mana), we also clear to let them retry? 
                // Or maybe keep it? Usually clear to avoid confusion.
                playerBuffers.remove(uuid); 
            }
        } else {
            // Check if this buffer is a prefix of ANY combo. If not, clear it (invalid pattern).
            boolean possible = false;
            for (List<ClickType> key : combos.keySet()) {
                if (key.size() >= buffer.size() && key.subList(0, buffer.size()).equals(buffer)) {
                    possible = true;
                    break;
                }
            }
            
            if (!possible) {
                // Invalid sequence, reset
                // Optional: Play a "fail" sound
                playerBuffers.remove(uuid);
            }
        }
    }

    public Spell getSpell(String id) {
        return spells.get(id);
    }

    public boolean cast(Player player, String spellId) {
        Spell spell = spells.get(spellId);
        if (spell == null) return false;

        UUID uuid = player.getUuid();
        long now = System.currentTimeMillis();

        // Check Cooldown
        if (isOnCooldown(uuid, spellId)) {
            long remaining = (cooldowns.get(uuid).get(spellId) - now) / 1000;
            // Only send message if explicit cast? Or actionbar?
            player.sendActionBar(net.kyori.adventure.text.Component.text("Cooldown: " + remaining + "s", net.kyori.adventure.text.format.NamedTextColor.RED));
            return false;
        }

        // Check Mana
        ReflactPlayer reflactPlayer = ReflactEngine.getPlayerManager().getPlayer(uuid);
        if (reflactPlayer != null) {
            if (reflactPlayer.getCurrentMana() < spell.getManaCost()) {
                player.sendActionBar(net.kyori.adventure.text.Component.text("Not enough Mana!", net.kyori.adventure.text.format.NamedTextColor.BLUE));
                return false;
            }
            reflactPlayer.setCurrentMana(reflactPlayer.getCurrentMana() - spell.getManaCost());
            
            // Send Data Update Packet (Todo)
            ReflactEngine.getNetworkManager().sendManaUpdate(player, reflactPlayer.getCurrentMana());
        }

        // Apply Cooldown
        cooldowns.computeIfAbsent(uuid, k -> new ConcurrentHashMap<>())
                 .put(spellId, now + spell.getCooldownMillis());

        // Cast
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
