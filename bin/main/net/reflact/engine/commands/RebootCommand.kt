package net.reflact.engine.commands

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.minestom.server.MinecraftServer
import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.Command
import net.minestom.server.entity.Player
import net.reflact.engine.ReflactEngine
import net.reflact.engine.data.Rank

class RebootCommand : Command("reboot", "restart") {
    init {
        setCondition(::checkCondition)
        setDefaultExecutor { sender, _ ->
            val message = Component.text("Server is rebooting...", NamedTextColor.RED)
            MinecraftServer.getConnectionManager().onlinePlayers.forEach { it.kick(message) }
            sender.sendMessage("Rebooting server...")
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
