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

    protected static final int VERTICAL_OFFSET = 20;
    protected static final int HORIZONTAL_OFFSET = 20;
    protected static final int CELL_WIDTH = 40;
    protected static final int CELL_HEIGHT = 40;

    protected Role systemRole;

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
        Place p1 = createPlace("p1", 0, 2, 1);
        Place p2 = createPlace("p2", 4, 2, 0);
        addPlaces(petriflow, p1, p2);
        Transition t1 = createTransition("t1", "Create", 2, 2);
        Transition t2 = createTransition("t2", "Read", 6, 0);
        Transition t3 = createTransition("t3", "Update", 6, 2);
        Transition t4 = createTransition("t4", "Delete", 6, 4);
        Transition layout = createTransition("layout", 2, 0);
        addTransitions(petriflow, t1, t2, t3, t4, layout);

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

    protected Transition createTransition(String id, String label, int x, int y) {
        Transition t = createTransition(id, x, y);
        t.setLabel(i18nWithDefaultValue(label));
        return t;
    }

    protected Transition createTransition(String id, int x, int y) {
        Transition t = new Transition();
        t.setId(id);
        Coordinates pos = transformCoordinates(x, y);
        t.setX(pos.getX());
        t.setY(pos.getY());
        return t;
    }

    protected void addTransitions(Document pn, Transition... transitions) {
        pn.getTransition().addAll(List.of(transitions));
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