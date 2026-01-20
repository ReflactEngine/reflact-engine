package net.reflact.engine.guild

import net.minestom.server.MinecraftServer
import net.minestom.server.coordinate.Pos
import net.minestom.server.entity.Player
import net.minestom.server.event.player.PlayerBlockBreakEvent
import net.minestom.server.event.player.PlayerBlockPlaceEvent
import net.minestom.server.instance.InstanceContainer
import net.minestom.server.instance.block.Block
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlin.concurrent.thread

class GuildIslandManager {

    lateinit var guildInstance: InstanceContainer
        private set
    
    private val editModeMap: MutableMap<UUID, Boolean> = ConcurrentHashMap()

    companion object {
        private const val ISLAND_SPACING = 2000
        private const val REAL_RADIUS = 500
    }

    fun init() {
        val instanceManager = MinecraftServer.getInstanceManager()

        // Use default dimension for now to avoid registry complexity in this snippet
        
        guildInstance = instanceManager.createInstanceContainer()
        guildInstance.setGenerator { unit ->
            // Void generator (do nothing)
        }

        // Listeners for Edit Mode
        val handler = MinecraftServer.getGlobalEventHandler()
        handler.addListener(PlayerBlockBreakEvent::class.java) { event ->
            if (event.instance != guildInstance) return@addListener
            if (!isInEditMode(event.player)) {
                event.isCancelled = true
            }
        }

        handler.addListener(PlayerBlockPlaceEvent::class.java) { event ->
            if (event.instance != guildInstance) return@addListener
            if (!isInEditMode(event.player)) {
                event.isCancelled = true
            }
        }
    }

    fun createIsland(guildId: Int) {
        // Calculate position based on ID to avoid overlap
        // Simple grid: x = (id % 100) * SPACING, z = (id / 100) * SPACING
        val x = (guildId % 100) * ISLAND_SPACING
        val z = (guildId / 100) * ISLAND_SPACING
        val y = 100

        // Async generation
        thread {
            for (dx in -REAL_RADIUS..REAL_RADIUS) {
                for (dz in -REAL_RADIUS..REAL_RADIUS) {
                    if (dx * dx + dz * dz <= REAL_RADIUS * REAL_RADIUS) {
                        guildInstance.setBlock(x + dx, y, z + dz, Block.GRASS_BLOCK)
                        // Bedrock layer underneath?
                        // guildInstance.setBlock(x + dx, y - 1, z + dz, Block.BEDROCK);
                    }
                }
            }
        }
    }

    fun teleportToIsland(player: Player, guildId: Int) {
        val x = (guildId % 100) * ISLAND_SPACING
        val z = (guildId / 100) * ISLAND_SPACING
        val y = 101

        player.setInstance(guildInstance, Pos(x.toDouble(), y.toDouble(), z.toDouble()))
    }

    fun toggleEditMode(player: Player) {
        val current = editModeMap.getOrDefault(player.uuid, false)
        editModeMap[player.uuid] = !current
        player.sendMessage("Edit mode: " + if (!current) "ON" else "OFF")
    }

    fun isInEditMode(player: Player): Boolean {
        return editModeMap.getOrDefault(player.uuid, false)
    }
}
