package net.reflact.engine.managers;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.component.DataComponents;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.component.CustomData;
import net.minestom.server.item.component.CustomModelData;
import net.reflact.engine.item.ItemType;
import net.reflact.engine.item.RpgItem;
import net.reflact.engine.registry.ReflactRegistry;

import java.util.Optional;

public class ItemManager {
    private final ReflactRegistry<RpgItem> itemRegistry = new ReflactRegistry<>("Items");

    public void register(RpgItem item) {
        itemRegistry.register(item.getId(), item);
    }

    public Optional<RpgItem> getTemplate(String id) {
        return itemRegistry.get(id);
    }

    public RpgItem createUnique(String itemId) {
        Optional<RpgItem> templateOpt = itemRegistry.get(itemId);
        if (templateOpt.isEmpty()) return null;

        RpgItem template = templateOpt.get();
        RpgItem uniqueItem = new RpgItem(template.getId(), template.getDisplayName(), template.getType(), template.getTier());
        uniqueItem.setLore(template.getLore());
        uniqueItem.setLevelRequirement(template.getLevelRequirement());
        uniqueItem.setClassRequirement(template.getClassRequirement());
        uniqueItem.setCustomModelData(template.getCustomModelData());
        template.getAttributes().forEach(uniqueItem::setAttribute);
        
        return uniqueItem;
    }

    public ItemStack toItemStack(RpgItem item) {
        if (item == null) return ItemStack.AIR;
        
        Material material = mapTypeToMaterial(item.getType());

        var builder = ItemStack.builder(material)
                .set(DataComponents.CUSTOM_NAME, Component.text(item.getTier().getColor()).append(Component.text(item.getDisplayName())).decoration(TextDecoration.ITALIC, false))
                .set(DataComponents.CUSTOM_DATA, new CustomData(CompoundBinaryTag.builder()
                        .putString("reflact_uuid", item.getUuid().toString())
                        .putString("reflact_template_id", item.getId())
                        .build()));

        if (item.getCustomModelData() != 0) {
            builder.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(item.getCustomModelData()));
        }

        return builder.build();
    }

    public ItemStack generateItemStack(String itemId) {
        return toItemStack(createUnique(itemId));
    }
    
    private Material mapTypeToMaterial(ItemType type) {
        return switch (type) {
            case WEAPON -> Material.NETHERITE_SWORD;
            case HELMET -> Material.NETHERITE_HELMET;
            case CHESTPLATE -> Material.NETHERITE_CHESTPLATE;
            case LEGGINGS -> Material.NETHERITE_LEGGINGS;
            case BOOTS -> Material.NETHERITE_BOOTS;
            case ACCESSORY -> Material.AMETHYST_SHARD;
            case MATERIAL -> Material.BLAZE_ROD;
            default -> Material.MAGMA_CREAM;
        };
    }
}
