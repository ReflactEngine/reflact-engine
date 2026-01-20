package net.reflact.engine.gui

import net.kyori.adventure.text.Component
import net.minestom.server.entity.Player
import net.minestom.server.inventory.Inventory
import net.minestom.server.inventory.InventoryType
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import net.reflact.engine.commands.AccessoriesCommand
import net.minestom.server.event.inventory.InventoryPreClickEvent

class MenuGui {
    companion object {
        fun open(player: Player) {
            val inventory = Inventory(InventoryType.CHEST_3_ROW, Component.text("Game Menu"))
            
            // Accessories
            inventory.setItemStack(11, ItemStack.of(Material.DIAMOND)
                .withCustomName(Component.text("§bAccessories"))
                .withLore(listOf(Component.text("§7Manage your accessories"))))

            // Stats
            inventory.setItemStack(13, ItemStack.of(Material.PLAYER_HEAD)
                .withCustomName(Component.text("§eYour Stats"))
                .withLore(listOf(Component.text("§7View your attributes"))))

            // Settings
            inventory.setItemStack(15, ItemStack.of(Material.COMPARATOR)
                .withCustomName(Component.text("§7Settings"))
                .withLore(listOf(Component.text("§7Game settings"))))

            player.openInventory(inventory)
        }

        fun onClick(event: InventoryPreClickEvent) {
            event.isCancelled = true
            val player = event.player
            val slot = event.slot
            
            when (slot) {
                11 -> { 
                    player.closeInventory()
                    AccessoriesCommand.openMenu(player)
                }
                13 -> {
                        player.sendMessage("Stats not implemented yet.")
                }
                15 -> {
                    player.sendMessage("Settings not implemented yet.")
                }
            }
        }
    }
}
