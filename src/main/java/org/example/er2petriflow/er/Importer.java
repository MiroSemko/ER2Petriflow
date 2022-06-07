package org.example.er2petriflow.er;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.er2petriflow.er.domain.*;
import org.example.er2petriflow.er.json.Connector;
import org.example.er2petriflow.er.json.Details;
import org.example.er2petriflow.er.json.Diagram;
import org.example.er2petriflow.er.json.Shape;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class Importer {

    protected static final String SHAPE_TYPE_ENTITY = "Entity";
    protected static final String SHAPE_TYPE_ATTRIBUTE = "Attribute";
    protected static final String SHAPE_TYPE_RELATION = "Relationship";

    private Diagram imported;
    private ERDiagram result;

    private Map<Integer, Shape> shapeMap;
    private List<Details> entities;
    private List<Details> relations;
    private Map<Integer, List<Connector>> connectorMap;
    private Map<Integer, Entity> entityMap;

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
        mapConnectors();
        result = new ERDiagram();
        parseEntities();
        parseRelations();
        return Optional.of(result);
    }

    protected void mapShapes() {
        shapeMap = new HashMap<>();
        entityMap = new HashMap<>();
        entities = new ArrayList<>();
        relations = new ArrayList<>();

        for (Shape s : imported.getShapes()) {
            shapeMap.put(s.getDetails().getId(), s);
            parseShape(s);
        }
    }

    protected void parseShape(Shape shape) {
        switch (shape.getType()) {
            case SHAPE_TYPE_ENTITY:
                entities.add(shape.getDetails());
                break;
            case SHAPE_TYPE_RELATION:
                relations.add(shape.getDetails());
                break;
        }
    }

    protected void mapConnectors() {
        connectorMap = new HashMap<>();

        for (Connector c : imported.getConnectors()) {
            addConnectorToMap(c.getSource(), c);
            if (!c.getSource().equals(c.getDestination())) {
                addConnectorToMap(c.getDestination(), new Connector(c.getType(), c.getDestination(), c.getSource()));
            }
        }
    }

    private void addConnectorToMap(Integer key, Connector value) {
        if (!connectorMap.containsKey(key)) {
            connectorMap.put(key, new ArrayList<>());
        }
        connectorMap.get(key).add(value);
    }

    protected void parseEntities() {
        for (Details entity : entities) {
            Entity e = new Entity(entity.getName());
            parseAttributes(entity, e);
            result.addEntity(e);
            entityMap.put(entity.getId(), e);
        }
    }

    protected void parseAttributes(Details entity, Entity result) {
        if (!connectorMap.containsKey(entity.getId())) {
            return;
        }

        Iterator<Connector> iterator = connectorMap.get(entity.getId()).iterator();
        while (iterator.hasNext()) {
            Shape destination = resolveDestinationShape(iterator.next());
            if (!destination.getType().equals(SHAPE_TYPE_ATTRIBUTE)) {
                continue;
            }
            result.addAttribute(parseAttribute(destination));
            iterator.remove();
        }
    }

    protected Shape resolveDestinationShape(Connector c) {
        return shapeMap.get(c.getDestination());
    }

    protected Attribute parseAttribute(Shape attribute) {
        String[] split = attribute.getDetails().getName().split(":", 2);
        return new Attribute(split[0].trim(), AttributeType.resolve(split[1].trim()), attribute.getDetails().getIsUnique());
    }

    protected void parseRelations() {
        for (Details relation: relations) {
            Relation r = new Relation(relation.getName());
            for (Connector c : connectorMap.get(relation.getId())) {
                parseRelationConnection(r,resolveDestinationShape(c));
            }
            result.addRelation(r);
        }
    }

    protected void parseRelationConnection(Relation relation, Shape connection) {
        switch (connection.getType()) {
            case SHAPE_TYPE_ENTITY -> relation.addEntity(entityMap.get(connection.getDetails().getId()));
            case SHAPE_TYPE_ATTRIBUTE -> relation.addAttribute(parseAttribute(connection));
        }
    }

}
