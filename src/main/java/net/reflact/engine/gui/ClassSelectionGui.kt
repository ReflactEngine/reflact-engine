package net.reflact.engine.gui

import net.kyori.adventure.text.Component
import net.minestom.server.entity.Player
import net.minestom.server.inventory.Inventory
import net.minestom.server.inventory.InventoryType
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import net.reflact.engine.ReflactEngine
import net.reflact.engine.classes.ReflactClass
import net.minestom.server.event.inventory.InventoryPreClickEvent

class ClassSelectionGui {

    companion object {
        fun open(player: Player) {
            val inventory = Inventory(InventoryType.CHEST_3_ROW, Component.text("Select Class"))

            // Warrior
            inventory.setItemStack(10, ItemStack.of(Material.IRON_SWORD)
                .withCustomName(Component.text("§cWarrior"))
                .withLore(listOf(Component.text("§7High Defense and Damage"))))

            // Archer
            inventory.setItemStack(12, ItemStack.of(Material.BOW)
                .withCustomName(Component.text("§aArcher"))
                .withLore(listOf(Component.text("§7High Range and Speed"))))

            // Mage
            inventory.setItemStack(14, ItemStack.of(Material.BLAZE_ROD)
                .withCustomName(Component.text("§9Mage"))
                .withLore(listOf(Component.text("§7High Intelligence and Mana"))))
                
             // Assassin
            inventory.setItemStack(16, ItemStack.of(Material.IRON_HOE) // Scythe/Dagger placeholder
                .withCustomName(Component.text("§eAssassin"))
                .withLore(listOf(Component.text("§7High Crit and Damage"))))

            player.openInventory(inventory)
        }

        fun onClick(event: InventoryPreClickEvent) {
            event.isCancelled = true
            val player = event.player
            val slot = event.slot
            
            val reflactPlayer = ReflactEngine.getPlayerManager().getPlayer(player.uuid) ?: return
            
            var selected: ReflactClass? = null
            
            when (slot) {
                10 -> selected = ReflactClass.WARRIOR
                12 -> selected = ReflactClass.ARCHER
                14 -> selected = ReflactClass.MAGE
                16 -> selected = ReflactClass.ASSASSIN
            }
            
            if (selected != null) {
                reflactPlayer.selectedClass = selected
                player.sendMessage("§aSelected class: " + selected.displayName)
                player.closeInventory()
                
                // Give starter spells
                val spells = ReflactEngine.getClassManager().getAvailableSpells(selected, 1)
                var spellSlot = 1
                reflactPlayer.spellSlots.clear()
                for (spellId in spells) {
                    reflactPlayer.spellSlots[spellSlot] = spellId
                    spellSlot++
                }
                if (spells.isNotEmpty()) {
                    player.sendMessage("§aLearned spells: " + spells.joinToString(", "))
                }
            }
        }
    }
}
