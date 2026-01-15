package net.reflact.engine.networking.packet;

public class ManaUpdatePacket extends ReflactPacket {
    private final double mana;

    public ManaUpdatePacket(double mana) {
        this.mana = mana;
    }

    public double getMana() {
        return mana;
    }

    @Override
    public String getPacketId() {
        return "mana_update";
    }
}
