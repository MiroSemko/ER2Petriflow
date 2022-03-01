package org.example.er2petriflow.er;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.er2petriflow.er.domain.ERDiagram;
import org.example.er2petriflow.er.domain.Entity;
import org.example.er2petriflow.er.json.Details;
import org.example.er2petriflow.er.json.Diagram;
import org.example.er2petriflow.er.json.Shape;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class Importer {

    private Diagram imported;
    private ERDiagram result;

    private Map<Integer, Shape> shapeMap;
    private List<Details> entities;
    private List<Details> attributes;

    public Optional<ERDiagram> importDiagram(InputStream jsonFile) {
        try {
            Diagram imported = unmarshall(jsonFile);
            return convert(imported);
        } catch (IOException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    protected Diagram unmarshall(InputStream jsonFile) throws IOException {
        return new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).readValue(jsonFile, Diagram.class);
    }

    protected Optional<ERDiagram> convert(Diagram imported) {
        this.imported = imported;
        mapShapes();
        result = new ERDiagram();
        parseEntities();
        return Optional.of(result);
    }

    protected void mapShapes() {
        shapeMap = new HashMap<>();
        entities = new ArrayList<>();
        attributes = new ArrayList<>();

        for (Shape s : imported.getShapes()) {
            shapeMap.put(s.getDetails().getId(), s);
            parseShape(s);
        }
    }

    protected void parseShape(Shape shape) {
        switch (shape.getType()) {
            case "Entity":
                entities.add(shape.getDetails());
            case "Attribute":
                attributes.add(shape.getDetails());
        }
    }

    protected void parseEntities() {
        for (Details entity: entities) {
            Entity e = new Entity(entity.getName());
            result.addEntity(e);
        }
    }

}
