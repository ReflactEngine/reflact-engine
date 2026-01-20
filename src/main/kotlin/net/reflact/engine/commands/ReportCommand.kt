package net.reflact.engine.commands

import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.arguments.ArgumentType
import net.minestom.server.entity.Player

class ReportCommand : Command("report") {
    init {
        val playerArg = ArgumentType.Word("player")
        val reasonArg = ArgumentType.StringArray("reason")

        defaultExecutor = net.minestom.server.command.builder.CommandExecutor { sender, context ->
            DEFAULT_EXECUTOR(sender, context)
        }

        addSyntax({ sender, context ->
            if (sender !is Player) return@addSyntax
            val targetName = context.get(playerArg)
            val reasonParts = context.get(reasonArg)
            val reason = reasonParts.joinToString(" ")

            // In a real scenario, check if player exists in online players or database
            // For now, we just acknowledge the report
            sender.sendMessage("§aReport submitted for $targetName: $reason")

            // Notify staff (simulated by logging)
            println("[REPORT] ${sender.username} reported $targetName for: $reason")

        }, playerArg, reasonArg)
    }

    companion object {
        val DEFAULT_EXECUTOR = { sender: net.minestom.server.command.CommandSender, _: net.minestom.server.command.builder.CommandContext ->
            sender.sendMessage("Usage: /report <player> <reason>")
        }
    }
}
