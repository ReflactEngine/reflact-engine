package net.reflact.engine.commands

import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.Command
import net.minestom.server.entity.Player
import net.reflact.engine.ReflactEngine
import net.reflact.engine.data.Rank

class BuildModeCommand : Command("buildmode", "bm") {
    init {
        setCondition(::checkCondition)

        setDefaultExecutor { sender, _ ->
            val player = sender as Player
            val reflactPlayer = ReflactEngine.getPlayerManager().getPlayer(player.uuid) ?: return@setDefaultExecutor

            val newState = !reflactPlayer.isBuildMode
            reflactPlayer.isBuildMode = newState
            player.sendMessage("Build mode " + (if (newState) "enabled" else "disabled"))
        }
    }

    private fun checkCondition(sender: CommandSender, commandString: String?): Boolean {
        if (sender !is Player) return false
        val reflactPlayer = ReflactEngine.getPlayerManager().getPlayer(sender.uuid)
        return reflactPlayer != null && reflactPlayer.rank.hasPermission(Rank.MODERATOR)
    }
}
