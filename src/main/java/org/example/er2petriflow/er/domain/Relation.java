package org.example.er2petriflow.er.domain;

import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
public class Relation {

    @Setter
    private String processIdentifier;

    private final String name;
    private final Set<Entity> connections;

    public Relation(String name) {
        this.name = name;
        this.connections = new HashSet<>();
    }

    public void addEntity(Entity e) {
        this.connections.add(e);
    }
}
