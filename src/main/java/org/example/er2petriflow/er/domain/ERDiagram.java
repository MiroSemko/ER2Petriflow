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
        entities.remove(entity.getId());
        entityCounter--;
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

    public Entity getEntityByName(String name) {
        for(Entity e : entities.values()){
            if(Objects.equals(e.getName(), name))
                return e;
        }
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ERDiagram)) return false;

        ERDiagram erDiagram = (ERDiagram) o;

        if (!getEntities().equals(erDiagram.getEntities())) return false;
        if (!getRelations().equals(erDiagram.getRelations())) return false;
        if (!entityCounter.equals(erDiagram.entityCounter)) return false;
        return relationCounter.equals(erDiagram.relationCounter);
    }


    public String toVisualString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ERDiagram:\n\n");
        sb.append("Entities:\n");

        for (Entity entity : entities.values()) {
            sb.append("[").append(entity.getName()).append("]\n");
            for (Attribute attribute : entity.getAttributes()) {
                sb.append("  |---- ").append(attribute.getName()).append(" (").append(attribute.getType()).append(")\n");
            }
        }

        sb.append("\nRelations:\n");
        for (Relation relation : relations.values()) {
            List<Entity> connectedEntities = new ArrayList<>(relation.getConnections());

            sb.append(relation.getName()).append(" (");

            for (int i = 0; i < connectedEntities.size(); i++) {
                sb.append(connectedEntities.get(i).getName());
                if (i < connectedEntities.size() - 1) {
                    sb.append(", ");
                }
            }
            sb.append(")\n");

            for (Attribute attribute : relation.getAttributes()) {
                sb.append("  |---- ").append(attribute.getName()).append(" (").append(attribute.getType()).append(")\n");
            }
        }

        return sb.toString();
    }




}
