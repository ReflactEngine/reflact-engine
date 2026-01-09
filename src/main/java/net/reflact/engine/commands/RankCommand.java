package net.reflact.engine.commands;

import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentEnum;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import net.minestom.server.entity.Player;
import net.reflact.engine.ReflactEngine;
import net.reflact.engine.data.Rank;
import net.reflact.engine.data.ReflactPlayer;

import java.util.UUID;

public class RankCommand extends Command {

    public RankCommand() {
        super("rank");

        setCondition(this::checkCondition);

        var targetArg = new ArgumentWord("target");
        var rankArg = new ArgumentEnum<>("rank", Rank.class);

        addSyntax((sender, context) -> {
            String targetName = context.get(targetArg);
            Rank newRank = context.get(rankArg);

            // In a real scenario, we would resolve UUID from name via Mojang API or cache.
            // For this basic implementation, we only support online players or players who have joined before IF we had a name->uuid cache.
            // But since Minestom doesnt inherently map name->uuid for offline players without config, 
            // I will scan online players first.
            
            Player onlineTarget = net.minestom.server.MinecraftServer.getConnectionManager().getOnlinePlayerByUsername(targetName);
            UUID targetUuid = null;
            
            if (onlineTarget != null) {
                targetUuid = onlineTarget.getUuid();
            } else {
                sender.sendMessage("Player must be online for this basic implementation.");
                return;
            }

            ReflactPlayer targetPlayer = ReflactEngine.getPlayerManager().getPlayer(targetUuid);
            if (targetPlayer != null) {
                targetPlayer.setRank(newRank);
                // Save immediately
                ReflactEngine.getPlayerManager().savePlayer(targetUuid);
                sender.sendMessage("Set rank of " + targetName + " to " + newRank.getDisplay());
                if (onlineTarget != null) {
                    onlineTarget.sendMessage("Your rank has been updated to " + newRank.getDisplay());
                }
            }

        }, targetArg, rankArg);
    }

    private boolean checkCondition(CommandSender sender, String commandString) {
        if (sender instanceof Player player) {
            ReflactPlayer reflactPlayer = ReflactEngine.getPlayerManager().getPlayer(player.getUuid());
            return reflactPlayer != null && reflactPlayer.getRank().hasPermission(Rank.ADMIN);
        }
        return true; // Console is Admin
    }
}
