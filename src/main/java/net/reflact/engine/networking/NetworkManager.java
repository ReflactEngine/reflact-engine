package net.reflact.engine.networking;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.PlayerPluginMessageEvent;
import net.minestom.server.network.packet.server.common.PluginMessagePacket;
import net.reflact.engine.networking.packet.ReflactPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class NetworkManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(NetworkManager.class);
    public static final String CHANNEL = "reflact:main";

    public void init() {
        // Handle incoming packets from Client
        MinecraftServer.getGlobalEventHandler().addListener(PlayerPluginMessageEvent.class, event -> {
            if (!event.getIdentifier().equals(CHANNEL)) return;

            try {
                // We assume the payload matches what we write: VarInt Length + String Bytes
                // Or if we change Client to just send Raw Bytes, we can read directly.
                // For now, let's try to read the string assuming standard Minecraft String serialization.
                String message = readStringFromBytes(event.getMessage());
                
                ReflactPacket packet = ReflactProtocol.deserialize(message);
                if (packet != null) {
                    handlePacket(event.getPlayer(), packet);
                }
            } catch (Exception e) {
                LOGGER.error("Error processing packet from {}: {}", event.getPlayer().getUsername(), e.getMessage());
            }
        });
    }
    private final java.util.Map<String, java.util.function.BiConsumer<Player, ReflactPacket>> handlers = new java.util.concurrent.ConcurrentHashMap<>();

    public void registerHandler(String packetId, java.util.function.BiConsumer<Player, ReflactPacket> handler) {
        handlers.put(packetId, handler);
        LOGGER.info("Registered handler for packet ID: {}", packetId);
    }
    
    private void handlePacket(Player player, ReflactPacket packet) {
        LOGGER.info("Received packet {} from {}", packet.getPacketId(), player.getUsername());
        
        if (handlers.containsKey(packet.getPacketId())) {
            handlers.get(packet.getPacketId()).accept(player, packet);
        } else {
            LOGGER.warn("No handler found for packet ID: {}", packet.getPacketId());
        }
    }

    public void sendPacket(Player player, ReflactPacket packet) {
        String payloadString = ReflactProtocol.serialize(packet);
        
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            writeString(out, payloadString);
            player.sendPacket(new PluginMessagePacket(CHANNEL, out.toByteArray()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    // Helper to write String compatible with Minecraft PacketCodecs.STRING
    private void writeString(ByteArrayOutputStream out, String s) throws IOException {
        byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
        writeVarInt(out, bytes.length);
        out.write(bytes);
    }

    private void writeVarInt(ByteArrayOutputStream out, int value) {
        do {
            byte temp = (byte)(value & 0b01111111);
            value >>>= 7;
            if (value != 0) {
                temp |= 0b10000000;
            }
            out.write(temp);
        } while (value != 0);
    }
    
    private String readStringFromBytes(byte[] data) {
        // Simple manual VarInt reader + String decoder
        int i = 0;
        int max = 0;
        int result = 0;
        byte b;
        do {
            b = data[i++];
            result |= (b & 0x7F) << max;
            max += 7;
            if (max > 35) throw new RuntimeException("VarInt too big");
        } while ((b & 0x80) != 0);
        
        int length = result;
        return new String(data, i, length, StandardCharsets.UTF_8);
    }
}