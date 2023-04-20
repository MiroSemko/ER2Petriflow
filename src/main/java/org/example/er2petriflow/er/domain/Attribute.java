package org.example.er2petriflow.er.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
public class Attribute {

    private final String name;
    private final AttributeType type;
    private final boolean titlePart;

    @Setter
    private String variableIdentifier;

    public Attribute(String name, AttributeType type) {
        this(name, type, false);
    }

    public Attribute(String name, AttributeType type, boolean titlePart) {
        this.name = name;
        this.type = type;
        this.titlePart = titlePart;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Attribute)) return false;

        Attribute attribute = (Attribute) o;

        if (isTitlePart() != attribute.isTitlePart()) return false;
        if (!getName().equals(attribute.getName())) return false;
        if (getType() != attribute.getType()) return false;
        return getVariableIdentifier() != null ? getVariableIdentifier().equals(attribute.getVariableIdentifier()) : attribute.getVariableIdentifier() == null;
    }

}
