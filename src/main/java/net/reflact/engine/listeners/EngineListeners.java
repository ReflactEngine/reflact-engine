package net.reflact.engine.listeners;

import net.minestom.server.MinecraftServer;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.event.player.PlayerBlockBreakEvent;
import net.minestom.server.event.player.PlayerBlockPlaceEvent;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.minestom.server.event.player.PlayerHandAnimationEvent;
import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.item.Material;
import net.reflact.engine.ReflactEngine;
import net.reflact.engine.data.ReflactPlayer;
import net.reflact.engine.spells.ClickType;

import net.minestom.server.event.player.PlayerChatEvent;
import net.kyori.adventure.text.Component;

public class EngineListeners {

    public static void register(GlobalEventHandler handler) {
        // Chat Handling
        handler.addListener(PlayerChatEvent.class, event -> {
            event.setCancelled(true);
            Component message = Component.text(event.getPlayer().getUsername() + ": " + event.getRawMessage());
            MinecraftServer.getConnectionManager().getOnlinePlayers().forEach(p -> p.sendMessage(message));
        });

        // Data Loading
        handler.addListener(AsyncPlayerConfigurationEvent.class, event -> {
            ReflactEngine.getPlayerManager().loadPlayer(event.getPlayer().getUuid(), event.getPlayer().getUsername());
        });

        // Data Saving
        handler.addListener(PlayerDisconnectEvent.class, event -> {
            ReflactEngine.getPlayerManager().unloadPlayer(event.getPlayer().getUuid());
        });

        // Build Protection
        handler.addListener(PlayerBlockBreakEvent.class, event -> {
            ReflactPlayer player = ReflactEngine.getPlayerManager().getPlayer(event.getPlayer().getUuid());
            if (player == null || !player.isBuildMode()) {
                event.setCancelled(true);
            }
        });

        handler.addListener(PlayerBlockPlaceEvent.class, event -> {
            ReflactPlayer player = ReflactEngine.getPlayerManager().getPlayer(event.getPlayer().getUuid());
            if (player == null || !player.isBuildMode()) {
                event.setCancelled(true);
            }
        });
        
        // Spell Casting - Left Click (Swing)
        // Removed Combo Logic
        /*
        handler.addListener(PlayerHandAnimationEvent.class, event -> {
            Material mainHand = event.getPlayer().getItemInMainHand().material();
            if (mainHand == Material.STICK || mainHand == Material.BLAZE_ROD) {
                ReflactEngine.getSpellManager().processClick(event.getPlayer(), ClickType.LEFT);
            }
        });
        */
        
        // Spell Casting - Right Click (Use Item)
        /*
        handler.addListener(PlayerUseItemEvent.class, event -> {
            // Material mainHand = event.getPlayer().getItemInMainHand().material();
            // if (mainHand == Material.STICK || mainHand == Material.BLAZE_ROD) {
            //    ReflactEngine.getSpellManager().processClick(event.getPlayer(), ClickType.RIGHT);
            // }
        });
        */
        
        // Stats
        EquipmentListener.register(handler);
        DamageListener.register(handler);
    }
}
