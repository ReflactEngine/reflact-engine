package net.reflact.engine.items

import net.minestom.server.event.GlobalEventHandler
import net.minestom.server.event.player.PlayerHandAnimationEvent
import net.minestom.server.event.player.PlayerUseItemEvent
import net.minestom.server.item.Material
import net.reflact.engine.ReflactEngine

object WandItem {
    fun register(handler: GlobalEventHandler) {
        // We will treat a BLAZE_ROD as the "Wand" for now

        handler.addListener(PlayerUseItemEvent::class.java) { event ->
            if (event.itemStack.material() != Material.BLAZE_ROD) return@addListener

            val player = event.player
            val data = ReflactEngine.getPlayerManager().getPlayer(player.uuid) ?: return@addListener

            // Shift + Right Click = Slot 2
            // Right Click = Slot 1
            val slot = if (player.isSneaking) 2 else 1

            val spellId = data.getSpellInSlot(slot)
            if (spellId != null) {
                ReflactEngine.getSpellManager().cast(player, spellId)
            }
        }

        // Left Click = Slot 3? Or Melee?
        // Using Animation event for Left Click detection
        handler.addListener(PlayerHandAnimationEvent::class.java) { event ->
            if (event.player.itemInMainHand.material() != Material.BLAZE_ROD) return@addListener
            // Left click logic if needed
        }
    }
}
