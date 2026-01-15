package net.reflact.engine.managers;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.component.DataComponent;
import net.minestom.server.component.DataComponents;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.component.CustomData;
import net.reflact.engine.item.ItemType;
import net.reflact.engine.item.RpgItem;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ItemManager {
    private final Map<String, RpgItem> itemTemplates = new HashMap<>();

    public void register(RpgItem item) {
        itemTemplates.put(item.getId(), item);
    }

    public RpgItem getTemplate(String id) {
        return itemTemplates.get(id);
    }

    public RpgItem createUnique(String itemId) {
        RpgItem template = itemTemplates.get(itemId);
        if (template == null) return null;

        RpgItem uniqueItem = new RpgItem(template.getId(), template.getDisplayName(), template.getType(), template.getTier());
        uniqueItem.setLore(template.getLore());
        uniqueItem.setLevelRequirement(template.getLevelRequirement());
        uniqueItem.setClassRequirement(template.getClassRequirement());
        template.getAttributes().forEach(uniqueItem::setAttribute);
        
        return uniqueItem;
    }

    public ItemStack toItemStack(RpgItem item) {
        if (item == null) return ItemStack.AIR;
        
        Material material = mapTypeToMaterial(item.getType());

        return ItemStack.builder(material)
                .set(DataComponents.CUSTOM_NAME, Component.text(item.getTier().getColor()).append(Component.text(item.getDisplayName())).decoration(TextDecoration.ITALIC, false))
                .set(DataComponents.CUSTOM_DATA, new CustomData(CompoundBinaryTag.builder()
                        .putString("reflact_uuid", item.getUuid().toString())
                        .putString("reflact_template_id", item.getId())
                        .build()))
                .build();
    }

    public ItemStack generateItemStack(String itemId) {
        return toItemStack(createUnique(itemId));
    }
    
    private Material mapTypeToMaterial(ItemType type) {
        return switch (type) {
            case WEAPON -> Material.IRON_SWORD;
            case HELMET -> Material.IRON_HELMET;
            case CHESTPLATE -> Material.IRON_CHESTPLATE;
            case LEGGINGS -> Material.IRON_LEGGINGS;
            case BOOTS -> Material.IRON_BOOTS;
            case ACCESSORY -> Material.GOLD_NUGGET;
            case MATERIAL -> Material.STICK;
            default -> Material.STONE;
        };
    }
}
