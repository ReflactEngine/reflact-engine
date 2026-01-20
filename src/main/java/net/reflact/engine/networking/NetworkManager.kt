package net.reflact.engine.networking

import net.minestom.server.MinecraftServer
import net.minestom.server.entity.Player
import net.minestom.server.event.player.PlayerPluginMessageEvent
import net.minestom.server.network.packet.server.common.PluginMessagePacket
import net.reflact.common.network.PacketRegistry
import net.reflact.common.network.packet.ReflactPacket
import org.slf4j.LoggerFactory
import java.io.*
import java.nio.charset.StandardCharsets
import java.util.concurrent.ConcurrentHashMap
import java.util.function.BiConsumer

class NetworkManager {
    companion object {
        private val LOGGER = LoggerFactory.getLogger(NetworkManager::class.java)
        const val CHANNEL = "reflact:main"
    }

    private val handlers: MutableMap<String, BiConsumer<Player, ReflactPacket>> = ConcurrentHashMap()

    fun init() {
        MinecraftServer.getGlobalEventHandler().addListener(PlayerPluginMessageEvent::class.java) { event ->
            if (event.identifier != CHANNEL) return@addListener

            try {
                // Minestom provides raw bytes directly
                val data = event.message
                val bais = ByteArrayInputStream(data)
                val input = DataInputStream(bais)
                
                // Read Packet ID
                val id = input.readUTF()
                
                val clazz = PacketRegistry.get(id)
                if (clazz == null) {
                    LOGGER.warn("Unknown packet ID: {}", id)
                    return@addListener
                }

                val packet = clazz.getDeclaredConstructor().newInstance()
                packet.decode(input)
                
                handlePacket(event.player, packet, id)

            } catch (e: Exception) {
                LOGGER.error("Error processing packet from {}: {}", event.player.username, e.message)
                e.printStackTrace()
            }
        }
    }

    fun registerHandler(packetId: String, handler: BiConsumer<Player, ReflactPacket>) {
        handlers[packetId] = handler
        LOGGER.info("Registered handler for packet ID: {}", packetId)
    }

    fun onPlayerConfiguration(player: Player) {
        val channel = "minecraft:register"
        val data = "reflact:main".toByteArray(StandardCharsets.UTF_8)
        player.sendPacket(PluginMessagePacket(channel, data))
        LOGGER.info("Sent channel registration to {}", player.username)
    }

    private fun handlePacket(player: Player, packet: ReflactPacket, id: String) {
        // LOGGER.info("Received packet {} from {}", id, player.username) // Spammy
        val handler = handlers[id]
        if (handler != null) {
            handler.accept(player, packet)
        } else {
            LOGGER.warn("No handler found for packet ID: {}", id)
        }
    }

    fun sendPacket(player: Player, packet: ReflactPacket) {
        val id = PacketRegistry.getId(packet.javaClass)
        if (id == null) {
            LOGGER.error("Unknown packet class: {}", packet.javaClass)
            return
        }

        try {
            val baos = ByteArrayOutputStream()
            val out = DataOutputStream(baos)
            
            out.writeUTF(id)
            packet.encode(out)
            
            player.sendPacket(PluginMessagePacket(CHANNEL, baos.toByteArray()))
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}
