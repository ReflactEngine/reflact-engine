package net.reflact.engine.attributes;

import java.util.UUID;

public record AttributeModifier(String id, double amount, Operation operation) {
    public enum Operation {
        ADD_NUMBER, // +10
        ADD_SCALAR, // +10% of base
        MULTIPLY    // * 1.5 final
    }
}
