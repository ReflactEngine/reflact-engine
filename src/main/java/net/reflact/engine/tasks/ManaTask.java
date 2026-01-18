package net.reflact.engine.tasks;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.reflact.common.attribute.RpgAttributes;
import net.reflact.common.network.packet.ManaUpdatePacket;
import net.reflact.engine.ReflactEngine;
import net.reflact.engine.data.ReflactPlayer;

public class ManaTask {
    public static void start() {
        MinecraftServer.getSchedulerManager().submitTask(() -> {
            for (Player player : MinecraftServer.getConnectionManager().getOnlinePlayers()) {
                ReflactPlayer data = ReflactEngine.getPlayerManager().getPlayer(player.getUuid());
                if (data == null) continue;

                double current = data.getCurrentMana();
                double maxMana = data.getAttributes().getValue(RpgAttributes.MANA);
                double regen = data.getAttributes().getValue(RpgAttributes.MANA_REGEN);

                if (current < maxMana) {
                    current = Math.min(maxMana, current + regen);
                    data.setCurrentMana(current);
                    // Send packet
                    ReflactEngine.getNetworkManager().sendPacket(player, new ManaUpdatePacket(current, maxMana));
                }
            }
            return net.minestom.server.timer.TaskSchedule.tick(20);
        });
    }
}
