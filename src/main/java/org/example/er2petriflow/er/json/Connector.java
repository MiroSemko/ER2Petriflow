package org.example.er2petriflow.er.json;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Connector {

    private String type;
    private int source;
    private int destination;

}
