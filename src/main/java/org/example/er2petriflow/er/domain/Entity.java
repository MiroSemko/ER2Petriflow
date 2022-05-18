package org.example.er2petriflow.er.domain;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

public class Entity {

    @Getter
    @Setter
    private Long id;

    @Getter
    private final String name;
    @Getter
    private final List<Attribute> attributes;

    private int attributeCounter = 0;

    @Getter
    @Setter
    private String processIdentifier;

    public Entity(String name) {
        this.name = name;
        this.attributes = new ArrayList<>();
    }

    public void addAttribute(Attribute attribute) {
        this.attributes.add(attribute);
        attributeCounter++;
        attribute.setVariableIdentifier("variable" + attributeCounter);
    }
}
