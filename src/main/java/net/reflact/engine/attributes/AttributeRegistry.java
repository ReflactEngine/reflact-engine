package net.reflact.engine.attributes;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class AttributeRegistry {
    private static final Map<String, Attribute> attributes = new HashMap<>();

    public static void register(Attribute attribute) {
        attributes.put(attribute.id(), attribute);
    }

    public static Optional<Attribute> get(String id) {
        return Optional.ofNullable(attributes.get(id));
    }
    
    public static Collection<Attribute> getAll() {
        return attributes.values();
    }
}
