package net.reflact.engine.spells

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.minestom.server.entity.Player
import net.reflact.common.attribute.RpgAttributes
import net.reflact.common.network.packet.CastSlotPacket
import net.reflact.common.network.packet.CastSpellPacket
import net.reflact.common.network.packet.ManaUpdatePacket
import net.reflact.engine.ReflactEngine
import net.reflact.engine.registry.ReflactRegistry
import org.slf4j.LoggerFactory
import java.util.Optional
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class SpellManager {
    companion object {
        private val LOGGER = LoggerFactory.getLogger(SpellManager::class.java)
    }

    private val spellRegistry = ReflactRegistry<Spell>("Spells")

    // Cooldowns
    private val cooldowns: MutableMap<UUID, MutableMap<String, Long>> = ConcurrentHashMap()

    fun init() {
        ReflactEngine.getNetworkManager().registerHandler("cast_spell") { player, packet ->
            if (packet is CastSpellPacket) {
                cast(player, packet.spellId)
            }
        }

        ReflactEngine.getNetworkManager().registerHandler("cast_slot") { player, packet ->
            if (packet is CastSlotPacket) {
                val data = ReflactEngine.getPlayerManager().getPlayer(player.uuid)
                if (data != null) {
                    val spellId = data.getSpellAt(packet.slotIndex)
                    if (spellId != null) {
                        cast(player, spellId)
                    } else {
                        player.sendActionBar(Component.text("No spell in slot " + packet.slotIndex, NamedTextColor.RED))
                    }
                }
            }
        }
    }

    fun register(spell: Spell) {
        spellRegistry.register(spell.id, spell)
        LOGGER.info("Registered spell: {}", spell.name)
    }

    fun getSpell(id: String): Optional<Spell> {
        return spellRegistry.get(id)
    }

    fun cast(player: Player, spellId: String): Boolean {
        LOGGER.info("Attempting to cast spell: {} for player {}", spellId, player.username)
        val spellOpt = spellRegistry.get(spellId)
        if (spellOpt.isEmpty) {
            LOGGER.warn("Player {} tried to cast unknown spell {}", player.username, spellId)
            return false
        }
        val spell = spellOpt.get()
        val uuid = player.uuid
        val now = System.currentTimeMillis()

        if (isOnCooldown(uuid, spellId)) {
            val remaining = (cooldowns[uuid]!![spellId]!! - now) / 1000
            player.sendActionBar(Component.text("Cooldown: ${remaining}s", NamedTextColor.RED))
            return false
        }

        val reflactPlayer = ReflactEngine.getPlayerManager().getPlayer(uuid)
        if (reflactPlayer != null) {
            if (reflactPlayer.currentMana < spell.manaCost) {
                player.sendActionBar(Component.text("Not enough Mana!", NamedTextColor.BLUE))
                return false
            }
            reflactPlayer.currentMana = reflactPlayer.currentMana - spell.manaCost

            ReflactEngine.getNetworkManager().sendPacket(
                player,
                ManaUpdatePacket(reflactPlayer.currentMana, reflactPlayer.attributes.getValue(RpgAttributes.MANA))
            )
        }

        cooldowns.computeIfAbsent(uuid) { ConcurrentHashMap() }[spellId] = now + spell.cooldownMillis

        spell.onCast(player)
        return true;
    }

    private fun isOnCooldown(uuid: UUID, spellId: String): Boolean {
        if (!cooldowns.containsKey(uuid)) return false
        val end = cooldowns[uuid]!![spellId] ?: return false
        return end > System.currentTimeMillis()
    }
}
