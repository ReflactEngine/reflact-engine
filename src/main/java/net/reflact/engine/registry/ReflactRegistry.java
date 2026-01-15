package net.reflact.engine.registry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * A generic registry for modular systems.
 * @param <T> The type of object being registered.
 */
public class ReflactRegistry<T> {
    private final Logger logger;
    private final Map<String, T> registry = new HashMap<>();

    public ReflactRegistry(String name) {
        this.logger = LoggerFactory.getLogger("Registry-" + name);
    }

    public void register(String id, T item) {
        if (registry.containsKey(id)) {
            logger.warn("Duplicate registration for ID: {}", id);
            return;
        }
        registry.put(id, item);
        logger.info("Registered: {}", id);
    }

    public Optional<T> get(String id) {
        return Optional.ofNullable(registry.get(id));
    }

    public Collection<T> getAll() {
        return registry.values();
    }
    
    public Collection<String> getKeys() {
        return registry.keySet();
    }
}
