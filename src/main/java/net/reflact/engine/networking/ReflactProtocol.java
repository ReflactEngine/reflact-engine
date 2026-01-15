package net.reflact.engine.networking;

import com.google.gson.Gson;
import net.reflact.engine.networking.packet.ReflactPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class ReflactProtocol {
    private static final Logger LOGGER = LoggerFactory.getLogger(ReflactProtocol.class);
    private static final Map<String, Class<? extends ReflactPacket>> packetRegistry = new HashMap<>();
    private static final Gson gson = new Gson();

    public static void register(String id, Class<? extends ReflactPacket> clazz) {
        packetRegistry.put(id, clazz);
        LOGGER.info("Registered Packet: {} -> {}", id, clazz.getSimpleName());
    }

    public static String serialize(ReflactPacket packet) {
        return packet.getPacketId() + ":" + gson.toJson(packet);
    }

    public static ReflactPacket deserialize(String rawData) {
        int splitIndex = rawData.indexOf(":");
        if (splitIndex == -1) {
            LOGGER.error("Invalid packet format: {}", rawData);
            return null;
        }

        String id = rawData.substring(0, splitIndex);
        String json = rawData.substring(splitIndex + 1);

        Class<? extends ReflactPacket> clazz = packetRegistry.get(id);
        if (clazz == null) {
            LOGGER.warn("Unknown packet ID: {}", id);
            return null;
        }

        try {
            return gson.fromJson(json, clazz);
        } catch (Exception e) {
            LOGGER.error("Failed to parse packet JSON for ID {}: {}", id, e.getMessage());
            return null;
        }
    }
}
