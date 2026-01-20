package net.reflact.engine.spells.dynamic

import java.util.HashMap
import java.util.function.Supplier

object EffectRegistry {
    private val REGISTRY: MutableMap<String, Supplier<SpellEffect>> = HashMap()

    init {
        register("damage") { DamageEffect() }
        register("projectile") { ProjectileEffect() }
    }

    fun register(type: String, factory: Supplier<SpellEffect>) {
        REGISTRY[type] = factory
    }

    fun create(type: String): SpellEffect? {
        val supplier = REGISTRY[type]
        return supplier?.get()
    }
}
