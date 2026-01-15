package net.reflact.engine.commands;

import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentEnum;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;

public class GamemodeCommand extends Command {
    public GamemodeCommand() {
        super("gamemode", "gm");

        var modeArg = ArgumentType.Enum("mode", GameMode.class).setFormat(ArgumentEnum.Format.LOWER_CASED);

        addSyntax((sender, context) -> {
            if (!(sender instanceof Player player)) return;
            GameMode mode = context.get(modeArg);
            player.setGameMode(mode);
            player.sendMessage("Gamemode set to " + mode);
        }, modeArg);
    }
}
