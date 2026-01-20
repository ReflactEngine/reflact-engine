package net.reflact.engine.commands

import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.arguments.ArgumentEnum
import net.minestom.server.command.builder.arguments.ArgumentType
import net.minestom.server.entity.GameMode
import net.minestom.server.entity.Player

class GamemodeCommand : Command("gamemode", "gm") {
    init {
        val modeArg = ArgumentType.Enum("mode", GameMode::class.java).setFormat(ArgumentEnum.Format.LOWER_CASED)

        addSyntax({ sender, context ->
            if (sender !is Player) return@addSyntax
            val mode = context.get(modeArg)
            sender.gameMode = mode
            sender.sendMessage("Gamemode set to $mode")
        }, modeArg)
    }
}
