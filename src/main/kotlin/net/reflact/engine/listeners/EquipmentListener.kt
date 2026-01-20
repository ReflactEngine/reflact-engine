package net.reflact.engine.listeners

import net.minestom.server.MinecraftServer
import net.minestom.server.entity.Player
import net.minestom.server.event.GlobalEventHandler
import net.minestom.server.event.item.EntityEquipEvent
import net.minestom.server.event.player.PlayerSpawnEvent
import net.reflact.engine.ReflactEngine

object EquipmentListener {
    fun register(handler: GlobalEventHandler) {
        handler.addListener(EntityEquipEvent::class.java) { event ->
            if (event.entity is Player) {
                val player = event.entity as Player
                // Minestom might trigger this before the item is actually in the slot for getEquipment?
                // The event has getEquippedItem() and getSlot().
                // But recalculateStats reads from player.getEquipment().
                // The event is Cancellable, so usually it happens BEFORE update.
                // But we want to calculate AFTER.
                // We can schedule next tick.

                MinecraftServer.getSchedulerManager().scheduleNextTick {
                    ReflactEngine.getPlayerManager().recalculateStats(player)
                }
            }
        }

        handler.addListener(PlayerSpawnEvent::class.java) { event ->
            ReflactEngine.getPlayerManager().recalculateStats(event.player)
        }
    }
}
