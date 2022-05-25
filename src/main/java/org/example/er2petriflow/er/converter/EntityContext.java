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
    private Data input;
    @Setter
    private Function fillFunction;

    public EntityContext(Entity entity) {
        this.entity = entity;
    }
}
