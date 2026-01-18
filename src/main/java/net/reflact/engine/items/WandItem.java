package net.reflact.engine.items;

import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.item.Material;
import net.reflact.engine.ReflactEngine;
import net.reflact.engine.data.ReflactPlayer;
import net.minestom.server.event.player.PlayerHandAnimationEvent;

public class WandItem {

    public static void register(GlobalEventHandler handler) {
        // We will treat a BLAZE_ROD as the "Wand" for now
        
        handler.addListener(PlayerUseItemEvent.class, event -> {
            if (event.getItemStack().material() != Material.BLAZE_ROD) return;
            
            Player player = event.getPlayer();
            ReflactPlayer data = ReflactEngine.getPlayerManager().getPlayer(player.getUuid());
            if (data == null) return;

            // Shift + Right Click = Slot 2
            // Right Click = Slot 1
            int slot = player.isSneaking() ? 2 : 1;
            
            String spellId = data.getSpellInSlot(slot);
            if (spellId != null) {
                ReflactEngine.getSpellManager().cast(player, spellId);
            }
        });
        
        // Left Click = Slot 3? Or Melee?
        // Using Animation event for Left Click detection
        handler.addListener(PlayerHandAnimationEvent.class, event -> {
            if (event.getPlayer().getItemInMainHand().material() != Material.BLAZE_ROD) return;
            // Left click logic if needed
        });
    }
}
