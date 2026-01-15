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

    public static void init() {
        LOGGER.info("ReflactEngine initialized!");
        playerManager = new PlayerManager();
        spellManager = new SpellManager();
        networkManager = new NetworkManager();
        itemManager = new ItemManager();
        
        RpgAttributes.registerAll();
        networkManager.init();
        
        // Register default spells
        spellManager.register(new FireballSpell(), List.of(ClickType.RIGHT, ClickType.LEFT, ClickType.RIGHT));
        
        // Register sample items
        registerSampleItems();
        
        // Register events
        EngineListeners.register(MinecraftServer.getGlobalEventHandler());

        // Register commands
        var commandManager = MinecraftServer.getCommandManager();
        commandManager.register(new BuildModeCommand());
        commandManager.register(new RankCommand());
        commandManager.register(new net.reflact.engine.commands.GiveItemCommand());
        
        // Start Tasks
        net.reflact.engine.tasks.ManaTask.start();
    }
    
    private static void registerSampleItems() {
        RpgItem sword = new RpgItem("starter_sword", "Novice Blade", ItemType.WEAPON, ItemTier.NORMAL);
        sword.setAttribute("attack_damage", 5.0);
        sword.setAttribute("attack_speed", 1.2);
        sword.setLore(List.of("A simple blade for a simple adventurer."));
        itemManager.register(sword);
        
        RpgItem chestplate = new RpgItem("mythic_chest", "Aegis of Valor", ItemType.CHESTPLATE, ItemTier.MYTHIC);
        chestplate.setAttribute("health", 100.0);
        chestplate.setAttribute("defense", 50.0);
        chestplate.setAttribute("health_regen", 5.0);
        chestplate.setLore(List.of("Forged in the fires of the sun.", "Grants immense power."));
        itemManager.register(chestplate);
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
