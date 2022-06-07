package org.example.er2petriflow.er.converter;

import org.example.er2petriflow.er.throwable.UnsupportedRelationException;
import org.example.er2petriflow.er.domain.Entity;
import org.example.er2petriflow.er.domain.Relation;
import org.example.er2petriflow.generated.petriflow.*;
import org.example.er2petriflow.util.IncrementingCounter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.example.er2petriflow.er.Converter.createChangeTitleActionCode;
import static org.example.er2petriflow.er.converter.PetriflowUtils.*;
import static org.example.er2petriflow.er.converter.PetriflowUtils.PROCESS_PREFIX_FIELD_ID;

public class RelationConverter {

    protected static final String ENTITY_SELECTION_PREFIX = "entity";
    protected static final String OLD_VALUE_PREFIX = "oldValue";
    protected static final String ENTITY_HTML_AREA_PREFIX = "html_";
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
            
            def query = com.netgrif.application.engine.workflow.domain.QCase.case$.processIdentifier.eq(prefixField.value + "%s").and(com.netgrif.application.engine.workflow.domain.QCase.case$.activePlaces.get("%s").eq(1));
            for (def i = 0; i < fieldIds.size(); i++) {
                if (entityIds.size() <= i) {
                    break;
                }
                
                def id = entityIds[i];
                if (id != null) {
                    query = query.and(com.netgrif.application.engine.workflow.domain.QCase.case$.dataSet.get(fieldIds[i]).value.eq(id));
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
            
            return idMap.collectEntries({ [it.key, it.value.collect({ entityMap.get(it) })] })
            }
            """, "%s", "%s", CREATED_PLACE_ID);
    protected static final String FILL_OPTIONS_ACTION_TEMPLATE = """
            optionField: f.this,
            prefix: f.%s;

            %s(optionField, prefix);
            """;

    private static final String COPY_VALUE_ACTION_TEMPLATE = """
            from: f.%s,
            to: f.%s;

            change to value { from.value }
            """;

    private static final String VIEW_RELATION_ENTITY_ACTION_TEMPLATE = String.format("""
            prefix: f.%s,
            htmlArea: f.%s;
            
            def relatedCases = delegate."${prefix.value}%s".search(prefix, %s);
            
            def builder = new StringBuilder();
            
            relatedCases.each {
            
                builder.append(it.key.title)
                
                it.value.each {
                    builder.append(" | ")
                    builder.append(it != null ? it.title : "-")
                }
                
                builder.append("\\n")
                
            }
            
            
            change htmlArea value {builder.toString()}
            """, PROCESS_PREFIX_FIELD_ID, "%s", "%s", "%s");

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
        if (relation.getConnections().size() == 1) {
            throw new UnsupportedRelationException("Unary relations are not supported!", relation);
        }

        char suffix = 'A';
        for (EntityContext context : entities) {
            Data selector = createForRelation(context.getEntity(), ENTITY_SELECTION_PREFIX + suffix);
            context.setSelectorField(selector);
            result.getData().add(selector);
            suffix++;
        }
    }

    protected Data createForRelation(Entity entity, String fieldId) {
        return createDataVariable(
                fieldId,
                entity.getName(),
                DataType.ENUMERATION_MAP
        );
    }

    protected void updateEntityWorkflows() {
        for (int i = 0; i < entities.size(); i++) {
            var context = entities.get(i);
            updateEntityProcess(context, i);
            context.getEntity().incrementProcessedRelations();
        }
    }

    protected void updateEntityProcess (EntityContext context, int i) {
        Data htmlArea = addDataVariableToEntity(ENTITY_HTML_AREA_PREFIX + relation.getProcessIdentifier(), DataType.TEXT, context.getEntity(), "htmltextarea");

        addDataEventAction(htmlArea, DataEventType.GET, EventPhaseType.PRE, String.format(VIEW_RELATION_ENTITY_ACTION_TEMPLATE, htmlArea.getId(), relation.getProcessIdentifier(), ("null, ".repeat(i) + "useCase.stringId")));

        var petriflow = context.getEntity().getPetriflow();
        var place = petriflow.getPlace().stream().filter(p -> p.getId().equals(CREATED_PLACE_ID)).findFirst().orElseThrow(() -> new IllegalStateException(String.format("Entity net does not have a place with id '%s'", CREATED_PLACE_ID)));
        Transition t = crateTransition(VIEW_RELATION_TRANSITION_PREFIX + relation.getProcessIdentifier(), "View " + relation.getName(), 6, 6 + context.getEntity().getProcessedRelations() * 2, petriflow);

        referenceDataOnTransitions(htmlArea.getId(), Behavior.VISIBLE, t);

        petriflow.getTransition().add(t);

        addArc(petriflow, place, t, ArcType.READ);
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
    protected Data addDataVariableToEntity(String proposedId, DataType type, Entity entity, String component) {
        var variable = addDataVariableToEntity(proposedId, type, entity);

        var c = new Component();
        c.setName(component);
        variable.setComponent(c);

        return variable;
    }

    protected void createRelationWorkflow() {
        CrudNet crudNet = createCrudNet(result, "relation");

        addDataGroup(crudNet.getLayout(), convertRelationAttributes());

        List<String> selectorFieldIds = new ArrayList<>();
        List<String> selectorFieldIdsWrapped = new ArrayList<>();
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
            selectorFieldIdsWrapped.add(String.format("\"%s\"", context.getSelectorField().getId()));

            suffix++;
        }
        result.getFunction().add(createFunction(Scope.NAMESPACE, "search", String.format(SEARCH_RELATION_FUNCTION_TEMPLATE, String.join(", ", selectorFieldIdsWrapped), result.getId())));

        String changeTitleActionCode = createChangeTitleActionCode(selectorFieldIds, java.util.function.Function.identity(), "%1$s.options.get(%1$s.value).defaultValue");

        // Actions
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

            addEventActionToTransitions(EventType.FINISH, EventPhaseType.POST, changeTitleActionCode, crudNet.getCreate(), crudNet.getUpdate());
        }
    }

    protected List<Data> convertRelationAttributes() {
        return relation.getAttributes().stream().map(PetriflowUtils::convertAttribute).peek(d -> result.getData().add(d)).collect(Collectors.toList());
    }
}
