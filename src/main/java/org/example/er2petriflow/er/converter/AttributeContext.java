package org.example.er2petriflow.er.converter;

import lombok.Getter;
import org.example.er2petriflow.er.domain.Attribute;
import org.example.er2petriflow.generated.petriflow.Data;

@Getter
public class AttributeContext {

    private final Attribute attribute;
    private final Data variable;

    public AttributeContext(Attribute attribute, Data variable) {
        this.attribute = attribute;
        this.variable = variable;
    }
}
