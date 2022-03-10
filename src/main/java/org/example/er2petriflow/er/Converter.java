package org.example.er2petriflow.er;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.er2petriflow.er.domain.Attribute;
import org.example.er2petriflow.er.domain.ERDiagram;
import org.example.er2petriflow.er.domain.Entity;
import org.example.er2petriflow.generated.petriflow.*;

import java.util.List;
import java.util.stream.Collectors;

public class Converter {

    public static final String SYSTEM_ROLE_ID = "system";
    public static final String SYSTEM_ROLE_TITLE = "System";

    public static final String LAYOUT_TRANSITION_ID = "layout";
    public static final String LAYOUT_TASK_REF_ID = "layoutTaskRef";

    protected static final int VERTICAL_OFFSET = 20;
    protected static final int HORIZONTAL_OFFSET = 20;
    protected static final int CELL_WIDTH = 40;
    protected static final int CELL_HEIGHT = 40;

    protected Role systemRole;
    protected int arcCounter;

    public List<Document> convertToPetriflows(ERDiagram diagram) {
        List<Document> result = convertEntities(diagram.getEntities());
        return result;
    }

    protected List<Document> convertEntities(List<Entity> entities) {
        return entities.stream().map(this::convertEntity).collect(Collectors.toList());
    }

    protected Document convertEntity(Entity entity) {
        Document result = new Document();

        result.setDefaultRole(true);
        result.setAnonymousRole(false);
        result.setVersion("1.0.0");
        result.setInitials("ENT");

        entity.setProcessIdentifier(entity.getProcessIdentifier());

        result.setId(entity.getProcessIdentifier());
        result.setTitle(i18nWithDefaultValue(entity.getName()));

        convertAttributes(entity, result);
        createSystemRole(result);
        createWorkflow(result);

        return result;
    }

    protected void convertAttributes(Entity entity, Document result) {
        result.getData().addAll(
                entity.getAttributes().stream().map(this::convertAttribute).collect(Collectors.toList())
        );
    }

    protected Data convertAttribute(Attribute attribute) {
        Data result = new Data();

        result.setId(attribute.getVariableIdentifier());
        result.setTitle(i18nWithDefaultValue(attribute.getName()));
        result.setType(attribute.getType().getMapping());

        return result;
    }

    protected void createSystemRole(Document petriflow) {
        this.systemRole = new Role();
        systemRole.setId(SYSTEM_ROLE_ID);
        systemRole.setTitle(i18nWithDefaultValue(SYSTEM_ROLE_TITLE));
        petriflow.getRole().add(systemRole);
    }

    protected void createWorkflow(Document petriflow) {
        // petri net
        Place p1 = createPlace("p1", 0, 2, 1);
        Place p2 = createPlace("p2", 4, 2, 0);
        addPlaces(petriflow, p1, p2);
        Transition t1 = createTransition("t1", "Create", 2, 2);
        Transition t2 = createTransition("t2", "Read", 6, 0);
        Transition t3 = createTransition("t3", "Update", 6, 2);
        Transition t4 = createTransition("t4", "Delete", 6, 4);
        Transition layout = createTransition(LAYOUT_TRANSITION_ID, 2, 0);
        addTransitions(petriflow, t1, t2, t3, t4, layout);
        this.arcCounter = 0;
        addArc(petriflow, p1, t1, ArcType.REGULAR);
        addArc(petriflow, t1, p2);
        addArc(petriflow, p2, t2, ArcType.READ);
        addArc(petriflow, p2, t3, ArcType.READ);
        addArc(petriflow, p2, t4, ArcType.READ);

        // roles
        RoleRef roleRef = new RoleRef();
        roleRef.setId(SYSTEM_ROLE_ID);
        Logic logic = new Logic();
        logic.setPerform(true);
        roleRef.setLogic(logic);
        layout.getRoleRef().add(roleRef);

        // Data refs
        referenceAllData(petriflow, layout);

        Data layoutTaskRef = new Data();
        layoutTaskRef.setId(LAYOUT_TASK_REF_ID);
        layoutTaskRef.setType(DataType.TASK_REF);
        Init init = new Init();
        init.setValue(LAYOUT_TRANSITION_ID);
        layoutTaskRef.setInit(init);
        petriflow.getData().add(layoutTaskRef);

        referenceDataOnTransitions(layoutTaskRef, Behavior.EDITABLE, t1, t3);
        referenceDataOnTransitions(layoutTaskRef, Behavior.VISIBLE, t2);
    }

    protected Place createPlace(String id, int x, int y, int marking) {
        Place p = new Place();
        p.setId(id);
        p.setTokens(marking);
        p.setStatic(false);
        Coordinates pos = transformCoordinates(x, y);
        p.setX(pos.getX());
        p.setY(pos.getY());
        return p;
    }

    protected void addPlaces(Document pn, Place... places) {
        pn.getPlace().addAll(List.of(places));
    }

    protected Transition createTransition(String id, int x, int y) {
        return createTransition(id, "", x, y);
    }

    protected Transition createTransition(String id, String label, int x, int y) {
        Transition t = new Transition();
        t.setId(id);
        Coordinates pos = transformCoordinates(x, y);
        t.setX(pos.getX());
        t.setY(pos.getY());
        t.setLabel(i18nWithDefaultValue(label));
        return t;
    }

    protected void addTransitions(Document pn, Transition... transitions) {
        pn.getTransition().addAll(List.of(transitions));
    }

    protected void addArc(Document pn, Place source, Transition destination, ArcType type) {
        addArc(pn, source.getId(), destination.getId(), type);
    }

    protected void addArc(Document pn, Transition source, Place destination) {
        addArc(pn, source.getId(), destination.getId(), ArcType.REGULAR);
    }

    protected void addArc(Document pn, String sourceId, String destinationId, ArcType type) {
        Arc a = new Arc();
        a.setId("a" + (this.arcCounter++));
        a.setSourceId(sourceId);
        a.setDestinationId(destinationId);
        a.setMultiplicity(1);
        a.setType(type);
        pn.getArc().add(a);
    }

    protected void referenceAllData(Document pn, Transition t) {
        DataGroup dataGroup = new DataGroup();
        dataGroup.setLayout(LayoutType.LEGACY);
        dataGroup.setId("dg");
        for (Data data : pn.getData()) {
            DataRef ref = createDataRef(data);
            dataGroup.getDataRef().add(ref);
        }
        t.getDataGroup().add(dataGroup);
    }

    protected void referenceDataOnTransitions(Data data, Behavior behavior, Transition... transitions) {
        for (Transition t : transitions) {
            DataRef dataRef = createDataRef(data, behavior);
            t.getDataRef().add(dataRef);
        }
    }

    protected DataRef createDataRef(Data data) {
        return createDataRef(data, Behavior.EDITABLE);
    }

    protected DataRef createDataRef(Data data, Behavior behavior) {
        DataRef result = new DataRef();
        result.setId(data.getId());
        Logic logic = new Logic();
        logic.getBehavior().add(behavior);
        result.setLogic(logic);
        return result;
    }

    protected static I18NStringType i18nWithDefaultValue(String defaultValue) {
        I18NStringType result = new I18NStringType();
        result.setValue(defaultValue);
        return result;
    }

    protected static Coordinates transformCoordinates(int x, int y) {
        return new Coordinates((x * CELL_WIDTH) + HORIZONTAL_OFFSET, (y * CELL_HEIGHT) + VERTICAL_OFFSET);
    }

    @Getter
    @AllArgsConstructor
    protected static class Coordinates {
        private int x;
        private int y;
    }
}