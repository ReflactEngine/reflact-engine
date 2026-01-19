package net.reflact.engine.managers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minestom.server.component.DataComponents;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.component.CustomData;
import net.reflact.common.attribute.*;
import net.reflact.common.item.CustomItem;
import net.reflact.engine.ReflactEngine;
import net.reflact.engine.data.ReflactPlayer;
import net.reflact.common.network.packet.ManaUpdatePacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(PlayerManager.class);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File DATA_FOLDER = new File("data/players");

    private final Map<UUID, ReflactPlayer> players = new ConcurrentHashMap<>();

    public PlayerManager() {
        if (!DATA_FOLDER.exists()) {
            DATA_FOLDER.mkdirs();
        }
    }

    public ReflactPlayer getPlayer(UUID uuid) {
        return players.get(uuid);
    }

    public ReflactPlayer loadPlayer(UUID uuid, String username) {
        File file = new File(DATA_FOLDER, uuid.toString() + ".json");
        ReflactPlayer player;

        if (file.exists()) {
            try (FileReader reader = new FileReader(file)) {
                player = GSON.fromJson(reader, ReflactPlayer.class);
            } catch (IOException e) {
                LOGGER.error("Failed to load player data for " + uuid, e);
                player = new ReflactPlayer(uuid, username);
            }
        } else {
            player = new ReflactPlayer(uuid, username);
        }
        
        // Ensure username is up to date in case of name change (optional logic)
        players.put(uuid, player);
        return player;
    }

    public void savePlayer(UUID uuid) {
        ReflactPlayer player = players.get(uuid);
        if (player == null) return;

        File file = new File(DATA_FOLDER, uuid.toString() + ".json");
        try (FileWriter writer = new FileWriter(file)) {
            GSON.toJson(player, writer);
        } catch (IOException e) {
            LOGGER.error("Failed to save player data for " + uuid, e);
        }
    }
    
    public void unloadPlayer(UUID uuid) {
        savePlayer(uuid);
        players.remove(uuid);
    }
    
    public void recalculateStats(Player player) {
        ReflactPlayer data = getPlayer(player.getUuid());
        if (data == null) return;
        
        // 1. Clear "Equipment" modifiers
        // We need a way to clear specific modifiers. For now, let's remove all starting with "equip_".
        // This requires AttributeContainer to support iteration or clearing.
        // Assuming we implement clearModifiers(prefix) in AttributeContainer.
        data.getAttributes().clearModifiersByPrefix("equip_");
        
        // 2. Iterate equipment
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack stack = player.getEquipment(slot);
            if (stack.isAir()) continue;
            
            // Get UUID from NBT
            CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
            if (customData == null) continue;
            
            String uuidStr = "";
            String templateId = "";
            try {
                // Read from NBT
                if (customData.nbt().contains("reflact_uuid")) {
                     uuidStr = customData.nbt().getString("reflact_uuid");
                }
                if (customData.nbt().contains("reflact_template_id")) {
                    templateId = customData.nbt().getString("reflact_template_id");
                }
            } catch (Exception e) { continue; }
            
            if (templateId.isEmpty()) continue;
            
            // Lookup Template
            CustomItem template = ReflactEngine.getItemManager().getTemplate(templateId).orElse(null);
            if (template == null) continue;
            
            // Apply attributes
            for (Map.Entry<String, Double> entry : template.getAttributes().entrySet()) {
                // Find Attribute object
                String attrId = entry.getKey();
                Attribute attr = AttributeRegistry.get(attrId).orElse(null);
                if (attr != null) {
                    // Create modifier with ID "equip_<slot>_<attr>"
                    // Assuming slot names are unique
                    String modId = "equip_" + slot.name() + "_" + attrId;
                    data.getAttributes().addModifier(attr, new AttributeModifier(modId, entry.getValue(), AttributeModifier.Operation.ADD_NUMBER));
                }
            }
        }
        
        // 3. Iterate Accessories
        for (Map.Entry<Integer, String> entry : data.getAccessories().entrySet()) {
            int slot = entry.getKey();
            String itemId = entry.getValue();
            if (itemId == null || itemId.isEmpty()) continue;
            
            CustomItem template = ReflactEngine.getItemManager().getTemplate(itemId).orElse(null);
            if (template == null) continue;
            
             for (Map.Entry<String, Double> attrEntry : template.getAttributes().entrySet()) {
                String attrId = attrEntry.getKey();
                Attribute attr = AttributeRegistry.get(attrId).orElse(null);
                if (attr != null) {
                    String modId = "equip_acc_" + slot + "_" + attrId;
                    data.getAttributes().addModifier(attr, new AttributeModifier(modId, attrEntry.getValue(), AttributeModifier.Operation.ADD_NUMBER));
                }
            }
        }
        
        // Apply to Player Entity
        double health = data.getAttributes().getValue(RpgAttributes.HEALTH);
        double walkSpeed = data.getAttributes().getValue(RpgAttributes.WALK_SPEED);
        
        player.getAttribute(net.minestom.server.entity.attribute.Attribute.MAX_HEALTH).setBaseValue(health);
        player.heal(); // Heal on recalc? Maybe not always.
        player.getAttribute(net.minestom.server.entity.attribute.Attribute.MOVEMENT_SPEED).setBaseValue(walkSpeed);
    }
}
