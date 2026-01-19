package net.reflact.engine.quest;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.event.entity.EntityDeathEvent;
import net.minestom.server.event.player.PlayerEntityInteractEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.reflact.engine.ReflactEngine;
import net.reflact.engine.data.ReflactPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class QuestManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(QuestManager.class);
    private static final Gson GSON = new Gson();
    private final Map<String, QuestData> quests = new HashMap<>();
    
    // Player Quest Progress: UUID -> Map<QuestID, StageIndex>
    // In a real DB impl this would be in DatabaseManager, but keeping in memory/ReflactPlayer for now.
    
    public void init() {
        loadQuests();
        registerListeners();
    }
    
    public void startQuest(Player player, String questId) {
        if (!quests.containsKey(questId)) {
            player.sendMessage(Component.text("Quest not found: " + questId, NamedTextColor.RED));
            return;
        }
        
        ReflactPlayer data = ReflactEngine.getPlayerManager().getPlayer(player.getUuid());
        if (data.hasQuest(questId)) {
            player.sendMessage(Component.text("You are already on this quest!", NamedTextColor.RED));
            return;
        }
        
        data.setQuestStage(questId, 0);
        player.sendMessage(Component.text("Quest Started: ", NamedTextColor.GOLD)
            .append(Component.text(quests.get(questId).name, NamedTextColor.YELLOW)));
        
        sendStageMessage(player, questId, 0);
    }
    
    public void completeQuest(Player player, String questId) {
        // Called when final stage done
        ReflactPlayer data = ReflactEngine.getPlayerManager().getPlayer(player.getUuid());
        if (!data.hasQuest(questId)) return;
        
        data.removeQuest(questId); // Or move to completed list
        // Add to completed history
        player.sendMessage(Component.text("Quest Complete: ", NamedTextColor.GOLD)
            .append(Component.text(quests.get(questId).name, NamedTextColor.GREEN)));
    }
    
    private void advanceStage(Player player, String questId) {
        ReflactPlayer data = ReflactEngine.getPlayerManager().getPlayer(player.getUuid());
        int current = data.getQuestStage(questId);
        QuestData quest = quests.get(questId);
        
        if (current + 1 >= quest.stages.size()) {
            completeQuest(player, questId);
        } else {
            data.setQuestStage(questId, current + 1);
            player.sendMessage(Component.text("Stage Complete!", NamedTextColor.GREEN));
            sendStageMessage(player, questId, current + 1);
        }
    }
    
    private void sendStageMessage(Player player, String questId, int stageIdx) {
        QuestData.Stage stage = quests.get(questId).stages.get(stageIdx);
        player.sendMessage(Component.text("Objective: ", NamedTextColor.GOLD)
            .append(Component.text(stage.description, NamedTextColor.WHITE)));
    }

    private void registerListeners() {
        // Kill listener
        MinecraftServer.getGlobalEventHandler().addListener(EntityDeathEvent.class, event -> {
            Entity entity = event.getEntity();
            // Find killer (Minestom EntityDeathEvent doesn't always have source easily depending on version, 
            // but let's assume we can get it or track damage)
            // For this snippet, assuming simple logic or skipping if complex to implementing damage tracking
            // Use damage tracker if available.
            
            // Checking if a player is nearby or tagged as killer
            // Simplified: If entity has a "killer" tag or we track it.
            // Minestom default doesn't track "killer" on EntityDeathEvent directly unless you use DamageType.
        });
        
        // NPC Talk Listener (via NpcManager triggering this? Or we just hook Interact)
        // We hook interact and check if it matches current stage
        MinecraftServer.getGlobalEventHandler().addListener(PlayerEntityInteractEvent.class, event -> {
            // Check if interaction target is an NPC
            // We need NpcManager to tell us the ID of the entity
            // Since NpcManager is separate, we can't easily cross-ref without static access or shared registry.
            // But we can check if Quest expects a "TALK" type.
            
            Player player = event.getPlayer();
            ReflactPlayer data = ReflactEngine.getPlayerManager().getPlayer(player.getUuid());
            if (data == null) return;
            
            // Loop active quests
            for (String qId : data.getActiveQuests()) {
                QuestData q = quests.get(qId);
                int stageIdx = data.getQuestStage(qId);
                if (stageIdx >= q.stages.size()) continue;
                
                QuestData.Stage stage = q.stages.get(stageIdx);
                if ("TALK".equalsIgnoreCase(stage.type)) {
                    // Check if target matches stage.target
                    // We need to resolve Entity ID to NPC ID.
                    // This requires coupling with NpcManager.
                    // Let's assume stage.target is the NPC ID.
                    String npcId = net.reflact.engine.ReflactEngine.getNpcManager().getNpcId(event.getTarget().getEntityId());
                    if (npcId != null && npcId.equals(stage.target)) {
                        advanceStage(player, qId);
                    }
                }
            }
        });
    }

    private void loadQuests() {
        File file = new File("config/quests.json");
        if (!file.exists()) {
            createDefaultQuests(file);
        }

        try (FileReader reader = new FileReader(file)) {
            List<QuestData> list = GSON.fromJson(reader, new TypeToken<List<QuestData>>(){}.getType());
            if (list != null) {
                for (QuestData q : list) {
                    quests.put(q.id, q);
                }
            }
            LOGGER.info("Loaded {} quests", quests.size());
        } catch (IOException e) {
            LOGGER.error("Failed to load quests", e);
        }
    }

    private void createDefaultQuests(File file) {
         try {
            file.getParentFile().mkdirs();
            List<QuestData> defaults = new ArrayList<>();
            
            QuestData intro = new QuestData();
            intro.id = "intro_quest";
            intro.name = "Welcome to Reflact";
            intro.description = List.of("Learn the basics.");
            intro.stages = new ArrayList<>();
            
            QuestData.Stage s1 = new QuestData.Stage();
            s1.type = "TALK";
            s1.target = "guide";
            s1.description = "Talk to the Guide again.";
            intro.stages.add(s1);
            
            defaults.add(intro);
            
            try (FileWriter writer = new FileWriter(file)) {
                GSON.toJson(defaults, writer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
