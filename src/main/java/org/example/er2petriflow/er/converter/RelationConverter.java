package org.example.er2petriflow.er.converter;

import org.example.er2petriflow.er.UnsupportedRelationException;
import org.example.er2petriflow.er.domain.Entity;
import org.example.er2petriflow.er.domain.Relation;
import org.example.er2petriflow.generated.petriflow.Data;
import org.example.er2petriflow.generated.petriflow.DataType;
import org.example.er2petriflow.generated.petriflow.Document;
import org.example.er2petriflow.generated.petriflow.Function;

import java.util.List;
import java.util.stream.Collectors;

import static org.example.er2petriflow.er.converter.PetriflowUtils.*;
import static org.example.er2petriflow.er.converter.PetriflowUtils.PROCESS_PREFIX_FIELD_ID;

public class RelationConverter {

    protected static final String ENTITY_SELECTION_PREFIX = "entity";

    protected static final String FILL_OPTIONS_FUNCTION_TEMPLATE = """
(optionField, prefixField) -> {
    def cases = findCases({ it.processIdentifier.eq(prefixField.value + "%s") });
    change optionField options { cases.collectEntries(([it.stringId, it.title])) }
}
""";
    protected static final String RESOLVE_PREFIX_ACTION_TEMPLATE = """
prefix: f.%s;

String[] splitProcessIdentifier = useCase.processIdentifier.split("_", 2)
if (splitProcessIdentifier.length < 2) {
    change prefix value { ""; }
    return;
}

change prefix value {
    if (splitProcessIdentifier[0].matches("[0-9a-f]{24}")) {
        return splitProcessIdentifier[0] + "_";
    }
    return "";
}
""";

    private final Relation relation;
    private final List<EntityContext> entities;
    private Document result;

    public RelationConverter(Relation relation) {
        this.relation = relation;
        this.entities = relation.getConnections().stream().map(EntityContext::new).collect(Collectors.toList());
    }

    public Document convert() {
        this.result = new Document();

        setDocumentMetadata(result, "REL", relation.getProcessIdentifier(), relation.getName());
        createRelationAttributes();
        createRelationWorkflow();

        return result;
    }

    protected void createRelationAttributes() {
        if (relation.getConnections().size() != 2) {
            throw new UnsupportedRelationException("Only binary relationships are currently supported!", relation);
        }

        char suffix = 'A';
        for (Entity entity : relation.getConnections()) {
            Data selector = createForRelation(entity, ENTITY_SELECTION_PREFIX + suffix);
            result.getData().add(selector);
            suffix++;
        }

        // for demo.netgrif.com group id resolution
        result.getData().add(createDataVariable(PROCESS_PREFIX_FIELD_ID, "", DataType.TEXT));
    }

    protected Data createForRelation(Entity entity, String fieldId) {
        return createDataVariable(
                fieldId,
                entity.getName(),
                DataType.ENUMERATION_MAP
        );
    }

    protected void createRelationWorkflow() {
        CrudNet crudNet = createCrudNet(result, "relation");

        // Functions
        Function fillA = createFunction("fillA", String.format(FILL_OPTIONS_FUNCTION_TEMPLATE, entities.get(0).getEntity().getProcessIdentifier()));
        Function fillB = createFunction("fillB", String.format(FILL_OPTIONS_FUNCTION_TEMPLATE, entities.get(1).getEntity().getProcessIdentifier()));

        result.getFunction().addAll(List.of(fillA, fillB));

        // prefix
        addCreateCaseAction(result, String.format(RESOLVE_PREFIX_ACTION_TEMPLATE, PROCESS_PREFIX_FIELD_ID));
    }
}
