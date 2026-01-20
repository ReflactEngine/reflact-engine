package net.reflact.engine.registry

import java.util.Collections
import java.util.Optional
import java.util.concurrent.ConcurrentHashMap

class ReflactRegistry<T>(private val name: String) {
    private val registry: MutableMap<String, T> = ConcurrentHashMap()

    fun register(id: String, value: T) {
        registry[id] = value
    }

    fun get(id: String): Optional<T> {
        return Optional.ofNullable(registry[id]) as Optional<T>
    }

    val keys: Collection<String>
        get() = Collections.unmodifiableCollection(registry.keys)

    val values: Collection<T>
        get() = Collections.unmodifiableCollection(registry.values)
}
