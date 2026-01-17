package net.reflact.engine.networking;

import com.google.gson.Gson;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.PlayerPluginMessageEvent;
import net.minestom.server.network.packet.server.common.PluginMessagePacket;
import net.reflact.common.network.PacketRegistry;
import net.reflact.common.network.packet.ReflactPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

public class NetworkManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(NetworkManager.class);
    public static final String CHANNEL = "reflact:main";
    private static final Gson gson = new Gson();

    private final Map<String, BiConsumer<Player, ReflactPacket>> handlers = new ConcurrentHashMap<>();

    public void init() {
        MinecraftServer.getGlobalEventHandler().addListener(PlayerPluginMessageEvent.class, event -> {
            if (!event.getIdentifier().equals(CHANNEL)) return;

            try {
                String message = readStringFromBytes(event.getMessage());
                int splitIndex = message.indexOf(":");
                if (splitIndex == -1) return;

                String id = message.substring(0, splitIndex);
                String json = message.substring(splitIndex + 1);

                Class<? extends ReflactPacket> clazz = PacketRegistry.get(id);
                if (clazz == null) {
                    LOGGER.warn("Unknown packet ID: {}", id);
                    return;
                }

                ReflactPacket packet = gson.fromJson(json, clazz);
                handlePacket(event.getPlayer(), packet, id);

            } catch (Exception e) {
                LOGGER.error("Error processing packet from {}: {}", event.getPlayer().getUsername(), e.getMessage());
            }
        });
    }

    public void registerHandler(String packetId, BiConsumer<Player, ReflactPacket> handler) {
        handlers.put(packetId, handler);
        LOGGER.info("Registered handler for packet ID: {}", packetId);
    }
    
    private void handlePacket(Player player, ReflactPacket packet, String id) {
        LOGGER.info("Received packet {} from {}", id, player.getUsername());
        if (handlers.containsKey(id)) {
            handlers.get(id).accept(player, packet);
        } else {
            LOGGER.warn("No handler found for packet ID: {}", id);
        }
    }

    public void sendPacket(Player player, ReflactPacket packet) {
        String id = PacketRegistry.getId(packet.getClass());
        if (id == null) {
            LOGGER.error("Unknown packet class: {}", packet.getClass());
            return;
        }
        String payloadString = id + ":" + gson.toJson(packet);
        
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            writeString(out, payloadString);
            player.sendPacket(new PluginMessagePacket(CHANNEL, out.toByteArray()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
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
