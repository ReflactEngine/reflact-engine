package net.reflact.engine.commands

import net.minestom.server.MinecraftServer
import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.arguments.ArgumentEnum
import net.minestom.server.command.builder.arguments.ArgumentWord
import net.minestom.server.entity.Player
import net.reflact.engine.ReflactEngine
import net.reflact.engine.data.Rank

class RankCommand : Command("rank") {
    init {
        setCondition(::checkCondition)

        val targetArg = ArgumentWord("target")
        val rankArg = ArgumentEnum("rank", Rank::class.java)

        addSyntax({ sender, context ->
            val targetName = context.get(targetArg)
            val newRank = context.get(rankArg)

            val onlineTarget = MinecraftServer.getConnectionManager().getOnlinePlayerByUsername(targetName)
            val targetUuid = onlineTarget?.uuid

            if (targetUuid == null) {
                sender.sendMessage("Player must be online for this basic implementation.")
                return@addSyntax
            }

            val targetPlayer = ReflactEngine.getPlayerManager().getPlayer(targetUuid)
            if (targetPlayer != null) {
                targetPlayer.rank = newRank
                ReflactEngine.getPlayerManager().savePlayer(targetUuid)
                sender.sendMessage("Set rank of $targetName to ${newRank.display}")
                onlineTarget?.sendMessage("Your rank has been updated to ${newRank.display}")
            }
        }, targetArg, rankArg)
    }

    private fun checkCondition(sender: CommandSender, commandString: String?): Boolean {
        if (sender is Player) {
            val reflactPlayer = ReflactEngine.getPlayerManager().getPlayer(sender.uuid)
            return reflactPlayer != null && reflactPlayer.rank.hasPermission(Rank.ADMIN)
        }
        return true
    }
}
