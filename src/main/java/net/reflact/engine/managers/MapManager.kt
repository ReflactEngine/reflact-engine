package net.reflact.engine.managers

import net.minestom.server.entity.Player
import net.reflact.common.network.packet.MapDataPacket
import net.reflact.engine.ReflactEngine

class MapManager {

    companion object {
        private const val MAP_SIZE = 128
    }

    fun sendMapData(player: Player) {
        // Generate a server-side map pattern
        // For demonstration, we'll create a pattern that is clearly not client-generated
        // Size is 4 bytes per pixel (RGBA)
        val colors = ByteArray(MAP_SIZE * MAP_SIZE * 4)

        for (x in 0 until MAP_SIZE) {
            for (z in 0 until MAP_SIZE) {
                // Create a pattern: Checkerboard
                val white = ((x / 8) + (z / 8)) % 2 == 0
                val color = if (white) -0x1 else -0xffff01 // White and Blue (0xFF0000FF but int)
                
                // Kotlin Bit operations
                // 0xFFFFFFFF is -1 in Int
                // 0xFF0000FF is -16776961 ? No, 0xFF0000FF = A:FF R:00 G:00 B:FF (Blue+Alpha)
                
                val index = (x + z * MAP_SIZE) * 4
                if (index < 0 || index >= colors.size - 3) continue // Safety check

                colors[index] = ((color shr 16) and 0xFF).toByte() // B (Blue)
                colors[index + 1] = ((color shr 8) and 0xFF).toByte()  // G (Green)
                colors[index + 2] = (color and 0xFF).toByte()         // R (Red)
                colors[index + 3] = ((color shr 24) and 0xFF).toByte() // A (Alpha)
            }
        }

        // Send packet
        ReflactEngine.getNetworkManager().sendPacket(player, MapDataPacket(0, 0, MAP_SIZE, MAP_SIZE, colors))
    }
}
