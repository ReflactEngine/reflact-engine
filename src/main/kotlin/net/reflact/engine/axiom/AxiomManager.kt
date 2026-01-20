package net.reflact.engine.axiom

import net.minestom.server.MinecraftServer
import net.minestom.server.entity.Player
import net.minestom.server.event.player.PlayerPluginMessageEvent
import net.minestom.server.network.packet.server.common.PluginMessagePacket
import net.reflact.engine.ReflactEngine
import org.slf4j.LoggerFactory

class AxiomManager {
    companion object {
        private val LOGGER = LoggerFactory.getLogger(AxiomManager::class.java)
        private const val AXIOM_CHANNEL = "axiom:hello"
    }

    fun init() {
        MinecraftServer.getGlobalEventHandler().addListener(PlayerPluginMessageEvent::class.java) { event ->
            if (event.identifier == AXIOM_CHANNEL) {
                handleHandshake(event.player)
            }
        }
    }

    private fun handleHandshake(player: Player) {
        // Simple logic: If player is in build mode, allow Axiom.
        // If ReflactPlayer is null, deny.
        val reflactPlayer = ReflactEngine.getPlayerManager().getPlayer(player.uuid)
        val authorized = reflactPlayer != null && reflactPlayer.isBuildMode

        // Axiom expects a boolean (1 byte)
        val data = byteArrayOf((if (authorized) 1 else 0).toByte())
        player.sendPacket(PluginMessagePacket(AXIOM_CHANNEL, data))

        if (authorized) {
            LOGGER.info("Authorized Axiom for {}", player.username)
        }
    }
}
