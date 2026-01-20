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
import net.reflact.engine.ReflactEngine

object EngineListeners {
    fun register(handler: GlobalEventHandler) {
        // Chat Handling
        handler.addListener(PlayerChatEvent::class.java) { event ->
            event.isCancelled = true
            val message = Component.text("${event.player.username}: ${event.rawMessage}")
            MinecraftServer.getConnectionManager().onlinePlayers.forEach { it.sendMessage(message) }
        }

        // Data Loading
        handler.addListener(AsyncPlayerConfigurationEvent::class.java) { event ->
            ReflactEngine.getPlayerManager().loadPlayer(event.player.uuid, event.player.username)
            event.spawningInstance = MinecraftServer.getInstanceManager().instances.firstOrNull()
        }

        handler.addListener(PlayerSpawnEvent::class.java) { event ->
            if (event.isFirstSpawn) {
                ReflactEngine.getMapManager().sendMapData(event.player)
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
