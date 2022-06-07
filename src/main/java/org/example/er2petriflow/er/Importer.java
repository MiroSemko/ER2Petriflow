package org.example.er2petriflow.er;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.er2petriflow.er.domain.*;
import org.example.er2petriflow.er.json.Connector;
import org.example.er2petriflow.er.json.Details;
import org.example.er2petriflow.er.json.Diagram;
import org.example.er2petriflow.er.json.Shape;
import org.example.er2petriflow.er.throwable.UnsupportedConnectorException;
import org.example.er2petriflow.er.throwable.UnsupportedEntityException;
import org.example.er2petriflow.util.MapUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class Importer {

    protected static final String SHAPE_TYPE_ENTITY = "Entity";
    protected static final String SHAPE_TYPE_ATTRIBUTE = "Attribute";
    protected static final String SHAPE_TYPE_RELATION = "Relationship";

    protected static final String ENTITY_TYPE_NARY_RELATION = "associative";

    private Diagram imported;
    private ERDiagram result;

    private Map<Integer, Shape> shapeMap;
    private List<Details> entities;
    private Map<Integer, Details> relations;
    private Map<Integer, Set<Integer>> connectionMap;
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
        parseEntityNodes();
        parseRelationNodes();
        return Optional.of(result);
    }

    protected void mapShapes() {
        shapeMap = new HashMap<>();
        entityMap = new HashMap<>();
        entities = new ArrayList<>();
        relations = new LinkedHashMap<>();

        for (Shape s : imported.getShapes()) {
            shapeMap.put(s.getDetails().getId(), s);
            parseShape(s);
        }
    }

    protected void parseShape(Shape shape) {
        switch (shape.getType()) {
            case SHAPE_TYPE_ENTITY -> entities.add(shape.getDetails());
            case SHAPE_TYPE_RELATION -> relations.put(shape.getDetails().getId(), shape.getDetails());
        }
    }

    protected void mapConnectors() {
        connectionMap = new HashMap<>();

        for (Connector c : imported.getConnectors()) {
            if (c.getSource().equals(c.getDestination())) {
                throw new UnsupportedConnectorException("Reflexive connections are not supported!", c);
            }
            addConnectionToMap(c.getSource(), c.getDestination());
            addConnectionToMap(c.getDestination(), c.getSource());
        }
    }

    private void addConnectionToMap(Integer key, Integer destination) {
        if (!connectionMap.containsKey(key)) {
            connectionMap.put(key, new LinkedHashSet<>());
        }
        connectionMap.get(key).add(destination);
    }

    protected void parseEntityNodes() {
        for (Details entity : entities) {
            if (entity.getType().equals(ENTITY_TYPE_NARY_RELATION)) {
                mergeNaryRelation(entity);
            } else {
                parseEntity(entity);
            }
        }
    }

    protected void mergeNaryRelation(Details nary) {
        var connectionsToRemove = new HashSet<Integer>();
        connectionsToRemove.add(nary.getId());
        var connectionsToAdd = new HashSet<Integer>();

        for (Integer directId: connectionMap.get(nary.getId())) {
            Shape s = shapeMap.get(directId);
            if (!s.getType().equals(SHAPE_TYPE_RELATION)) {
                continue;
            }

            connectionsToRemove.add(directId);

            var connectedIds = connectionMap.get(directId);
            connectionsToAdd.addAll(connectedIds);
            for (Integer transitiveId: connectedIds) {
                if (Objects.equals(transitiveId, nary.getId())) {
                    continue;
                }

                var transitiveShape = shapeMap.get(transitiveId);
                if (transitiveShape.getType().equals(ENTITY_TYPE_NARY_RELATION)) {
                    throw new UnsupportedEntityException("Connected associative entities are not supported!", transitiveShape);
                }

                var transitiveConnections = connectionMap.get(transitiveId);
                transitiveConnections.remove(directId);
                transitiveConnections.add(nary.getId());
            }
        }

        connectionMap.get(nary.getId()).addAll(connectionsToAdd);
        connectionMap.get(nary.getId()).removeAll(connectionsToRemove);
        connectionsToRemove.remove(nary.getId());
        MapUtils.removeAll(connectionMap, connectionsToRemove);

        MapUtils.removeAll(relations, connectionsToRemove);
        relations.put(nary.getId(), nary);
    }

    protected void parseEntity(Details entity) {
        Entity e = new Entity(entity.getName());
        parseAttributes(entity, e);
        result.addEntity(e);
        entityMap.put(entity.getId(), e);
    }

    protected void parseAttributes(Details entity, Entity result) {
        if (!connectionMap.containsKey(entity.getId())) {
            return;
        }

        Iterator<Integer> iterator = connectionMap.get(entity.getId()).iterator();
        while (iterator.hasNext()) {
            Shape destination = shapeMap.get(iterator.next());
            if (!destination.getType().equals(SHAPE_TYPE_ATTRIBUTE)) {
                continue;
            }
            result.addAttribute(parseAttribute(destination));
            iterator.remove();
        }
    }

    protected Attribute parseAttribute(Shape attribute) {
        String[] split = attribute.getDetails().getName().split(":", 2);
        return new Attribute(split[0].trim(), AttributeType.resolve(split[1].trim()), attribute.getDetails().getIsUnique());
    }

    protected void parseRelationNodes() {
        for (Details relation : relations.values()) {
            Relation r = new Relation(relation.getName());
            for (Integer dest : connectionMap.get(relation.getId())) {
                parseRelationConnection(r, shapeMap.get(dest));
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
