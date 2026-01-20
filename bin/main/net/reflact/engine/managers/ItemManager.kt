package net.reflact.engine.managers

import net.kyori.adventure.nbt.CompoundBinaryTag
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration
import net.minestom.server.component.DataComponents
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import net.minestom.server.item.component.CustomData
import net.minestom.server.item.component.CustomModelData
import net.reflact.common.item.CustomItem
import net.reflact.common.item.ItemType
import net.reflact.engine.registry.ReflactRegistry
import java.util.Optional
import java.util.List as JavaList

class ItemManager {
    private val itemRegistry = ReflactRegistry<CustomItem>("Items")

    fun register(item: CustomItem) {
        itemRegistry.register(item.id, item)
    }

    fun getTemplate(id: String): Optional<CustomItem> {
        return itemRegistry.get(id)
    }

    fun getTemplateIds(): Collection<String> {
        return itemRegistry.keys
    }

    fun createUnique(itemId: String): CustomItem? {
        val templateOpt = itemRegistry.get(itemId)
        return if (templateOpt.isEmpty) null else templateOpt.get().instantiate()
    }

    fun toItemStack(item: CustomItem?): ItemStack {
        if (item == null) return ItemStack.AIR

        val material = mapTypeToMaterial(item.type)

        val builder = ItemStack.builder(material)
            .set(
                DataComponents.CUSTOM_NAME,
                Component.text(item.tier.color).append(Component.text(item.name ?: "")).decoration(TextDecoration.ITALIC, false)
            )
            .set(
                DataComponents.CUSTOM_DATA, CustomData(
                    CompoundBinaryTag.builder()
                        .putString("reflact_uuid", item.uuid.toString())
                        .putString("reflact_template_id", item.id)
                        .build()
                )
            )

        if (item.customModelData != 0) {
            // New Minestom API requires lists for flags/strings/colors, or use a simpler helper if available.
            builder.set(
                DataComponents.CUSTOM_MODEL_DATA,
                CustomModelData(JavaList.of(item.customModelData.toFloat()), JavaList.of(), JavaList.of(), JavaList.of())
            )
        }

        return builder.build()
    }

    fun generateItemStack(itemId: String): ItemStack {
        val item = createUnique(itemId) ?: return ItemStack.AIR
        return toItemStack(item)
    }

    private fun mapTypeToMaterial(type: ItemType): Material {
        return when (type) {
            ItemType.WEAPON -> Material.NETHERITE_SWORD
            ItemType.HELMET -> Material.NETHERITE_HELMET
            ItemType.CHESTPLATE -> Material.NETHERITE_CHESTPLATE
            ItemType.LEGGINGS -> Material.NETHERITE_LEGGINGS
            ItemType.BOOTS -> Material.NETHERITE_BOOTS
            ItemType.ACCESSORY -> Material.AMETHYST_SHARD
            ItemType.MATERIAL -> Material.BLAZE_ROD
            else -> Material.MAGMA_CREAM
        }
    }
}
