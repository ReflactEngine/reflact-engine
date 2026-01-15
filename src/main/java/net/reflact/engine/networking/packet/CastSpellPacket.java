package net.reflact.engine.networking.packet;

public class CastSpellPacket extends ReflactPacket {
    private final String spellId;

    public CastSpellPacket(String spellId) {
        this.spellId = spellId;
    }

    public String getSpellId() {
        return spellId;
    }

    @Override
    public String getPacketId() {
        return "cast_spell";
    }
}
