package org.example.er2petriflow.er.domain;

import lombok.Getter;

import java.util.List;

@Getter
public class Entity {

    private String name;
    private List<Attribute> attributes;

}
