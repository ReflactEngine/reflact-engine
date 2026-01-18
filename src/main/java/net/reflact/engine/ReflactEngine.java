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
        
        RpgAttributes.registerAll();
        networkManager.init();
        spellManager.init();
        axiomManager.init();
        
        // Register default spells
        spellManager.register(new FireballSpell(), List.of(ClickType.RIGHT, ClickType.LEFT, ClickType.RIGHT));
        spellManager.register(new net.reflact.engine.spells.HealSpell(), List.of(ClickType.RIGHT, ClickType.RIGHT, ClickType.RIGHT));
        
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
        
        // Start Tasks
        net.reflact.engine.tasks.ManaTask.start();
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
}
