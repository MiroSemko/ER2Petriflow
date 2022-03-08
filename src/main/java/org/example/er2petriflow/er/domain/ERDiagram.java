package org.example.er2petriflow.er.domain;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class ERDiagram {

    @Getter
    private final List<Entity> entities;
    @Getter
    private final List<Relation> relations;

    private int entityCounter = 0;

    public ERDiagram() {
        this.entities = new ArrayList<>();
        this.relations = new ArrayList<>();
    }

    public void addEntity(Entity entity) {
        entities.add(entity);
        entityCounter++;
        entity.setProcessIdentifier("Entity" + entityCounter);
    }

    public void addRelation(Relation relation) {
        relations.add(relation);
    }

}
