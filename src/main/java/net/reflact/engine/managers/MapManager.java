package net.reflact.engine.managers;

import net.minestom.server.entity.Player;
import net.reflact.common.network.packet.MapDataPacket;
import net.reflact.engine.ReflactEngine;

public class MapManager {
    
    private static final int MAP_SIZE = 128;

    public void sendMapData(Player player) {
        // Generate a server-side map pattern
        // For demonstration, we'll create a pattern that is clearly not client-generated
        // Size is 4 bytes per pixel (RGBA)
        byte[] colors = new byte[MAP_SIZE * MAP_SIZE * 4];
        
        for (int x = 0; x < MAP_SIZE; x++) {
            for (int z = 0; z < MAP_SIZE; z++) {
                // Create a pattern: Checkerboard
                boolean white = ((x / 8) + (z / 8)) % 2 == 0;
                int color = white ? 0xFFFFFFFF : 0xFF0000FF; // White and Blue
                
                // Colors are usually 4 bytes (ABGR or ARGB), but here we send int array or byte array?
                // Our packet takes byte[]. If we assume 1 byte per pixel (palette) or 4 bytes?
                // Client NativeImage usually wants RGBA (4 bytes).
                // Let's send 4 bytes per pixel.
                
                int index = (x + z * MAP_SIZE) * 4;
                if (index < 0 || index >= colors.length - 3) continue; // Safety check
                colors[index] = (byte) ((color >> 16) & 0xFF); // B (Blue)
                colors[index + 1] = (byte) ((color >> 8) & 0xFF);  // G (Green)
                colors[index + 2] = (byte) (color & 0xFF);         // R (Red)
                colors[index + 3] = (byte) ((color >> 24) & 0xFF); // A (Alpha) - usually 0xFF
            }
        }
        
        // Send packet
        // We center it at 0,0 for now, or relative to player? 
        // The packet has startX, startZ. Let's say this covers the area 0,0 to 128,128 world coords.
        // But for "Full Map", maybe we send a large area?
        // The user said "server sends the full map".
        
        ReflactEngine.getNetworkManager().sendPacket(player, new MapDataPacket(0, 0, MAP_SIZE, MAP_SIZE, colors));
    }
}
