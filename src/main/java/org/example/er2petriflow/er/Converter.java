package org.example.er2petriflow.er;

import org.example.er2petriflow.er.domain.ERDiagram;
import org.example.er2petriflow.er.domain.Entity;
import org.example.er2petriflow.generated.petriflow.Document;

import java.util.Collections;
import java.util.List;

public class Converter {

    public List<Document> convertToPetriflows(ERDiagram diagram) {
        List<Document> result = convertEntities(diagram.getEntities());
        return result;
    }

    protected List<Document> convertEntities(List<Entity> entities) {
        return Collections.emptyList();
    }
}
