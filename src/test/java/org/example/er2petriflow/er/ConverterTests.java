package org.example.er2petriflow.er;

import org.example.er2petriflow.er.domain.Attribute;
import org.example.er2petriflow.er.domain.ERDiagram;
import org.example.er2petriflow.er.domain.Entity;
import org.example.er2petriflow.er.domain.Relation;
import org.example.er2petriflow.generated.petriflow.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.example.er2petriflow.er.TestHelper.getTestFile;
import static org.example.er2petriflow.er.converter.PetriflowUtils.*;
import static org.junit.jupiter.api.Assertions.*;

public class ConverterTests {

    private Importer importer;
    private Converter converter;

    @BeforeEach
    void setUp() {
        importer = new Importer();
        converter = new Converter();
    }

//    @Test
    @DisplayName("Should convert single entity")
    void convertSingleEntity() {
        Optional<ERDiagram> result = importer.importDiagram(getTestFile("SingleEntity"));
        assertTrue(result.isPresent());

        ERDiagram diagram = result.get();
        assertNotNull(diagram.getEntities());
        List<Entity> entities = diagram.getEntities();
        assertEquals(1, entities.size());
        Entity entity = entities.get(0);

        List<Document> petriflows = converter.convertToPetriflows(result.get());
        assertNotNull(petriflows);
        assertEquals(1, petriflows.size());

        Document petriflow = petriflows.get(0);

        assertFalse(petriflow.isAnonymousRole());
        assertTrue(petriflow.isDefaultRole());
        assertNotNull(petriflow.getId());
        assertNotNull(petriflow.getTitle());
        assertNotNull(petriflow.getTitle().getValue());
        assertEquals(entity.getName(), petriflow.getTitle().getValue());

        assertNotNull(entity.getAttributes());
        Map<String, Attribute> attributeMap = entity.getAttributes().stream().collect(Collectors.toMap(Attribute::getVariableIdentifier, Function.identity()));

        assertNotNull(petriflow.getData());
        assertEquals(attributeMap.size() + 1, petriflow.getData().size());

        for (Data dataVariable : petriflow.getData()) {
            assertNotNull(dataVariable);

            Attribute attribute = attributeMap.remove(dataVariable.getId());

            if (attribute == null) {
                assertEquals(LAYOUT_TASK_REF_ID, dataVariable.getId());
                assertNotNull(dataVariable.getType());
                assertEquals(DataType.TASK_REF, dataVariable.getType());
                continue;
            }

            assertNotNull(attribute);
            assertNotNull(dataVariable.getTitle());
            assertNotNull(dataVariable.getTitle().getValue());
            assertEquals(attribute.getName(), dataVariable.getTitle().getValue());
            assertNotNull(dataVariable.getType());
            assertNotNull(attribute.getType());
            assertEquals(attribute.getType().getMapping(), dataVariable.getType());
        }

        assertNotNull(petriflow.getRole());
        assertEquals(1, petriflow.getRole().size());
        Role role = petriflow.getRole().get(0);
        assertNotNull(role);
        assertEquals(SYSTEM_ROLE_ID, role.getId());
        assertNotNull(role.getTitle());
        assertNotNull(role.getTitle().getValue());
        assertEquals(SYSTEM_ROLE_TITLE, role.getTitle().getValue());

        assertNotNull(petriflow.getTransition());
        assertEquals(5, petriflow.getTransition().size());
        assertNotNull(petriflow.getPlace());
        assertEquals(2, petriflow.getPlace().size());
        assertNotNull(petriflow.getArc());
        assertEquals(5, petriflow.getArc().size());

        for (Transition t : petriflow.getTransition()) {
            assertNotNull(t.getLabel());
            assertNotNull(t.getDataGroup());
            assertEquals(1, t.getDataGroup().size());
            DataGroup dataGroup = t.getDataGroup().get(0);
            assertNotNull(dataGroup);
            assertNotNull(dataGroup.getId());
            assertEquals(LayoutType.LEGACY, dataGroup.getLayout());

            if (t.getId().equals(LAYOUT_TRANSITION_ID)) {
                assertEquals(entity.getAttributes().size(), dataGroup.getDataRef().size());
            } else {
                assertNotNull(t.getDataGroup());
                assertEquals(1, dataGroup.getDataRef().size());
                DataRef ref = dataGroup.getDataRef().get(0);
                assertNotNull(ref);
                assertEquals(LAYOUT_TASK_REF_ID, ref.getId());

                if (t.getId().equals(DELETE_TRANSITION_ID)) {
                    assertNotNull(t.getEvent());
                    assertEquals(2, t.getEvent().size());
                    for (Event e : t.getEvent()) {
                        assertNotNull(e);
                        assertNotNull(e.getTitle());
                        assertNotNull(e.getTitle().getValue());
                        assertNotNull(e.getType());
                    }
                }
            }
        }
    }

    @Test
    @DisplayName("Should convert single relation")
    void convertSingleRelation() {
        Optional<ERDiagram> result = importer.importDiagram(getTestFile("SingleRelation"));
        assertTrue(result.isPresent());

        ERDiagram diagram = result.get();
        assertNotNull(diagram.getEntities());
        List<Entity> entities = diagram.getEntities();
        assertEquals(2, entities.size());
        List<Relation> relations = diagram.getRelations();
        assertEquals(1, relations.size());

        List<Document> petriflows = converter.convertToPetriflows(result.get());
        assertNotNull(petriflows);
        assertEquals(3, petriflows.size());
    }
}
