package net.reflact.engine.commands;

import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemStack;
import net.reflact.engine.ReflactEngine;
import net.reflact.engine.item.RpgItem;
import net.reflact.engine.networking.packet.S2CSyncItemPacket;

public class GiveItemCommand extends Command {
    public GiveItemCommand() {
        super("giveitem");

        var itemIdArg = ArgumentType.String("itemId");

        addSyntax((sender, context) -> {
            if (!(sender instanceof Player player)) return;
            
            String itemId = context.get(itemIdArg);
            
            RpgItem uniqueItem = ReflactEngine.getItemManager().createUnique(itemId);
            
            if (uniqueItem == null) {
                player.sendMessage("Item not found: " + itemId);
                return;
            }

            ItemStack stack = ReflactEngine.getItemManager().toItemStack(uniqueItem);
            player.getInventory().addItemStack(stack);
            
            // Sync the item data to the client
            // Passing -1 as slotId for now, implying "update cache for this UUID" rather than "set slot X"
            // The client should store UUID->Item mapping regardless of slot.
            ReflactEngine.getNetworkManager().sendPacket(player, new S2CSyncItemPacket(uniqueItem, -1));
            
            player.sendMessage("Given " + uniqueItem.getDisplayName());
            
        }, itemIdArg);
    }
}
