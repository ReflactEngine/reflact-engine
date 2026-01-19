package net.reflact.engine.npc;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.PlayerSkin;
import net.minestom.server.event.player.PlayerEntityInteractEvent;
import net.minestom.server.instance.Instance;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class NpcManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(NpcManager.class);
    private static final Gson GSON = new Gson();
    private final Map<String, NpcData> npcDataMap = new HashMap<>();
    private final Map<Integer, String> entityIdToNpcId = new ConcurrentHashMap<>();
    private final List<Entity> spawnedEntities = new ArrayList<>();

    public void init() {
        loadNpcs();
        
        // Register interaction listener
        MinecraftServer.getGlobalEventHandler().addListener(PlayerEntityInteractEvent.class, event -> {
            String npcId = entityIdToNpcId.get(event.getTarget().getEntityId());
            if (npcId != null) {
                handleInteraction(event.getPlayer(), npcId);
            }
        });
    }

    public void spawnAll(Instance instance) {
        for (NpcData data : npcDataMap.values()) {
            // Check world match if we support multi-world, for now just spawn everything
            spawnNpc(data, instance);
        }
    }

    private void spawnNpc(NpcData data, Instance instance) {
        Pos pos = new Pos(data.location.x, data.location.y, data.location.z, data.location.yaw, data.location.pitch);
        
        if ("PLAYER".equalsIgnoreCase(data.type)) {
            // FakePlayer requires a connection, easier to use Entity(EntityType.PLAYER) for simple NPCs
            // But Minestom Entity(EntityType.PLAYER) doesn't show up as a player with skin easily without extra work.
            // Using a simple Entity for now to be safe and "prod ready" in stability.
            // Actually, let's use a Zombie or Villager if Player is too complex for this quick impl,
            // but the user wants a framework.
            
            Entity npc = new Entity(EntityType.PLAYER);
            npc.setInstance(instance, pos);
            npc.setCustomName(Component.text(data.name));
            npc.setCustomNameVisible(true);
            npc.setNoGravity(true);
            
            // Add to tracking
            entityIdToNpcId.put(npc.getEntityId(), data.id);
            spawnedEntities.add(npc);
            
        } else {
            EntityType type = EntityType.VILLAGER;
            try {
                java.lang.reflect.Field field = EntityType.class.getField(data.type.toUpperCase());
                type = (EntityType) field.get(null);
            } catch (Exception e) {}
            
            Entity npc = new Entity(type);
            npc.setInstance(instance, pos);
            npc.setCustomName(Component.text(data.name));
            npc.setCustomNameVisible(true);
            npc.setNoGravity(true);
            
            entityIdToNpcId.put(npc.getEntityId(), data.id);
            spawnedEntities.add(npc);
        }
    }

    public String getNpcId(int entityId) {
        return entityIdToNpcId.get(entityId);
    }

    private void handleInteraction(net.minestom.server.entity.Player player, String npcId) {
        NpcData data = npcDataMap.get(npcId);
        if (data == null || data.interactions == null) return;

        for (NpcData.Interaction interaction : data.interactions) {
            if ("DIALOGUE".equalsIgnoreCase(interaction.type)) {
                player.sendMessage(Component.text(data.name + ": ", NamedTextColor.YELLOW)
                        .append(Component.text(interaction.value, NamedTextColor.WHITE)));
            } else if ("QUEST_START".equalsIgnoreCase(interaction.type)) {
                 net.reflact.engine.ReflactEngine.getQuestManager().startQuest(player, interaction.value);
            } else if ("QUEST_COMPLETE".equalsIgnoreCase(interaction.type)) {
                 net.reflact.engine.ReflactEngine.getQuestManager().completeQuest(player, interaction.value);
            }
        }
    }

    private void loadNpcs() {
        File file = new File("config/npcs.json");
        if (!file.exists()) {
            createDefaultNpcs(file);
        }

        try (FileReader reader = new FileReader(file)) {
            List<NpcData> npcs = GSON.fromJson(reader, new TypeToken<List<NpcData>>(){}.getType());
            if (npcs != null) {
                for (NpcData npc : npcs) {
                    npcDataMap.put(npc.id, npc);
                }
            }
            LOGGER.info("Loaded {} NPCs", npcDataMap.size());
        } catch (IOException e) {
            LOGGER.error("Failed to load NPCs", e);
        }
    }

    private void createDefaultNpcs(File file) {
        try {
            file.getParentFile().mkdirs();
            List<NpcData> defaults = new ArrayList<>();
            
            NpcData guide = new NpcData();
            guide.id = "guide";
            guide.name = "Guide";
            guide.type = "PLAYER";
            guide.location = new NpcData.Location();
            guide.location.x = 2; guide.location.y = 42; guide.location.z = 2;
            guide.interactions = new ArrayList<>();
            
            NpcData.Interaction welcome = new NpcData.Interaction();
            welcome.type = "DIALOGUE";
            welcome.value = "Welcome to the server! Right click me to start your journey.";
            guide.interactions.add(welcome);
            
            NpcData.Interaction startQuest = new NpcData.Interaction();
            startQuest.type = "QUEST_START";
            startQuest.value = "intro_quest";
            guide.interactions.add(startQuest);
            
            defaults.add(guide);
            
            try (FileWriter writer = new FileWriter(file)) {
                GSON.toJson(defaults, writer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
