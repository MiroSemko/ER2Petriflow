package org.example.er2petriflow;

import org.example.er2petriflow.er.Importer;
import org.example.er2petriflow.er.domain.ERDiagram;

import java.util.Optional;

public class ER2PetriflowCLI {

    public static void main(String[] args) {
        Importer importer = new Importer();
        Optional<ERDiagram> diagram = importer.importDiagram(ER2PetriflowCLI.class.getResourceAsStream("/test_graph.graphml"));
        assert diagram.isPresent();
    }

}
