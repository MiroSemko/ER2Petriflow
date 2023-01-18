package org.example.er2petriflow.er.domain;

import lombok.Getter;

import java.util.*;

public class ERDiagram {

    private final HashMap<Long, Entity> entities;
    private final HashMap<Long, Relation> relations;

    private Long entityCounter = 0L;
    private Long relationCounter = 0L;

    public ERDiagram() {
        this.entities = new HashMap<>();
        this.relations = new HashMap<>();
    }

    public void addEntity(Entity entity) {
        entityCounter++;
        entity.setId(entityCounter);
        entity.setProcessIdentifier(entity.getName());
        entities.put(entityCounter, entity);
    }

    public void removeEntity(Entity entity){
        //todo maybe counter?
        entities.remove(entityCounter,entity);
        entityCounter--;
        entity = null;
    }

    public void addRelation(Relation relation) {
        relationCounter++;
        relation.setId(relationCounter);
        relation.setProcessIdentifier(relation.getName());
        relations.put(relationCounter, relation);
    }

    public List<Entity> getEntities() {
        return new ArrayList<>(entities.values());
    }

    public List<Relation> getRelations() {
        return new ArrayList<>(relations.values());
    }

    public Map<Long, Entity> getEntityMap() {
        return this.entities;
    }
}
