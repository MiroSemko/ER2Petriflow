package org.example.er2petriflow.er.converter;

import org.example.er2petriflow.er.UnsupportedRelationException;
import org.example.er2petriflow.er.domain.Entity;
import org.example.er2petriflow.er.domain.Relation;
import org.example.er2petriflow.generated.petriflow.*;
import org.example.er2petriflow.util.IncrementingCounter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.example.er2petriflow.er.converter.PetriflowUtils.*;
import static org.example.er2petriflow.er.converter.PetriflowUtils.PROCESS_PREFIX_FIELD_ID;

public class RelationConverter {

    protected static final String ENTITY_SELECTION_PREFIX = "entity";
    protected static final String OLD_VALUE_PREFIX = "oldValue";
    protected static final String CASE_REF_PREFIX = "relation_";
    protected static final String TASK_REF_PREFIX = "viewRelation_";
    protected static final String SET_RELATION_TRANSITION_PREFIX = "update_";
    protected static final String VIEW_RELATION_TRANSITION_PREFIX = "read_";

    protected static final String FILL_OPTIONS_FUNCTION_TEMPLATE = String.format("""
            { optionField, prefixField ->
                def cases = findCases({ it.processIdentifier.eq(prefixField.value + "%s").and(it.activePlaces.get("%s").eq(1)) });
                change optionField options { cases.collectEntries({[it.stringId, it.title]}) }
            }
            """, "%s", CREATED_PLACE_ID);

    protected static final String SEARCH_RELATION_FUNCTION_TEMPLATE = String.format("""
            { prefixField, String... entityIds ->
            
            def fieldIds = [%s];
            def missingFieldIds = new HashSet(fieldIds);
            
            def query = Qcase.case$.processIdentifier.eq(prefixField.value + "%s").and(QCase.case$.activePlaces.get("%s").eq(1));
            for (def i = 0; i < fieldIds.length; i++) {
                if (entityIds.length >= i) {
                    break;
                }
                
                def id = entityIds[i];
                if (id != null) {
                    query = query.and(QCase.case$.dataSet.get(fieldIds[i]).value.eq(id));
                    missingFieldIds.remove(fieldIds[i]);
                }
            }
            
            def relationCases = findCases({ query });
            def idMap = new LinkedHashMap();
            def missingEntityIds = new HashSet();
            
            for (def relationCase : relationCases) {
                def entry = [];
                for (def fieldId : fieldIds) {
                    if (!missingFieldIds.contains(fieldId)) {
                        entry.add(null);
                        continue;
                    }
                    def entityId = relationCase.dataSet.get(fieldId).value;
                    entry.add(entityId);
                    missingEntityIds.add(entityId);
                }
                idMap.put(relationCase, entry);
            }
            
            def entityCases = findCases({ it._id.in(missingEntityIds) })
            
            def entityMap = entityCases.collectEntries( {[it.stringId, it]} )
            
            return idMap.collectEntries({ [it.ketKey(), it.getValue().collect({ entityMap.get(it) })] })
            }
            """, "%s", "%s", CREATED_PLACE_ID);
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

    private static final String CHANGE_ENTITY_TASKREF_ACTION_TEMPLATE = String.format("""
            caseRef: f.%s,
            taskRef: f.%s;
                        
            def viewTasks = findTasks({it.caseId.in(caseRef.value) & it.transitionId.eq("%s")})
                        
            change taskRef value { viewTasks.collect({ it.stringId }) }
            """, "%s", "%s", READ_TRANSITION_ID);

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
        for (var context : entities) {
            addRelationFieldsToEntity(context);
            addRelationTransitionsToEntity(context);
            context.getEntity().incrementProcessedRelations();
        }
    }

    protected void addRelationFieldsToEntity(EntityContext context) {
        // TODO should be a case ref field, but demo.netgrif.com does not support case refs correctly
        var caseRef = addDataVariableToEntity(CASE_REF_PREFIX + relation.getProcessIdentifier(), DataType.TASK_REF, context.getEntity());
        context.setCaseRefFieldId(caseRef.getId());
        context.setTaskRefFieldId(
                addDataVariableToEntity(TASK_REF_PREFIX + relation.getProcessIdentifier(), DataType.TASK_REF, context.getEntity()).getId()
        );

        addDataEventAction(caseRef, DataEventType.SET, EventPhaseType.POST, String.format(CHANGE_ENTITY_TASKREF_ACTION_TEMPLATE, caseRef.getId(), context.getTaskRefFieldId()));
    }

    protected void addRelationTransitionsToEntity(EntityContext context) {
        var petriflow = context.getEntity().getPetriflow();
        var place = petriflow.getPlace().stream().filter(p -> p.getId().equals(CREATED_PLACE_ID)).findFirst().orElseThrow(() -> new IllegalStateException(String.format("Entity net does not have a place with id '%s'", CREATED_PLACE_ID)));
        Transition t1 = crateTransition(SET_RELATION_TRANSITION_PREFIX + relation.getProcessIdentifier(), 4, 6 + context.getEntity().getProcessedRelations() * 2, petriflow);
        context.setCaseRefTransitionId(t1.getId());
        Transition t2 = crateTransition(VIEW_RELATION_TRANSITION_PREFIX + relation.getProcessIdentifier(), "View " + relation.getName(), 6, 6 + context.getEntity().getProcessedRelations() * 2, petriflow);

        addSystemRolePerform(t1);
        referenceDataOnTransitions(context.getCaseRefFieldId(), Behavior.EDITABLE, t1);

        referenceDataOnTransitions(context.getTaskRefFieldId(), Behavior.VISIBLE, t2);

        petriflow.getTransition().add(t1);
        petriflow.getTransition().add(t2);

        addArc(petriflow, place, t1, ArcType.READ);
        addArc(petriflow, place, t2, ArcType.READ);
    }

    protected Data addDataVariableToEntity(String proposedId, DataType type, Entity entity) {
        String finalId = proposedId;
        var petriflow = entity.getPetriflow();
        IncrementingCounter counter = new IncrementingCounter();

        var existingIds = petriflow.getData().stream().map(Data::getId).collect(Collectors.toSet());
        while (existingIds.contains(finalId)) {
            finalId = proposedId + counter.next();
        }

        Data variable = createDataVariable(finalId, type);
        petriflow.getData().add(variable);

        return variable;
    }

    protected void createRelationWorkflow() {
        CrudNet crudNet = createCrudNet(result, "relation");

        List<String> selectorFieldIds = new ArrayList<>();
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

            selectorFieldIds.add(context.getSelectorField().getId());

            suffix++;
        }
        result.getFunction().add(createFunction(Scope.NAMESPACE, "search", String.format(SEARCH_RELATION_FUNCTION_TEMPLATE, String.join(", ", selectorFieldIds), result.getId())));

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
