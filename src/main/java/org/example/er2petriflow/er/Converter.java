package org.example.er2petriflow.er;

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

    public List<Document> convertToPetriflows(ERDiagram diagram) {
        var result = convertEntities(diagram.getEntities());
        result.addAll(convertRelations(diagram.getRelations()));
        return result;
    }

    protected List<Document> convertEntities(Collection<Entity> entities) {
        return entities.stream().map(this::convertEntity).collect(Collectors.toList());
    }

    protected Document convertEntity(Entity entity) {
        Document result = new Document();

        setDocumentMetadata(result, "ENT", entity.getProcessIdentifier(), entity.getName());

        convertEntityAttributes(entity, result);
        createEntityWorkflow(result);

        entity.setPetriflow(result);
        return result;
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