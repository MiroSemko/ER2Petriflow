package org.example.er2petriflow.er.domain;

import lombok.Getter;
import lombok.Setter;
import org.example.er2petriflow.generated.petriflow.Document;
import org.example.er2petriflow.util.IncrementingCounter;

import java.util.ArrayList;
import java.util.List;

public class Entity {

    @Getter
    @Setter
    private Long id;

    @Getter
    private final String name;
    @Getter
    private final List<Attribute> attributes;

    private final IncrementingCounter counter;

    @Getter
    @Setter
    private String processIdentifier;

    @Getter
    @Setter
    private Document petriflow;

    @Getter
    private int processedRelations = 0;

    public Entity(String name) {
        this.name = name;
        this.attributes = new ArrayList<>();
        this.counter = new IncrementingCounter();
    }

    public void addAttribute(Attribute attribute) {
        this.attributes.add(attribute);
        attribute.setVariableIdentifier("variable" + counter.next());
    }

    public void incrementProcessedRelations() {
        this.processedRelations++;
    }
}
