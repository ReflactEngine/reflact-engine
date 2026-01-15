package net.reflact.engine.networking.packet;

import com.google.gson.Gson;

public abstract class ReflactPacket {
    protected static final Gson gson = new Gson();
    
    public abstract PacketType getType();
    
    public String toJson() {
        return gson.toJson(this);
    }
    
    public static <T extends ReflactPacket> T fromJson(String json, Class<T> clazz) {
        return gson.fromJson(json, clazz);
    }
}
