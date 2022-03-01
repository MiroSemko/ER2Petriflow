package org.example.er2petriflow.er;

public enum Key {
    POTENTIAL_COMPACT_ENTITY("NodeStyle");

    private final String attributeName;

    Key(String attributeName) {
        this.attributeName = attributeName;
    }

    public String getAttributeName() {
        return attributeName;
    }
}
