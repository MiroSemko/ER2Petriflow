package org.example.er2petriflow.er.domain;

import lombok.Getter;
import lombok.Setter;
import org.example.er2petriflow.generated.petriflow.Document;

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
}
