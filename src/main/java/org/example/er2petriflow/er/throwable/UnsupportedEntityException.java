package org.example.er2petriflow.er.throwable;

import lombok.Getter;
import org.example.er2petriflow.er.json.Shape;

public class UnsupportedEntityException extends IllegalArgumentException {

    @Getter
    private final Shape entity;

    public UnsupportedEntityException(String s, Shape entity) {
        super(s);
        this.entity = entity;
    }
}
