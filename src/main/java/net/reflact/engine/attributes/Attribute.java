package net.reflact.engine.attributes;

public record Attribute(String id, double baseValue, double minValue, double maxValue) {
    public Attribute(String id, double baseValue) {
        this(id, baseValue, Double.MIN_VALUE, Double.MAX_VALUE);
    }
}
