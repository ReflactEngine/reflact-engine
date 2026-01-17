package net.reflact.engine.commands;

import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;
import net.reflact.common.item.CustomItem;
import net.reflact.common.network.packet.S2CSyncItemPacket;
import net.reflact.engine.ReflactEngine;

public class GiveItemCommand extends Command {
    public GiveItemCommand() {
        super("giveitem");

        var itemIdArg = ArgumentType.String("itemId");

        addSyntax((sender, context) -> {
            String itemId = context.get(itemIdArg);
            Player player = (Player) sender;
            
            CustomItem uniqueItem = ReflactEngine.getItemManager().createUnique(itemId);
            if (uniqueItem == null) {
                player.sendMessage("Item not found: " + itemId);
                return;
            }
            
            player.getInventory().addItemStack(ReflactEngine.getItemManager().toItemStack(uniqueItem));
            
            // Sync to client cache
            ReflactEngine.getNetworkManager().sendPacket(player, new S2CSyncItemPacket(uniqueItem, -1));
            
            player.sendMessage("Gave item: " + uniqueItem.getName());
        }, itemIdArg);
    }
}
