package net.reflact.engine.attributes;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class AttributeContainer {
    private final Map<Attribute, Double> baseValues = new ConcurrentHashMap<>();
    private final Map<Attribute, List<AttributeModifier>> modifiers = new ConcurrentHashMap<>();

    // Cache for performance? For now, recalculate on get (safe).
    
    public void setBaseValue(Attribute attribute, double value) {
        baseValues.put(attribute, value);
    }

    public double getBaseValue(Attribute attribute) {
        return baseValues.getOrDefault(attribute, attribute.baseValue());
    }

    public void addModifier(Attribute attribute, AttributeModifier modifier) {
        modifiers.computeIfAbsent(attribute, k -> new ArrayList<>()).add(modifier);
    }

    public void removeModifier(Attribute attribute, String modifierId) {
        if (modifiers.containsKey(attribute)) {
            modifiers.get(attribute).removeIf(mod -> mod.id().equals(modifierId));
        }
    }
    
    public void clearModifiersByPrefix(String prefix) {
        for (List<AttributeModifier> mods : modifiers.values()) {
            mods.removeIf(mod -> mod.id().startsWith(prefix));
        }
    }

    public double getValue(Attribute attribute) {
        double base = getBaseValue(attribute);
        
        List<AttributeModifier> mods = modifiers.get(attribute);
        if (mods == null || mods.isEmpty()) return Math.max(attribute.minValue(), Math.min(attribute.maxValue(), base));

        double addNumber = 0;
        double addScalar = 0;
        double multiply = 1;

        for (AttributeModifier mod : mods) {
            switch (mod.operation()) {
                case ADD_NUMBER -> addNumber += mod.amount();
                case ADD_SCALAR -> addScalar += mod.amount();
                case MULTIPLY -> multiply *= mod.amount();
            }
        }

        double finalValue = (base + addNumber) * (1 + addScalar) * multiply;
        return Math.max(attribute.minValue(), Math.min(attribute.maxValue(), finalValue));
    }
}
