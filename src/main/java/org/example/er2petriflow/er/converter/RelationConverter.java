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

    protected static final String FILL_OPTIONS_FUNCTION_TEMPLATE = """
            { optionField, prefixField ->
                def cases = findCases({ it.processIdentifier.eq(prefixField.value + "%s") });
                change optionField options { cases.collectEntries({[it.stringId, it.title]}) }
            }
            """;

    protected static final String UPDATE_RELATION_FUNCTION = """
            { oldValue, newValue, updateTransId, caseRefFieldId ->
                def removed = new HashSet(oldValue);
                removed.removeAll(newValue);
                def added = new HashSet(newValue);
                added.removeAll(oldValue);
                
                def removedCases = findCases({ it._id.in(removed) });
                removedCases.each {
                    def newValue = new ArrayList(it.dataSet.get(caseRefFieldId).value);
                    newValue.remove(useCase.stringId)
                
                    def t = assignTask(updateTransId, it)
                    setData(t, [(caseRefFieldId):["value":newValue]])
                    finishTask(t)
                }
                
                def addedCases = findCases({it._id.in(added)});
                addedCases.each {
                    def newValue = new ArrayList(it.dataSet.get(caseRefFieldId).value);
                    newValue.add(useCase.stringId)
                
                    def t = assignTask(updateTransId, it)
                    setData(t, [(caseRefFieldId):["value":newValue]])
                    finishTask(t)
                }
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

    private static final String CHANGE_ENTITY_TASKREF_ACTION_TEMPLATE = String.format("""
            caseRef: f.%s,
            taskRef: f.%s;
                        
            def viewTasks = findTasks({it.caseId.in(caseRef.value) & it.transitionId.eq(%s)})
                        
            change taskRef value { viewTasks.collect({ it.stringId }) }
            """, "%s", "%s", READ_TRANSITION_ID);

    private static final String UPDATE_RELATION_ACTION_TEMPLATE = """
            oldA: f.%s,
            newA: f.%s,
            oldB: f.%s,
            newB: f.%s;
            
            updateRelation(oldA.value, newA.value, %s, %s)
            updateRelation(oldB.value, newB.value, %s, %s)
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
        result.getFunction().add(createFunction("updateRelation", UPDATE_RELATION_FUNCTION));

        // Actions
        // prefix
        addCreateCaseAction(result, String.format(RESOLVE_PREFIX_ACTION_TEMPLATE, PROCESS_PREFIX_FIELD_ID));

        List<String> fieldReferences = new ArrayList<>();
        List<String> methodArguments = new ArrayList<>();

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

            // old, new
            fieldReferences.add(context.getOldValueField().getId());
            fieldReferences.add(context.getSelectorField().getId());

            // transition, case ref
            methodArguments.add(context.getCaseRefTransitionId());
            methodArguments.add(context.getCaseRefFieldId());
        }

        fieldReferences.addAll(methodArguments);
        addEventActionToTransitions(EventType.FINISH, EventPhaseType.PRE, String.format(
                UPDATE_RELATION_ACTION_TEMPLATE, fieldReferences.toArray()
        ), crudNet.getCreate(), crudNet.getUpdate());
    }
}
