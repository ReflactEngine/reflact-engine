package net.reflact.engine.networking.packet;

import com.google.gson.Gson;

/**
 * Base class for all Reflact packets.
 * Uses Gson for serialization.
 */
public abstract class ReflactPacket {
    // Shared Gson instance
    protected static final Gson gson = new Gson();

    /**
     * @return The unique ID of this packet type.
     */
    public abstract String getPacketId();

    public String toJson() {
        return gson.toJson(this);
    }
}