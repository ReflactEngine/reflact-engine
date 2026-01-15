package net.reflact.engine.networking;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.server.common.PluginMessagePacket;
import net.reflact.engine.networking.packet.ReflactPacket;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class NetworkManager {
    public static final String CHANNEL = "reflact:main";
    public static final String MANA_CHANNEL = "reflact:mana"; // Restored for SpellManager

    public void init() {
        // Register Client -> Server Listeners if needed
    }

    public void sendPacket(Player player, ReflactPacket packet) {
        String json = packet.toJson();
        String payloadString = packet.getType().ordinal() + ":" + json;
        
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

    public void sendManaUpdate(Player player, double currentMana) {
        // Write double
        java.nio.ByteBuffer buffer = java.nio.ByteBuffer.allocate(8);
        buffer.putDouble(currentMana);
        player.sendPacket(new PluginMessagePacket(MANA_CHANNEL, buffer.array()));
    }
}
