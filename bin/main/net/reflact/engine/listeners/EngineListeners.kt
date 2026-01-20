package net.reflact.engine.listeners

import net.kyori.adventure.text.Component
import net.minestom.server.MinecraftServer
import net.minestom.server.event.GlobalEventHandler
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent
import net.minestom.server.event.player.PlayerBlockBreakEvent
import net.minestom.server.event.player.PlayerBlockPlaceEvent
import net.minestom.server.event.player.PlayerChatEvent
import net.minestom.server.event.player.PlayerDisconnectEvent
import net.minestom.server.event.player.PlayerSpawnEvent
import net.minestom.server.event.player.PlayerUseItemEvent
import net.minestom.server.event.inventory.InventoryPreClickEvent
import net.minestom.server.item.Material
import net.minestom.server.item.ItemStack
import net.reflact.engine.ReflactEngine

object EngineListeners {
    fun register(handler: GlobalEventHandler) {
        // Chat Handling
        handler.addListener(PlayerChatEvent::class.java) { event ->
            event.isCancelled = true
            val message = Component.text("${event.player.username}: ${event.rawMessage}")
            MinecraftServer.getConnectionManager().onlinePlayers.forEach { it.sendMessage(message) }
        }

        // GUI Handling
        handler.addListener(InventoryPreClickEvent::class.java) { event ->
            val inventory = event.inventory
            if (inventory != null) {
                // Check for Class Selection (Slot 10 is Warrior)
                val item10 = inventory.getItemStack(10)
                if (item10.material() == Material.IRON_SWORD) {
                     net.reflact.engine.gui.ClassSelectionGui.onClick(event)
                     return@addListener
                }
                
                // Check for Menu (Slot 11 is Accessories)
                val item11 = inventory.getItemStack(11)
                if (item11.material() == Material.DIAMOND) {
                     net.reflact.engine.gui.MenuGui.onClick(event)
                     return@addListener
                }
            }
        }

        // Data Loading
        handler.addListener(AsyncPlayerConfigurationEvent::class.java) { event ->
            ReflactEngine.getPlayerManager().loadPlayer(event.player.uuid, event.player.username)
            event.spawningInstance = MinecraftServer.getInstanceManager().instances.firstOrNull()
        }

        handler.addListener(PlayerSpawnEvent::class.java) { event ->
            if (event.isFirstSpawn) {
                ReflactEngine.getMapManager().sendMapData(event.player)
                
                // Give Compass
                val p = event.player
                val hasCompass = p.inventory.itemStacks.any { it.material() == Material.COMPASS }
                if (!hasCompass) {
                    p.inventory.addItemStack(ItemStack.of(Material.COMPASS).withCustomName(Component.text("§aGame Menu")))
                }

                val player = ReflactEngine.getPlayerManager().getPlayer(event.player.uuid)
                if (player != null && player.selectedClass == null) {
                    net.reflact.engine.gui.ClassSelectionGui.open(event.player)
                }
            }
        }
        
        handler.addListener(PlayerUseItemEvent::class.java) { event ->
            if (event.itemStack.material() == Material.COMPASS) {
                net.reflact.engine.gui.MenuGui.open(event.player)
                event.isCancelled = true
            }
        }

        // Data Saving
        handler.addListener(PlayerDisconnectEvent::class.java) { event ->
            ReflactEngine.getPlayerManager().unloadPlayer(event.player.uuid)
        }

        // Build Protection
        handler.addListener(PlayerBlockBreakEvent::class.java) { event ->
            val player = ReflactEngine.getPlayerManager().getPlayer(event.player.uuid)
            if (player == null || !player.isBuildMode) {
                event.isCancelled = true
            }
        }

        handler.addListener(PlayerBlockPlaceEvent::class.java) { event ->
            val player = ReflactEngine.getPlayerManager().getPlayer(event.player.uuid)
            if (player == null || !player.isBuildMode) {
                event.isCancelled = true
            }
        }
    }
}
