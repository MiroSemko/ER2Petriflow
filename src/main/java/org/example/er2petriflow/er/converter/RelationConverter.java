package org.example.er2petriflow.er.converter;

import org.example.er2petriflow.er.UnsupportedRelationException;
import org.example.er2petriflow.er.domain.Entity;
import org.example.er2petriflow.er.domain.Relation;
import org.example.er2petriflow.generated.petriflow.*;
import org.example.er2petriflow.util.IncrementingCounter;

import java.util.List;
import java.util.stream.Collectors;

import static org.example.er2petriflow.er.converter.PetriflowUtils.*;
import static org.example.er2petriflow.er.converter.PetriflowUtils.PROCESS_PREFIX_FIELD_ID;

public class RelationConverter {

    protected static final String ENTITY_SELECTION_PREFIX = "entity";
    protected static final String OLD_VALUE_PREFIX = "oldValue";
    protected static final String CASE_REF_PREFIX = "relation_";
    protected static final String TASK_REF_PREFIX = "viewRelation_";

    protected static final String FILL_OPTIONS_FUNCTION_TEMPLATE = """
            { optionField, prefixField ->
                def cases = findCases({ it.processIdentifier.eq(prefixField.value + "%s") });
                change optionField options { cases.collectEntries({[it.stringId, it.title]}) }
            }
            """;

    protected static final String FILL_OPTIONS_ACTION_TEMPLATE = """
            optionField: f.this,
            prefix: f.%s;

            %s(optionField, prefix);
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

    private static final String COPY_VALUE_ACTION_TEMPLATE = """
            from: f.%s,
            to: f.%s;

            change to value { from.value }
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
        updateEntityWorkflows();
        createRelationWorkflow();

        return result;
    }

    protected void createRelationAttributes() {
        if (relation.getConnections().size() != 2) {
            throw new UnsupportedRelationException("Only binary relationships are currently supported!", relation);
        }

        char suffix = 'A';
        for (EntityContext context : entities) {
            Data selector = createForRelation(context.getEntity(), ENTITY_SELECTION_PREFIX + suffix);
            context.setSelectorField(selector);
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

    protected void updateEntityWorkflows() {
        for (var context: entities) {
            addRelationFieldsToEntity(context);
        }
    }

    protected void addRelationFieldsToEntity(EntityContext context) {
        context.setCaseRefFieldId(
                // TODO should be a case ref field, but demo.netgrif.com does not support case refs correctly
                addDataVariableToEntity(CASE_REF_PREFIX + relation.getProcessIdentifier(), DataType.TASK_REF, context.getEntity())
        );
        context.setTaskRefFieldId(
                addDataVariableToEntity(TASK_REF_PREFIX + relation.getProcessIdentifier(), DataType.TASK_REF, context.getEntity())
        );
    }

    protected String addDataVariableToEntity(String proposedId, DataType type, Entity entity) {
        String finalId = proposedId;
        var petriflow = entity.getPetriflow();
        IncrementingCounter counter = new IncrementingCounter();

        var existingIds = petriflow.getData().stream().map(Data::getId).collect(Collectors.toSet());
        while (existingIds.contains(finalId)) {
            finalId = proposedId + counter.next();
        }

        Data variable = createDataVariable(finalId, type);
        petriflow.getData().add(variable);

        return finalId;
    }
    protected void createRelationWorkflow() {
        CrudNet crudNet = createCrudNet(result, "relation");

        char suffix = 'A';
        for (EntityContext context : entities) {
            // Functions
            Function f = createFunction("fill" + suffix, String.format(FILL_OPTIONS_FUNCTION_TEMPLATE, context.getEntity().getProcessIdentifier()));
            context.setFillFunction(f);
            result.getFunction().add(f);

            // Non-referenced data
            Data old = createForRelation(context.getEntity(), OLD_VALUE_PREFIX + suffix);
            context.setOldValueField(old);
            result.getData().add(old);

            suffix++;
        }

        // Actions
        // prefix
        addCreateCaseAction(result, String.format(RESOLVE_PREFIX_ACTION_TEMPLATE, PROCESS_PREFIX_FIELD_ID));

        // selector actions
        for (EntityContext context : entities) {
            addDataEventAction(context.getSelectorField(), DataEventType.GET, EventPhaseType.POST, String.format(
                    FILL_OPTIONS_ACTION_TEMPLATE,
                    PROCESS_PREFIX_FIELD_ID,
                    context.getFillFunction().getName()
            ));

            addEventActionToTransitions(EventType.FINISH, EventPhaseType.POST, String.format(
                    COPY_VALUE_ACTION_TEMPLATE,
                    context.getSelectorField().getId(),
                    context.getOldValueField().getId()
            ), crudNet.getCreate(), crudNet.getUpdate());
            addEventActionToTransitions(EventType.CANCEL, EventPhaseType.POST, String.format(
                    COPY_VALUE_ACTION_TEMPLATE,
                    context.getOldValueField().getId(),
                    context.getSelectorField().getId()
            ), crudNet.getCreate(), crudNet.getUpdate());
        }
    }
}
