package net.reflact.engine.commands

import net.minestom.server.command.builder.Command
import net.minestom.server.coordinate.Pos
import net.minestom.server.entity.Player

class SpawnCommand : Command("spawn") {
    init {
        setDefaultExecutor { sender, _ ->
            if (sender !is Player) {
                sender.sendMessage("Only players can use this command.")
                return@setDefaultExecutor
            }
            sender.teleport(Pos(0.5, 100.0, 0.5))
            sender.sendMessage("§aTeleported to spawn.")
        }
    }
}
