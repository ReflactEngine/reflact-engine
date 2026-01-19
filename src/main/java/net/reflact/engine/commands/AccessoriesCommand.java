package net.reflact.engine.commands;

import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Player;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.inventory.click.ClickType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.reflact.engine.ReflactEngine;
import net.reflact.engine.data.ReflactPlayer;

import java.util.Map;

public class AccessoriesCommand extends Command {
    public AccessoriesCommand() {
        super("accessories", "acc");
        
        setDefaultExecutor((sender, context) -> {
            if (!(sender instanceof Player player)) return;
            openMenu(player);
        });
    }
    
    private void openMenu(Player player) {
        Inventory inv = new Inventory(InventoryType.CHEST_2_ROW, Component.text("Accessories"));
        ReflactPlayer data = ReflactEngine.getPlayerManager().getPlayer(player.getUuid());
        Map<Integer, String> accs = data.getAccessories();
        
        // Setup Slots
        // 0-4: Rings (5)
        // 5-7: Bracelets (3)
        // 9-10: Necklaces (2)
        // 11-12: Braces (2)
        
        // Fill background
        ItemStack bg = ItemStack.of(Material.GRAY_STAINED_GLASS_PANE).withCustomName(Component.empty());
        for (int i = 0; i < inv.getSize(); i++) {
            inv.setItemStack(i, bg);
        }
        
        // Set Items
        setSlot(inv, accs, 0, "Ring 1");
        setSlot(inv, accs, 1, "Ring 2");
        setSlot(inv, accs, 2, "Ring 3");
        setSlot(inv, accs, 3, "Ring 4");
        setSlot(inv, accs, 4, "Ring 5");
        
        setSlot(inv, accs, 5, "Bracelet 1");
        setSlot(inv, accs, 6, "Bracelet 2");
        setSlot(inv, accs, 7, "Bracelet 3");
        
        setSlot(inv, accs, 9, "Necklace 1");
        setSlot(inv, accs, 10, "Necklace 2");
        
        setSlot(inv, accs, 12, "Brace 1");
        setSlot(inv, accs, 13, "Brace 2"); // Index 11 in ReflactPlayer mapping logic was hypothetical, here explicit
        
        // Listener
        // inv.addInventoryCondition((p, slot, clickType, result) -> {
        //    if (inv.getItemStack(slot).material() == Material.GRAY_STAINED_GLASS_PANE) {
        //        result.setCancel(true);
        //    }
        // });
        
        player.openInventory(inv);
    }
    
    private void setSlot(Inventory inv, Map<Integer, String> accs, int slot, String name) {
        if (accs.containsKey(slot)) {
            // In real impl, fetch item stack from ItemManager
            ItemStack item = ItemStack.of(Material.GOLD_NUGGET).withCustomName(Component.text(accs.get(slot)));
            inv.setItemStack(slot, item);
        } else {
            inv.setItemStack(slot, ItemStack.of(Material.BARRIER).withCustomName(Component.text(name, NamedTextColor.GRAY)));
        }
    }
}
