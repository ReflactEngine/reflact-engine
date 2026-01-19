package net.reflact.engine;

import net.minestom.server.MinecraftServer;
import net.reflact.common.attribute.RpgAttributes;
import net.reflact.common.item.CustomItem;
import net.reflact.engine.commands.BuildModeCommand;
import net.reflact.engine.commands.RankCommand;
import net.reflact.engine.database.DatabaseManager;
import net.reflact.engine.listeners.EngineListeners;
import net.reflact.engine.managers.ItemManager;
import net.reflact.engine.managers.PlayerManager;
import net.reflact.engine.networking.NetworkManager;
import net.reflact.engine.spells.ClickType;
import net.reflact.engine.spells.FireballSpell;
import net.reflact.engine.spells.SpellManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ReflactEngine {
    private static final Logger LOGGER = LoggerFactory.getLogger(ReflactEngine.class);
    private static PlayerManager playerManager;
    private static SpellManager spellManager;
    private static NetworkManager networkManager;
    private static ItemManager itemManager;

    private static net.reflact.engine.axiom.AxiomManager axiomManager;
    private static net.reflact.engine.managers.MapManager mapManager;
    
    private static net.reflact.engine.npc.NpcManager npcManager;
    private static net.reflact.engine.quest.QuestManager questManager;
    private static net.reflact.engine.guild.GuildManager guildManager;
    private static net.reflact.engine.party.PartyManager partyManager;
    private static net.reflact.engine.classes.ClassManager classManager;

    private static DatabaseManager databaseManager;

    public static void init() {
        LOGGER.info("ReflactEngine initialized!");
        
        databaseManager = new DatabaseManager();
        databaseManager.init();
        
        playerManager = new PlayerManager();
        spellManager = new SpellManager();
        networkManager = new NetworkManager();
        itemManager = new ItemManager();
        axiomManager = new net.reflact.engine.axiom.AxiomManager();
        mapManager = new net.reflact.engine.managers.MapManager();
        
        npcManager = new net.reflact.engine.npc.NpcManager();
        questManager = new net.reflact.engine.quest.QuestManager();
        guildManager = new net.reflact.engine.guild.GuildManager();
        partyManager = new net.reflact.engine.party.PartyManager();
        classManager = new net.reflact.engine.classes.ClassManager();
        
        RpgAttributes.registerAll();
        networkManager.init();
        spellManager.init();
        axiomManager.init();
        
        npcManager.init();
        questManager.init();
        guildManager.init();
        partyManager.init();
        classManager.init();
        
        // Register default spells
        spellManager.register(new FireballSpell());
        spellManager.register(new net.reflact.engine.spells.HealSpell());
        
        // Register Dynamic Spells
        net.reflact.engine.spells.dynamic.DynamicSpell blink = new net.reflact.engine.spells.dynamic.DynamicSpell("blink", "Blink", 3000, 20);
        blink.addEffect(new net.reflact.engine.spells.dynamic.TeleportEffect());
        spellManager.register(blink);
        
        // Load Items from DB
        List<CustomItem> items = databaseManager.loadItems();
        for (CustomItem item : items) {
            itemManager.register(item);
        }
        
        // Register events
        EngineListeners.register(MinecraftServer.getGlobalEventHandler());
        net.reflact.engine.items.WandItem.register(MinecraftServer.getGlobalEventHandler());

        // Register commands
        var commandManager = MinecraftServer.getCommandManager();
        commandManager.register(new BuildModeCommand());
        commandManager.register(new RankCommand());
        commandManager.register(new net.reflact.engine.commands.GiveItemCommand());
        commandManager.register(new net.reflact.engine.commands.GamemodeCommand());
        commandManager.register(new net.reflact.engine.commands.AccessoriesCommand());
        
        // Start Tasks
        net.reflact.engine.tasks.ManaTask.start();
        
        // Map Update Task
        /*
        MinecraftServer.getSchedulerManager().submitTask(() -> {
            for (net.minestom.server.entity.Player p : MinecraftServer.getConnectionManager().getOnlinePlayers()) {
                mapManager.sendMapData(p);
            }
            return net.minestom.server.timer.TaskSchedule.seconds(5);
        });
        */
    }
    
    public static DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public static PlayerManager getPlayerManager() {
        return playerManager;
    }

    public static SpellManager getSpellManager() {
        return spellManager;
    }

    public static NetworkManager getNetworkManager() {
        return networkManager;
    }
    
    public static ItemManager getItemManager() {
        return itemManager;
    }

    public static net.reflact.engine.managers.MapManager getMapManager() {
        return mapManager;
    }
    
    public static net.reflact.engine.npc.NpcManager getNpcManager() {
        return npcManager;
    }

    public static net.reflact.engine.quest.QuestManager getQuestManager() {
        return questManager;
    }

    public static net.reflact.engine.guild.GuildManager getGuildManager() {
        return guildManager;
    }

    public static net.reflact.engine.party.PartyManager getPartyManager() {
        return partyManager;
    }
    
    public static net.reflact.engine.classes.ClassManager getClassManager() {
        return classManager;
    }
}
