package net.reflact.engine.commands

import net.minestom.server.MinecraftServer
import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.Command
import net.minestom.server.entity.Player
import net.reflact.engine.ReflactEngine
import net.reflact.engine.data.Rank

class StopCommand : Command("stop") {
    init {
        setCondition(::checkCondition)
        setDefaultExecutor { sender, _ ->
            sender.sendMessage("Stopping server...")
            MinecraftServer.stopCleanly()
        }
    }

    private fun checkCondition(sender: CommandSender, commandString: String?): Boolean {
        if (sender is Player) {
            val reflactPlayer = ReflactEngine.getPlayerManager().getPlayer(sender.uuid)
            return reflactPlayer != null && reflactPlayer.rank.hasPermission(Rank.ADMIN)
        }
        return true
    }
}
