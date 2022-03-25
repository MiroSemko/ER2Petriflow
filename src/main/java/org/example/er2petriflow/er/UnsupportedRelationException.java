package org.example.er2petriflow.er;

import lombok.Getter;
import org.example.er2petriflow.er.domain.Relation;

public class UnsupportedRelationException extends IllegalArgumentException {

    @Getter
    private final Relation relation;

    public UnsupportedRelationException(String s, Relation relation) {
        super(s);
        this.relation = relation;
    }
}
