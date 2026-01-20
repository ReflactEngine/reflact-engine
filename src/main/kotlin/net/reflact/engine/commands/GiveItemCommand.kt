package net.reflact.engine.commands

import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.arguments.ArgumentType
import net.minestom.server.command.builder.suggestion.SuggestionEntry
import net.minestom.server.entity.Player
import net.reflact.common.network.packet.S2CSyncItemPacket
import net.reflact.engine.ReflactEngine

class GiveItemCommand : Command("item") {
    init {
        val itemIdArg = ArgumentType.String("itemId")

        itemIdArg.setSuggestionCallback { _, _, suggestion ->
            for (id in ReflactEngine.getItemManager().getTemplateIds()) {
                suggestion.addEntry(SuggestionEntry(id))
            }
        }

        addSyntax({ sender, context ->
            if (sender !is Player) return@addSyntax
            val itemId = context.get(itemIdArg)
            val player = sender

            val uniqueItem = ReflactEngine.getItemManager().createUnique(itemId)
            if (uniqueItem == null) {
                player.sendMessage("Item not found: $itemId")
                return@addSyntax
            }

            player.inventory.addItemStack(ReflactEngine.getItemManager().toItemStack(uniqueItem))

            // Sync to client cache
            ReflactEngine.getNetworkManager().sendPacket(player, S2CSyncItemPacket(uniqueItem, -1))

            player.sendMessage("Gave item: ${uniqueItem.name}")
        }, itemIdArg)
    }
}
