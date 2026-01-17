package net.reflact.engine.managers;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.component.DataComponents;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.component.CustomData;
import net.minestom.server.item.component.CustomModelData;
import net.reflact.common.item.CustomItem;
import net.reflact.common.item.ItemType;
import net.reflact.engine.registry.ReflactRegistry;

import java.util.Optional;
import java.util.List;

public class ItemManager {
    private final ReflactRegistry<CustomItem> itemRegistry = new ReflactRegistry<>("Items");

    public void register(CustomItem item) {
        itemRegistry.register(item.getId(), item);
    }

    public Optional<CustomItem> getTemplate(String id) {
        return itemRegistry.get(id);
    }

    public CustomItem createUnique(String itemId) {
        Optional<CustomItem> templateOpt = itemRegistry.get(itemId);
        if (templateOpt.isEmpty()) return null;

        return templateOpt.get().instantiate();
    }

    public ItemStack toItemStack(CustomItem item) {
        if (item == null) return ItemStack.AIR;
        
        Material material = mapTypeToMaterial(item.getType());

        var builder = ItemStack.builder(material)
                .set(DataComponents.CUSTOM_NAME, Component.text(item.getTier().getColor()).append(Component.text(item.getName())).decoration(TextDecoration.ITALIC, false))
                .set(DataComponents.CUSTOM_DATA, new CustomData(CompoundBinaryTag.builder()
                        .putString("reflact_uuid", item.getUuid().toString())
                        .putString("reflact_template_id", item.getId())
                        .build()));

        if (item.getCustomModelData() != 0) {
            // New Minestom API requires lists for flags/strings/colors, or use a simpler helper if available.
            // Using raw values for now or assuming just integer for old behavior
             builder.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(List.of((float)item.getCustomModelData()), List.of(), List.of(), List.of()));
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
