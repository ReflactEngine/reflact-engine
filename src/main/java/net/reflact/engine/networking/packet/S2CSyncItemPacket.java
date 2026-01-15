package net.reflact.engine.networking.packet;

import net.reflact.engine.item.RpgItem;

public class S2CSyncItemPacket extends ReflactPacket {
    private RpgItem item;
    private int slotId; // -1 for main hand, etc.

    public S2CSyncItemPacket(RpgItem item, int slotId) {
        this.item = item;
        this.slotId = slotId;
    }

    @Override
    public PacketType getType() {
        return PacketType.S2C_SYNC_ITEM;
    }
    
    public RpgItem getItem() { return item; }
    public int getSlotId() { return slotId; }
}
