package net.reflact.engine.networking

import com.google.gson.Gson
import net.minestom.server.MinecraftServer
import net.minestom.server.entity.Player
import net.minestom.server.event.player.PlayerPluginMessageEvent
import net.minestom.server.network.packet.server.common.PluginMessagePacket
import net.reflact.common.network.PacketRegistry
import net.reflact.common.network.packet.ReflactPacket
import org.slf4j.LoggerFactory
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.util.concurrent.ConcurrentHashMap
import java.util.function.BiConsumer

class NetworkManager {
    companion object {
        private val LOGGER = LoggerFactory.getLogger(NetworkManager::class.java)
        const val CHANNEL = "reflact:main"
        private val GSON = Gson()
    }

    private val handlers: MutableMap<String, BiConsumer<Player, ReflactPacket>> = ConcurrentHashMap()

    fun init() {
        MinecraftServer.getGlobalEventHandler().addListener(PlayerPluginMessageEvent::class.java) { event ->
            if (event.identifier != CHANNEL) return@addListener

            try {
                val message = readStringFromBytes(event.message)
                val splitIndex = message.indexOf(":")
                if (splitIndex == -1) return@addListener

                val id = message.substring(0, splitIndex)
                val json = message.substring(splitIndex + 1)

                val clazz = PacketRegistry.get(id)
                if (clazz == null) {
                    LOGGER.warn("Unknown packet ID: {}", id)
                    return@addListener
                }

                val packet = GSON.fromJson(json, clazz)
                handlePacket(event.player, packet, id)

            } catch (e: Exception) {
                LOGGER.error("Error processing packet from {}: {}", event.player.username, e.message)
            }
        }
    }

    fun registerHandler(packetId: String, handler: BiConsumer<Player, ReflactPacket>) {
        handlers[packetId] = handler
        LOGGER.info("Registered handler for packet ID: {}", packetId)
    }

    fun onPlayerConfiguration(player: Player) {
        // Send Register packet to inform client about our channel
        // This is crucial for Fabric API's ClientPlayNetworking to allow sending packets
        val channel = "minecraft:register"
        val data = "reflact:main".toByteArray(StandardCharsets.UTF_8)
        player.sendPacket(PluginMessagePacket(channel, data))
        LOGGER.info("Sent channel registration to {}", player.username)
    }

    private fun handlePacket(player: Player, packet: ReflactPacket, id: String) {
        LOGGER.info("Received packet {} from {}", id, player.username)
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
        val payloadString = "$id:${GSON.toJson(packet)}"

        try {
            val out = ByteArrayOutputStream()
            writeString(out, payloadString)
            player.sendPacket(PluginMessagePacket(CHANNEL, out.toByteArray()))
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    @Throws(IOException::class)
    private fun writeString(out: ByteArrayOutputStream, s: String) {
        val bytes = s.toByteArray(StandardCharsets.UTF_8)
        writeVarInt(out, bytes.size)
        out.write(bytes)
    }

    private fun writeVarInt(out: ByteArrayOutputStream, value: Int) {
        var v = value
        do {
            var temp = (v and 0b01111111).toByte()
            v = v ushr 7
            if (v != 0) {
                temp = (temp.toInt() or 0b10000000).toByte()
            }
            out.write(temp.toInt())
        } while (v != 0)
    }

    private fun readStringFromBytes(data: ByteArray): String {
        if (data.isEmpty()) return ""
        var i = 0
        var max = 0
        var result = 0
        var b: Byte
        do {
            if (i >= data.size) throw RuntimeException("VarInt too big or data too short")
            b = data[i++]
            result = result or ((b.toInt() and 0x7F) shl max)
            max += 7
            if (max > 35) throw RuntimeException("VarInt too big")
        } while ((b.toInt() and 0x80) != 0)

        val length = result
        if (i + length > data.size) {
            throw RuntimeException("String length mismatch: expected " + length + ", got " + (data.size - i))
        }
        return String(data, i, length, StandardCharsets.UTF_8)
    }
}
