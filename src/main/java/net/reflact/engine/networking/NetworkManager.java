package net.reflact.engine.networking;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.reflact.engine.ReflactEngine;
// import net.minestom.server.network.packet.client.common.PluginMessagePacket;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class NetworkManager {
    public static final String MANA_CHANNEL = "reflect:mana";
    public static final String CAST_CHANNEL = "reflect:cast";

    public void init() {
        // Register Client -> Server Listeners
        /*
        MinecraftServer.getPacketListenerManager().setPlayListener(PluginMessagePacket.class, (packet, player) -> {
            if (packet.identifier().equals(CAST_CHANNEL)) {
                String spellId = new String(packet.data(), StandardCharsets.UTF_8);
                // Execute on main thread
                MinecraftServer.getSchedulerManager().scheduleNextTick(() -> {
                    ReflactEngine.getSpellManager().cast(player, spellId);
                });
            }
        });
        */
        System.out.println("Warning: Networking disabled due to Minestom API changes.");
    }

    public void sendManaUpdate(Player player, double currentMana) {
        /*
        byte[] data = Double.toString(currentMana).getBytes(StandardCharsets.UTF_8);
        player.sendPacket(new net.minestom.server.network.packet.server.common.PluginMessagePacket(MANA_CHANNEL, data));
        */
    }
}
