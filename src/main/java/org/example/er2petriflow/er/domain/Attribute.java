package org.example.er2petriflow.er.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
public class Attribute {

    private final String name;
    private final AttributeType type;

    @Setter
    private String variableIdentifier;

    public Attribute(String name, AttributeType type) {
        this.name = name;
        this.type = type;
    }
}
