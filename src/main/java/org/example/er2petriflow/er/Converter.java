package org.example.er2petriflow.er;

import org.example.er2petriflow.er.converter.CrudNet;
import org.example.er2petriflow.er.converter.RelationConverter;
import org.example.er2petriflow.er.domain.Attribute;
import org.example.er2petriflow.er.domain.ERDiagram;
import org.example.er2petriflow.er.domain.Entity;
import org.example.er2petriflow.er.domain.Relation;
import org.example.er2petriflow.generated.petriflow.*;

import java.util.*;
import java.util.stream.Collectors;

import static org.example.er2petriflow.er.converter.PetriflowUtils.*;

public class Converter {

    protected Map<String, Document> entityMap;

    public List<Document> convertToPetriflows(ERDiagram diagram) {
        entityMap = new HashMap<>();
        Map<Long, Document> entities = convertEntities(diagram.getEntities());
        var result = convertRelations(diagram.getRelations());
        result.addAll(entities.values());
        return result;
    }

    protected Map<Long, Document> convertEntities(Collection<Entity> entities) {
        return entities.stream().map(this::convertEntity).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    protected AbstractMap.Entry<Long, Document> convertEntity(Entity entity) {
        Document result = new Document();

        setDocumentMetadata(result, "ENT", entity.getProcessIdentifier(), entity.getName());

        convertEntityAttributes(entity, result);
        createEntityWorkflow(result);

        entityMap.put(entity.getProcessIdentifier(), result);

        return new AbstractMap.SimpleEntry<>(entity.getId(), result);
    }

    protected List<Document> convertRelations(Collection<Relation> relations) {
        return relations.stream().map(this::convertRelation).collect(Collectors.toList());
    }

    protected Document convertRelation(Relation relation) {
        var converter = new RelationConverter(relation);
        return converter.convert();
    }

    protected void convertEntityAttributes(Entity entity, Document result) {
        result.getData().addAll(
                entity.getAttributes().stream().map(this::convertAttribute).collect(Collectors.toList())
        );
    }

    protected Data convertAttribute(Attribute attribute) {
        return createDataVariable(
                attribute.getVariableIdentifier(),
                attribute.getName(),
                attribute.getType().getMapping()
        );
    }

    protected void createEntityWorkflow(Document petriflow) {
        createCrudNet(petriflow, "instance");
    }
}