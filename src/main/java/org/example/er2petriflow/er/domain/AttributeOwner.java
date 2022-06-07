package org.example.er2petriflow.er.domain;

import lombok.Getter;
import org.example.er2petriflow.util.IncrementingCounter;

import java.util.ArrayList;
import java.util.List;

public abstract class AttributeOwner {
    @Getter
    private final List<Attribute> attributes;
    private final IncrementingCounter counter;

    public AttributeOwner() {
        this.counter = new IncrementingCounter();
        this.attributes = new ArrayList<>();
    }

    public void addAttribute(Attribute attribute) {
        this.attributes.add(attribute);
        attribute.setVariableIdentifier("variable" + counter.next());
    }
}
