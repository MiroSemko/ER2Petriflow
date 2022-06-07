package org.example.er2petriflow.er;

import org.example.er2petriflow.er.converter.AttributeContext;
import org.example.er2petriflow.er.converter.PetriflowUtils;
import org.example.er2petriflow.er.converter.RelationConverter;
import org.example.er2petriflow.er.domain.Attribute;
import org.example.er2petriflow.er.domain.ERDiagram;
import org.example.er2petriflow.er.domain.Entity;
import org.example.er2petriflow.er.domain.Relation;
import org.example.er2petriflow.generated.petriflow.Data;
import org.example.er2petriflow.generated.petriflow.DataEventType;
import org.example.er2petriflow.generated.petriflow.Document;
import org.example.er2petriflow.generated.petriflow.EventPhaseType;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.example.er2petriflow.er.converter.PetriflowUtils.*;

public class Converter {

    protected static final String CHANGE_CASE_TITLE_ACTION_TEMPLATE = """
            %s;
            
            changeCaseProperty "title" about { %s }
            """;

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
        var titleAttributes = new ArrayList<Data>();
        var allAttributes = entity.getAttributes().stream()
                .map(this::convertAttribute)
                .peek(context -> {
                    if (context.getAttribute().isTitlePart()) {
                        titleAttributes.add(context.getVariable());
                    }
                })
                .map(AttributeContext::getVariable)
                .collect(Collectors.toList());

        if (titleAttributes.size() > 0) {
            String actionCode = createChangeTitleActionCode(titleAttributes, Data::getId, "%s.value");

            for (Data d : titleAttributes) {
                addDataEventAction(d, DataEventType.SET, EventPhaseType.POST, actionCode);
            }
        }

        result.getData().addAll(allAttributes);
    }

    protected AttributeContext convertAttribute(Attribute attribute) {
        return new AttributeContext(attribute, PetriflowUtils.convertAttribute(attribute));
    }

    protected void createEntityWorkflow(Document petriflow) {
        createCrudNet(petriflow, "instance");
    }

    public static <T> String createChangeTitleActionCode(List<T> individuals, Function<T, String> individualConverter, String valueTemplate) {
        String refs = individuals.stream().map(i -> String.format("%s: f.%s", individualConverter.apply(i), individualConverter.apply(i))).collect(Collectors.joining(",\n"));
        String value = individuals.stream().map(i -> String.format(valueTemplate, individualConverter.apply(i))).collect(Collectors.joining(" + \" \" + "));
        return String.format(CHANGE_CASE_TITLE_ACTION_TEMPLATE, refs, value);
    }
}