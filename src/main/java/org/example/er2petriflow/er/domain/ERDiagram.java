package org.example.er2petriflow.er.domain;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class ERDiagram {

    private List<Entity> entities;
    private List<Relation> relations;

    public ERDiagram() {
        this.entities = new ArrayList<>();
        this.relations = new ArrayList<>();
    }

    public void addEntity(Entity entity) {
        entities.add(entity);
    }

    public void addRelation(Relation relation) {
        relations.add(relation);
    }

}
