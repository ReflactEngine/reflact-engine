package net.reflact.engine.managers

import com.google.gson.GsonBuilder
import net.minestom.server.component.DataComponents
import net.minestom.server.entity.EquipmentSlot
import net.minestom.server.entity.Player
import net.minestom.server.entity.attribute.Attribute as MinestomAttribute
import net.reflact.common.attribute.Attribute
import net.reflact.common.attribute.AttributeModifier
import net.reflact.common.attribute.AttributeRegistry
import net.reflact.common.attribute.RpgAttributes
import net.reflact.common.item.CustomItem
import net.reflact.engine.ReflactEngine
import net.reflact.engine.data.ReflactPlayer
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class PlayerManager {
    companion object {
        private val LOGGER = LoggerFactory.getLogger(PlayerManager::class.java)
        private val GSON = GsonBuilder().setPrettyPrinting().create()
        private val DATA_FOLDER = File("data/players")
    }

    private val players: MutableMap<UUID, ReflactPlayer> = ConcurrentHashMap()

    init {
        if (!DATA_FOLDER.exists()) {
            DATA_FOLDER.mkdirs()
        }
    }

    fun getPlayer(uuid: UUID): ReflactPlayer? {
        return players[uuid]
    }

    fun loadPlayer(uuid: UUID, username: String): ReflactPlayer {
        val file = File(DATA_FOLDER, "$uuid.json")
        var player: ReflactPlayer

        if (file.exists()) {
            player = try {
                FileReader(file).use { reader ->
                    GSON.fromJson(reader, ReflactPlayer::class.java)
                }
            } catch (e: IOException) {
                LOGGER.error("Failed to load player data for $uuid", e)
                ReflactPlayer(uuid, username)
            }
            
            // Fix Mana if 0 (due to transient or first load)
            if (player.currentMana <= 1.0) {
                var maxMana = player.attributes.getValue(RpgAttributes.MANA)
                if (maxMana <= 0) maxMana = 100.0 // Fallback
                player.currentMana = maxMana
            }
        } else {
            player = ReflactPlayer(uuid, username)
        }

        // Ensure username is up to date in case of name change (optional logic)
        players[uuid] = player
        return player
    }

    fun savePlayer(uuid: UUID) {
        val player = players[uuid] ?: return

        val file = File(DATA_FOLDER, "$uuid.json")
        try {
            FileWriter(file).use { writer ->
                GSON.toJson(player, writer)
            }
        } catch (e: IOException) {
            LOGGER.error("Failed to save player data for $uuid", e)
        }
    }

    fun unloadPlayer(uuid: UUID) {
        savePlayer(uuid)
        players.remove(uuid)
    }

    fun recalculateStats(player: Player) {
        val data = getPlayer(player.uuid) ?: return

        // 1. Clear "Equipment" modifiers
        data.attributes.clearModifiersByPrefix("equip_")

        // 2. Iterate equipment
        for (slot in EquipmentSlot.values()) {
            val stack = player.getEquipment(slot)
            if (stack.isAir) continue

            // Get UUID from NBT
            val customData = stack.get(DataComponents.CUSTOM_DATA) ?: continue

            var templateId = ""
            try {
                // Read from NBT
                if (customData.nbt().contains("reflact_template_id")) {
                    templateId = customData.nbt().getString("reflact_template_id")
                }
            } catch (e: Exception) { continue }

            if (templateId.isEmpty()) continue

            // Lookup Template
            val template = ReflactEngine.getItemManager().getTemplate(templateId).orElse(null) ?: continue

            // Apply attributes
            for ((attrId, value) in template.attributes) {
                // Find Attribute object
                val attr = AttributeRegistry.get(attrId).orElse(null)
                if (attr != null) {
                    // Create modifier with ID "equip_<slot>_<attr>"
                    // Assuming slot names are unique
                    val modId = "equip_${slot.name}_$attrId"
                    data.attributes.addModifier(attr, AttributeModifier(modId, value, AttributeModifier.Operation.ADD_NUMBER))
                }
            }
        }

        // 3. Iterate Accessories
        for ((slot, itemId) in data.accessories) {
            if (itemId == null || itemId.isEmpty()) continue

            val template = ReflactEngine.getItemManager().getTemplate(itemId).orElse(null) ?: continue

            for ((attrId, value) in template.attributes) {
                val attr = AttributeRegistry.get(attrId).orElse(null)
                if (attr != null) {
                    val modId = "equip_acc_${slot}_$attrId"
                    data.attributes.addModifier(attr, AttributeModifier(modId, value, AttributeModifier.Operation.ADD_NUMBER))
                }
            }
        }

        // Apply to Player Entity
        val health = data.attributes.getValue(RpgAttributes.HEALTH)
        val walkSpeed = data.attributes.getValue(RpgAttributes.WALK_SPEED)

        player.getAttribute(MinestomAttribute.MAX_HEALTH).baseValue = health
        player.heal() // Heal on recalc? Maybe not always.
        player.getAttribute(MinestomAttribute.MOVEMENT_SPEED).baseValue = walkSpeed
    }
}
