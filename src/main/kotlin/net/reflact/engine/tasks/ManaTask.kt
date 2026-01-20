package net.reflact.engine.tasks

import net.minestom.server.MinecraftServer
import net.minestom.server.timer.TaskSchedule
import net.reflact.common.attribute.RpgAttributes
import net.reflact.common.network.packet.ManaUpdatePacket
import net.reflact.engine.ReflactEngine

object ManaTask {
    fun start() {
        MinecraftServer.getSchedulerManager().submitTask {
            for (player in MinecraftServer.getConnectionManager().onlinePlayers) {
                val data = ReflactEngine.getPlayerManager().getPlayer(player.uuid) ?: continue

                var current = data.currentMana
                val maxMana = data.attributes.getValue(RpgAttributes.MANA)
                val regen = data.attributes.getValue(RpgAttributes.MANA_REGEN)

                if (current < maxMana) {
                    current = (current + regen).coerceAtMost(maxMana)
                    data.currentMana = current
                    // Send packet
                    ReflactEngine.getNetworkManager().sendPacket(player, ManaUpdatePacket(current, maxMana))
                }
            }
            TaskSchedule.tick(20)
        }
    }
}
