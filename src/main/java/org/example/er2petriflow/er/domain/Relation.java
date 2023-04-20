package org.example.er2petriflow.er.domain;

import lombok.Getter;
import lombok.Setter;

import java.util.*;

@Getter
public class Relation extends AttributeOwner {

    @Getter
    @Setter
    private Long id;

    @Setter
    private String processIdentifier;

    private final String name;
    private final Set<Entity> connections;

    public Relation(String name) {
        super();
        this.name = name;
        this.connections = new HashSet<>();
    }

    public void addEntity(Entity e) {
        this.connections.add(e);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Relation)) {
            return false;
        }
        Relation other = (Relation) obj;
        if (!Objects.equals(this.getId(), other.getId()) ||
                !Objects.equals(this.getName(), other.getName()) ||
                !Objects.equals(this.getProcessIdentifier(), other.getProcessIdentifier()) ||
                !Objects.equals(this.getConnections().size(), other.getConnections().size()) ||
                !Objects.equals(this.getAttributes().size(), other.getAttributes().size())) {
            return false;
        }

        List<Entity> thisConnections = new ArrayList<>(this.getConnections());
        List<Entity> otherConnections = new ArrayList<>(other.getConnections());

        Comparator<Entity> entityComparator = Comparator.comparing(Entity::getId);
        thisConnections.sort(entityComparator);
        otherConnections.sort(entityComparator);

        for (int i = 0; i < thisConnections.size(); i++) {
            if (!thisConnections.get(i).equals(otherConnections.get(i))) {
                return false;
            }
        }

        return other.getAttributes().containsAll(this.getAttributes());
    }

}
