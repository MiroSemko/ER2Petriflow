package org.example.er2petriflow.er.json;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class Diagram {

    private List<Shape> shapes;
    private List<Connector> connectors;

}
