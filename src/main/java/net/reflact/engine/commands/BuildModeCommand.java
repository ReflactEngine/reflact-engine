package net.reflact.engine.commands;

import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Player;
import net.reflact.engine.ReflactEngine;
import net.reflact.engine.data.Rank;
import net.reflact.engine.data.ReflactPlayer;

public class BuildModeCommand extends Command {

    public BuildModeCommand() {
        super("buildmode", "bm");

        setCondition(this::checkCondition);

        setDefaultExecutor((sender, context) -> {
            Player player = (Player) sender;
            ReflactPlayer reflactPlayer = ReflactEngine.getPlayerManager().getPlayer(player.getUuid());
            
            if (reflactPlayer == null) return;

            boolean newState = !reflactPlayer.isBuildMode();
            reflactPlayer.setBuildMode(newState);
            player.sendMessage("Build mode " + (newState ? "enabled" : "disabled"));
        });
    }

    private boolean checkCondition(CommandSender sender, String commandString) {
        if (!(sender instanceof Player player)) return false;
        ReflactPlayer reflactPlayer = ReflactEngine.getPlayerManager().getPlayer(player.getUuid());
        return reflactPlayer != null && reflactPlayer.getRank().hasPermission(Rank.MODERATOR);
    }
}
