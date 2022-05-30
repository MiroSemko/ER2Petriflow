package org.example.er2petriflow.er.converter;

import org.example.er2petriflow.generated.petriflow.*;
import org.example.er2petriflow.util.IncrementingCounter;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public abstract class PetriflowUtils {

    public static final String SYSTEM_ROLE_ID = "system";
    public static final String SYSTEM_ROLE_TITLE = "System";

    public static final String DELETE_TRANSITION_ID = "delete";
    public static final String LAYOUT_TRANSITION_ID = "layout";
    public static final String READ_TRANSITION_ID = "read";
    public static final String CREATED_PLACE_ID = "created";

    public static final String LAYOUT_TASK_REF_ID = "layoutTaskRef";

    public static final String PROCESS_PREFIX_FIELD_ID = "prefix";

    public static final String DELETE_ASSIGN_EVENT_LABEL = "Delete Instance";
    public static final String DELETE_FINISH_EVENT_LABEL = "Confirm Deletion";

    protected static final int VERTICAL_OFFSET = 20;
    protected static final int HORIZONTAL_OFFSET = 20;
    protected static final int CELL_WIDTH = 40;
    protected static final int CELL_HEIGHT = 40;


    public static void setDocumentMetadata(Document net, String initials, String identifier, String title) {
        net.setDefaultRole(true);
        net.setAnonymousRole(false);
        net.setVersion("1.0.0");
        net.setInitials(initials);
        net.setId(identifier);
        net.setTitle(i18nWithDefaultValue(title));
    }

    public static CrudNet createCrudNet(Document petriflow, String suffix) {
        // Petri net
        Place p1 = createPlace("p1", 0, 2, 1);
        Place p2 = createPlace(CREATED_PLACE_ID, 4, 2, 0);
        addPlaces(petriflow, p1, p2);
        Transition t1 = createTransition("t1", "Create " + suffix, 2, 2);
        Transition t2 = createTransition(READ_TRANSITION_ID, "Read " + suffix, 6, 0);
        Transition t3 = createTransition("t3", "Update " + suffix, 6, 2);
        Transition t4 = createTransition(DELETE_TRANSITION_ID, "Delete " + suffix, 6, 4);
        Transition layout = createTransition(LAYOUT_TRANSITION_ID, 2, 0);
        addTransitions(petriflow, t1, t2, t3, t4, layout);

        addArc(petriflow, p1, t1, ArcType.REGULAR);
        addArc(petriflow, t1, p2);
        addArc(petriflow, p2, t2, ArcType.READ);
        addArc(petriflow, p2, t3, ArcType.READ);
        addArc(petriflow, p2, t4, ArcType.READ);

        // roles
        createSystemRole(petriflow);
        addSystemRolePerform(layout);

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
        referenceDataOnTransitions(layoutTaskRef, Behavior.VISIBLE, t2, t4);

        // Actions
        createDeleteCaseAction(t4);

        return new CrudNet(t1, t2, t3, t4, p2);
    }

    public static Data createDataVariable(String id, String title, DataType type) {
        Data result = createDataVariable(id, type);

        result.setTitle(i18nWithDefaultValue(title));

        return result;
    }

    public static Data createDataVariable(String id, DataType type) {
        Data result = new Data();

        result.setId(id);
        result.setType(type);

        return result;
    }

    public static Role createSystemRole(Document petriflow) {
        var systemRole = new Role();
        systemRole.setId(SYSTEM_ROLE_ID);
        systemRole.setTitle(i18nWithDefaultValue(SYSTEM_ROLE_TITLE));
        petriflow.getRole().add(systemRole);
        return systemRole;
    }

    public static Place createPlace(String id, int x, int y, int marking) {
        Place p = new Place();
        p.setId(id);
        p.setTokens(marking);
        p.setStatic(false);
        Coordinates pos = transformCoordinates(x, y);
        p.setX(pos.getX());
        p.setY(pos.getY());
        return p;
    }

    public static void addPlaces(Document pn, Place... places) {
        pn.getPlace().addAll(List.of(places));
    }

    public static Transition crateTransition(String suggestedId, int x, int y, Document petriflow) {
        return crateTransition(suggestedId, "", x, y, petriflow);
    }

    public static Transition crateTransition(String suggestedId, String label, int x, int y, Document petriflow) {
        String finalId = suggestedId;

        var counter = new IncrementingCounter();
        var existingIds = petriflow.getTransition().stream().map(Transition::getId).collect(Collectors.toSet());
        while(existingIds.contains(finalId)) {
            finalId = suggestedId + counter.next();
        }

        return createTransition(finalId, label, x, y);
    }

    public static Transition createTransition(String id, int x, int y) {
        return createTransition(id, "", x, y);
    }

    public static Transition createTransition(String id, String label, int x, int y) {
        Transition t = new Transition();
        t.setId(id);
        Coordinates pos = transformCoordinates(x, y);
        t.setX(pos.getX());
        t.setY(pos.getY());
        t.setLabel(i18nWithDefaultValue(label));
        return t;
    }

    public static void addTransitions(Document pn, Transition... transitions) {
        pn.getTransition().addAll(List.of(transitions));
    }

    public static void addArc(Document pn, Place source, Transition destination, ArcType type) {
        addArc(pn, source.getId(), destination.getId(), type);
    }

    public static void addArc(Document pn, Transition source, Place destination) {
        addArc(pn, source.getId(), destination.getId(), ArcType.REGULAR);
    }

    public static void addArc(Document pn, String sourceId, String destinationId, ArcType type) {
        Arc a = new Arc();
        a.setId("a" + (pn.getArc().size() + 1));
        a.setSourceId(sourceId);
        a.setDestinationId(destinationId);
        a.setMultiplicity(1);
        a.setType(type);
        pn.getArc().add(a);
    }

    public static void referenceAllData(Document pn, Transition t) {
        DataGroup dataGroup = createDataGroup();
        for (Data data : pn.getData().stream().filter(d -> !d.getId().equals(PROCESS_PREFIX_FIELD_ID)).collect(Collectors.toList())) {
            DataRef ref = createDataRef(data);
            dataGroup.getDataRef().add(ref);
        }
        t.getDataGroup().add(dataGroup);
    }

    public static void referenceDataOnTransitions(Data data, Behavior behavior, Transition... transitions) {
        referenceDataOnTransitions(data.getId(), behavior, transitions);
    }

    public static void referenceDataOnTransitions(String datafieldId, Behavior behavior, Transition... transitions) {
        for (Transition t : transitions) {
            DataGroup dataGroup = createDataGroup();
            DataRef dataRef = createDataRef(datafieldId, behavior);
            dataGroup.getDataRef().add(dataRef);
            t.getDataGroup().add(dataGroup);
        }
    }

    public static DataGroup createDataGroup() {
        DataGroup dataGroup = new DataGroup();
        dataGroup.setLayout(LayoutType.LEGACY);
        dataGroup.setId("dg");
        return dataGroup;
    }

    public static DataRef createDataRef(Data data) {
        return createDataRef(data, Behavior.EDITABLE);
    }

    public static DataRef createDataRef(Data data, Behavior behavior) {
        return createDataRef(data.getId(), behavior);
    }

    public static DataRef createDataRef(String datafieldId, Behavior behavior) {
        DataRef result = new DataRef();
        result.setId(datafieldId);
        Logic logic = new Logic();
        logic.getBehavior().add(behavior);
        result.setLogic(logic);
        return result;
    }

    public static void createDeleteCaseAction(Transition deleteTransition) {
        Event assign = createTransitionEvent(EventType.ASSIGN, DELETE_ASSIGN_EVENT_LABEL);
        deleteTransition.getEvent().add(assign);

        addTransitionEventAction(deleteTransition, EventType.FINISH, DELETE_FINISH_EVENT_LABEL, EventPhaseType.POST, "async.run { workflowService.deleteCase(useCase.stringId) }");
    }


    public static void addCreateCaseAction(Document petriflow, String actionCode) {
        CaseEvent create;
        boolean isNew = false;
        if (petriflow.getCaseEvents() != null && petriflow.getCaseEvents().getEvent() != null && petriflow.getCaseEvents().getEvent().stream().anyMatch(e -> e.getType().equals(CaseEventType.CREATE))) {
            create = petriflow.getCaseEvents().getEvent().stream().filter(e -> e.getType().equals(CaseEventType.CREATE)).findFirst().get();
        } else {
            create = createCaseEvent(CaseEventType.CREATE);
            isNew = true;
        }

        Action action = createAction(actionCode);

        addActions(create, EventPhaseType.POST, action);

        if (isNew) {
            if (petriflow.getCaseEvents() == null) {
                var caseEvents = new CaseEvents();
                petriflow.setCaseEvents(caseEvents);
            }
            petriflow.getCaseEvents().getEvent().add(create);
        }
    }

    public static void addDataEventAction(Data variable, DataEventType event, EventPhaseType phase, String actionCode) {
        DataEvent eventObj;
        boolean isNew = false;
        Optional<DataEvent> first = variable.getEvent().stream().filter(e -> e.getType().equals(event)).findFirst();
        if (first.isPresent()) {
            eventObj = first.get();
        } else {
            eventObj = createDataEvent(event);
            isNew = true;
        }

        Action action = createAction(actionCode);
        addActions(eventObj, phase, action);

        if (isNew) {
            variable.getEvent().add(eventObj);
        }
    }

    public static void addEventActionToTransitions(EventType event, EventPhaseType phase, String actionCode, Transition... transitions) {
        for (Transition t : transitions) {
            addTransitionEventAction(t, event, phase, actionCode);
        }
    }
    public static void addTransitionEventAction(Transition transition, EventType event, EventPhaseType phase, String actionCode) {
        Event eventObj;
        boolean isNew = false;
        Optional<Event> first = transition.getEvent().stream().filter(e -> e.getType().equals(event)).findFirst();
        if (first.isPresent()) {
            eventObj = first.get();
        } else {
            eventObj = createTransitionEvent(event);
            isNew = true;
        }

        Action action = createAction(actionCode);
        addActions(eventObj, phase, action);

        if (isNew) {
            transition.getEvent().add(eventObj);
        }
    }

    public static void addTransitionEventAction(Transition transition, EventType event, String eventTitle, EventPhaseType phase, String actionCode) {
        Event eventObj;
        boolean isNew = false;
        Optional<Event> first = transition.getEvent().stream().filter(e -> e.getType().equals(event)).findFirst();
        if (first.isPresent()) {
            eventObj = first.get();
            eventObj.setTitle(i18nWithDefaultValue(eventTitle));
        } else {
            eventObj = createTransitionEvent(event, eventTitle);
            isNew = true;
        }

        Action action = createAction(actionCode);
        addActions(eventObj, phase, action);

        if (isNew) {
            transition.getEvent().add(eventObj);
        }
    }

    public static Event createTransitionEvent(EventType type, String label) {
        Event event = createTransitionEvent(type);
        event.setTitle(i18nWithDefaultValue(label));
        return event;
    }

    public static Event createTransitionEvent(EventType type) {
        Event event = new Event();
        event.setType(type);
        return event;
    }

    public static void addActions(BaseEvent event, EventPhaseType phase, Action... actions) {
        Actions wrapper;
        boolean isNew = false;
        Optional<Actions> first = event.getActions().stream().filter(a -> a.getPhase().equals(phase)).findFirst();
        if (first.isPresent()) {
            wrapper = first.get();
        } else {
            wrapper = new Actions();
            wrapper.setPhase(phase);
            isNew = true;
        }

        wrapper.getAction().addAll(List.of(actions));

        if (isNew) {
            event.getActions().add(wrapper);
        }
    }

    public static void addSystemRolePerform(Transition t) {
        RoleRef roleRef = new RoleRef();
        roleRef.setId(SYSTEM_ROLE_ID);
        Logic logic = new Logic();
        logic.setPerform(true);
        roleRef.setLogic(logic);
        t.getRoleRef().add(roleRef);
    }

    public static CaseEvent createCaseEvent(CaseEventType type) {
        CaseEvent event = new CaseEvent();
        event.setType(type);
        return event;
    }

    public static DataEvent createDataEvent(DataEventType type) {
        DataEvent event = new DataEvent();
        event.setType(type);
        return event;
    }

    public static Action createAction(String actionCode) {
        Action action = new Action();
        action.setValue(actionCode);
        return action;
    }

    public static Function createFunction(String name, String body) {
        Function result = new Function();
        result.setName(name);
        result.setValue(body);
        result.setScope(Scope.PROCESS);
        return result;
    }

    protected static Coordinates transformCoordinates(int x, int y) {
        return new Coordinates((x * CELL_WIDTH) + HORIZONTAL_OFFSET, (y * CELL_HEIGHT) + VERTICAL_OFFSET);
    }

    public static I18NStringType i18nWithDefaultValue(String defaultValue) {
        I18NStringType result = new I18NStringType();
        result.setValue(defaultValue);
        return result;
    }
}
