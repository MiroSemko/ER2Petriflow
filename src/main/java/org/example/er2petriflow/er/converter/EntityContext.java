package org.example.er2petriflow.er.converter;

import lombok.Getter;
import lombok.Setter;
import org.example.er2petriflow.er.domain.Entity;
import org.example.er2petriflow.generated.petriflow.Data;
import org.example.er2petriflow.generated.petriflow.Function;

@Getter
public class EntityContext {

    private final Entity entity;
    @Setter
    private Data selectorField;
    @Setter
    private Data oldValueField;
    @Setter
    private Function fillFunction;
    @Setter
    private String caseRefFieldId;
    @Setter
    private String taskRefFieldId;
    @Setter
    private String caseRefTransitionId;

    public EntityContext(Entity entity) {
        this.entity = entity;
    }
}
