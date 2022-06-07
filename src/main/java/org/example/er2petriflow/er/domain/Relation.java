package org.example.er2petriflow.er.domain;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
public class Relation {

    @Getter
    @Setter
    private Long id;

    @Setter
    private String processIdentifier;

    private final String name;
    private final Set<Entity> connections;
    @Getter
    private final List<Attribute> attributes;

    public Relation(String name) {
        this.name = name;
        this.connections = new HashSet<>();
        this.attributes = new ArrayList<>();
    }

    public void addEntity(Entity e) {
        this.connections.add(e);
    }

    public void addAttribute(Attribute a) {
        this.attributes.add(a);
    }
}
