package net.reflact.engine.axiom;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.PlayerPluginMessageEvent;
import net.minestom.server.network.packet.server.common.PluginMessagePacket;
import net.reflact.engine.ReflactEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AxiomManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(AxiomManager.class);
    private static final String AXIOM_CHANNEL = "axiom:hello";

    public void init() {
        MinecraftServer.getGlobalEventHandler().addListener(PlayerPluginMessageEvent.class, event -> {
            if (event.getIdentifier().equals(AXIOM_CHANNEL)) {
                handleHandshake(event.getPlayer());
            }
        });
    }

    private void handleHandshake(Player player) {
        // Simple logic: If player is in build mode, allow Axiom.
        // If ReflactPlayer is null, deny.
        var reflactPlayer = ReflactEngine.getPlayerManager().getPlayer(player.getUuid());
        boolean authorized = reflactPlayer != null && reflactPlayer.isBuildMode();
        
        // Axiom expects a boolean (1 byte)
        byte[] data = new byte[] { (byte) (authorized ? 1 : 0) };
        player.sendPacket(new PluginMessagePacket(AXIOM_CHANNEL, data));
        
        if (authorized) {
            LOGGER.info("Authorized Axiom for {}", player.getUsername());
        }
    }
}
