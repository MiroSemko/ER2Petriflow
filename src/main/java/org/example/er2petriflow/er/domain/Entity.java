package org.example.er2petriflow.er.domain;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class Entity {

    private final String name;
    private final List<Attribute> attributes;

    public Entity(String name) {
        this.name = name;
        this.attributes = new ArrayList<>();
    }

    public void addAttribute(Attribute attribute) {
        this.attributes.add(attribute);
    }
}
