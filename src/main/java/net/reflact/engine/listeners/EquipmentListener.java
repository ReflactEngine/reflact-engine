package net.reflact.engine.listeners;

import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.item.EntityEquipEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.reflact.engine.ReflactEngine;

public class EquipmentListener {
    public static void register(GlobalEventHandler handler) {
        handler.addListener(EntityEquipEvent.class, event -> {
            if (event.getEntity() instanceof Player player) {
                // Minestom might trigger this before the item is actually in the slot for getEquipment?
                // The event has getEquippedItem() and getSlot().
                // But recalculateStats reads from player.getEquipment().
                // The event is Cancellable, so usually it happens BEFORE update.
                // But we want to calculate AFTER.
                // We can schedule next tick.
                
                net.minestom.server.MinecraftServer.getSchedulerManager().scheduleNextTick(() -> {
                     ReflactEngine.getPlayerManager().recalculateStats(player);
                });
            }
        });

        handler.addListener(PlayerSpawnEvent.class, event -> {
            ReflactEngine.getPlayerManager().recalculateStats(event.getPlayer());
        });
    }
}
