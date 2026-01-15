package net.reflact.engine.tasks;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.timer.TaskSchedule;
import net.reflact.engine.ReflactEngine;
import net.reflact.engine.attributes.RpgAttributes;
import net.reflact.engine.data.ReflactPlayer;

public class ManaTask {
    public static void start() {
        MinecraftServer.getSchedulerManager().submitTask(() -> {
            for (Player player : MinecraftServer.getConnectionManager().getOnlinePlayers()) {
                ReflactPlayer data = ReflactEngine.getPlayerManager().getPlayer(player.getUuid());
                if (data == null) continue;
                
                double maxMana = data.getAttributes().getValue(RpgAttributes.MANA);
                double regen = data.getAttributes().getValue(RpgAttributes.MANA_REGEN);
                
                double current = data.getCurrentMana();
                if (current < maxMana) {
                    current = Math.min(maxMana, current + regen);
                    data.setCurrentMana(current);
                    
                    // Sync to client
                    ReflactEngine.getNetworkManager().sendManaUpdate(player, current);
                }
            }
            return TaskSchedule.seconds(1);
        });
    }
}
