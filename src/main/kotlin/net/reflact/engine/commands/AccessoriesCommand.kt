package net.reflact.engine.commands

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.minestom.server.command.builder.Command
import net.minestom.server.entity.Player
import net.minestom.server.inventory.Inventory
import net.minestom.server.inventory.InventoryType
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import net.reflact.engine.ReflactEngine

class AccessoriesCommand : Command("accessories", "acc") {
    init {
        defaultExecutor = net.minestom.server.command.builder.CommandExecutor { sender, context ->
            DEFAULT_EXECUTOR(sender, context)
        }
    }

    companion object {
        val DEFAULT_EXECUTOR = { sender: net.minestom.server.command.CommandSender, _: net.minestom.server.command.builder.CommandContext ->
            if (sender is Player) {
                openMenu(sender)
            }
        }

        fun openMenu(player: Player) {
            val inv = Inventory(InventoryType.CHEST_2_ROW, Component.text("Accessories"))
            val data = ReflactEngine.getPlayerManager().getPlayer(player.uuid) ?: return // Null safety
            val accs = data.accessories

            // Setup Slots
            // 0-4: Rings (5)
            // 5-7: Bracelets (3)
            // 9-10: Necklaces (2)
            // 11-12: Braces (2)

            // Fill background
            val bg = ItemStack.of(Material.GRAY_STAINED_GLASS_PANE).withCustomName(Component.empty())
            for (i in 0 until inv.size) {
                inv.setItemStack(i, bg)
            }

            // Set Items
            setSlot(inv, accs, 0, "Ring 1")
            setSlot(inv, accs, 1, "Ring 2")
            setSlot(inv, accs, 2, "Ring 3")
            setSlot(inv, accs, 3, "Ring 4")
            setSlot(inv, accs, 4, "Ring 5")

            setSlot(inv, accs, 5, "Bracelet 1")
            setSlot(inv, accs, 6, "Bracelet 2")
            setSlot(inv, accs, 7, "Bracelet 3")

            setSlot(inv, accs, 9, "Necklace 1")
            setSlot(inv, accs, 10, "Necklace 2")

            setSlot(inv, accs, 12, "Brace 1")
            setSlot(inv, accs, 13, "Brace 2")

            player.openInventory(inv)
        }

        private fun setSlot(inv: Inventory, accs: Map<Int, String>, slot: Int, name: String) {
            if (accs.containsKey(slot)) {
                // In real impl, fetch item stack from ItemManager
                val item = ItemStack.of(Material.GOLD_NUGGET).withCustomName(Component.text(accs[slot]!!))
                inv.setItemStack(slot, item)
            } else {
                inv.setItemStack(slot, ItemStack.of(Material.BARRIER).withCustomName(Component.text(name, NamedTextColor.GRAY)))
            }
        }
    }
}
