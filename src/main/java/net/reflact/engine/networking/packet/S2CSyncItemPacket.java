package net.reflact.engine.networking.packet;

import net.reflact.engine.item.RpgItem;

public class S2CSyncItemPacket extends ReflactPacket {
    private final RpgItem item;
    private final int slotId;

    public S2CSyncItemPacket(RpgItem item, int slotId) {
        this.item = item;
        this.slotId = slotId;
    }

    public RpgItem getItem() {
        return item;
    }

    public int getSlotId() {
        return slotId;
    }

    @Override
    public String getPacketId() {
        return "sync_item";
    }
}