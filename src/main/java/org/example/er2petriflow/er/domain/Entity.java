package org.example.er2petriflow.er.domain;

import lombok.Getter;
import lombok.Setter;
import org.example.er2petriflow.generated.petriflow.Document;

import java.util.Objects;

public class Entity extends AttributeOwner {

    @Getter
    @Setter
    private Long id;

    @Getter
    private final String name;

    @Getter
    @Setter
    private String processIdentifier;

    @Getter
    @Setter
    private Document petriflow;

    @Getter
    private int processedRelations = 0;

    public Entity(String name) {
        super();
        this.name = name;
    }

    public void incrementProcessedRelations() {
        this.processedRelations++;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Entity)) {
            return false;
        }
        Entity other = (Entity) obj;
        return Objects.equals(this.getId(), other.getId()) &&
                Objects.equals(this.getName(), other.getName()) &&
                Objects.equals(this.getProcessIdentifier(), other.getProcessIdentifier()) &&
                Objects.equals(this.getPetriflow(), other.getPetriflow()) &&
                Objects.equals(this.getProcessedRelations(), other.getProcessedRelations()) &&
                Objects.equals(this.getAttributes().size(), other.getAttributes().size()) &&
                other.getAttributes().containsAll(this.getAttributes());
    }



}
