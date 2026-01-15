package net.reflact.engine;

import net.minestom.server.MinecraftServer;
import net.reflact.engine.attributes.RpgAttributes;
import net.reflact.engine.commands.BuildModeCommand;
import net.reflact.engine.commands.RankCommand;
import net.reflact.engine.item.ItemTier;
import net.reflact.engine.item.ItemType;
import net.reflact.engine.item.RpgItem;
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

    private static AxiomManager axiomManager;

    private static net.reflact.engine.database.DatabaseManager databaseManager;

    public static void init() {
        LOGGER.info("ReflactEngine initialized!");
        
        // Register Packets
        net.reflact.engine.networking.ReflactProtocol.register("mana_update", net.reflact.engine.networking.packet.ManaUpdatePacket.class);
        net.reflact.engine.networking.ReflactProtocol.register("cast_spell", net.reflact.engine.networking.packet.CastSpellPacket.class);
        net.reflact.engine.networking.ReflactProtocol.register("sync_item", net.reflact.engine.networking.packet.S2CSyncItemPacket.class);
        
        databaseManager = new net.reflact.engine.database.DatabaseManager();
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
        
        // Load Items from DB
        List<net.reflact.engine.item.RpgItem> items = databaseManager.loadItems();
        for (net.reflact.engine.item.RpgItem item : items) {
            itemManager.register(item);
        }
        
        // Register events
        EngineListeners.register(MinecraftServer.getGlobalEventHandler());

        // Register commands
        var commandManager = MinecraftServer.getCommandManager();
        commandManager.register(new BuildModeCommand());
        commandManager.register(new RankCommand());
        commandManager.register(new net.reflact.engine.commands.GiveItemCommand());
        commandManager.register(new net.reflact.engine.commands.GamemodeCommand());
        
        // Start Tasks
        net.reflact.engine.tasks.ManaTask.start();
    }
    
    public static net.reflact.engine.database.DatabaseManager getDatabaseManager() {
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
