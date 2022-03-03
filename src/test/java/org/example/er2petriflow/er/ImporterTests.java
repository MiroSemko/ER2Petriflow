package org.example.er2petriflow.er;

import lombok.AllArgsConstructor;
import org.example.er2petriflow.er.domain.Attribute;
import org.example.er2petriflow.er.domain.AttributeType;
import org.example.er2petriflow.er.domain.ERDiagram;
import org.example.er2petriflow.er.domain.Entity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.*;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

public class ImporterTests {

    private static final String ENTITY_1_NAME = "Entity1";
    private static final String ENTITY_2_NAME = "Entity with more attributes";

    Importer importer;

    @BeforeEach
    void setUp() {
        importer = new Importer();
    }

    @Test
    @DisplayName("Should import single entity")
    void importSingleEntity() {
        Optional<ERDiagram> result = importer.importDiagram(getTestFile("SingleEntity"));
        assertTrue(result.isPresent());

        ERDiagram diagram = result.get();
        assertNotNull(diagram.getEntities());
        assertEquals(1, diagram.getEntities().size());
        assertNotNull(diagram.getRelations());
        assertEquals(0, diagram.getRelations().size());

        testEntities(diagram, new EntityTest(ENTITY_1_NAME, this::testEntity1));
    }

    @Test
    @DisplayName("Should import multiple entities")
    void importMultipleEntities() {
        Optional<ERDiagram> result = importer.importDiagram(getTestFile("MultipleEntities"));
        assertTrue(result.isPresent());

        ERDiagram diagram = result.get();
        assertNotNull(diagram.getEntities());
        assertEquals(2, diagram.getEntities().size());
        assertNotNull(diagram.getRelations());
        assertEquals(0, diagram.getRelations().size());

        testEntities(diagram, new EntityTest(ENTITY_1_NAME, this::testEntity1), new EntityTest(ENTITY_2_NAME, this::testEntity2));
    }

    @Test
    @DisplayName("Should import single relation")
    void importSingleRelation() {
        Optional<ERDiagram> result = importer.importDiagram(getTestFile("SingleRelation"));
        assertTrue(result.isPresent());

        ERDiagram diagram = result.get();
        assertNotNull(diagram.getEntities());
        assertEquals(2, diagram.getEntities().size());
        assertNotNull(diagram.getRelations());
        assertEquals(1, diagram.getRelations().size());


    }

    private InputStream getTestFile(String fileName) {
        return ImporterTests.class.getResourceAsStream("/" + fileName + ".erdplus");
    }

    private void testEntity1(Entity entity) {
        testAttributes(entity, Set.of("Attribute1", "Attribute2"), AttributeType.TEXT);
    }

    private void testEntity2(Entity entity) {
        testAttributes(entity, Set.of("New Attribute", "NewAttribute2", "NewAttribute3", "NewAttribute4"), AttributeType.NUMBER);
    }

    private void testEntities(ERDiagram diagram, EntityTest... tests) {
        HashMap<String, Consumer<Entity>> testMap = new HashMap<>();
        for (EntityTest test : tests) {
            testMap.put(test.entityName, test.entityTest);
        }

        for (Entity e : diagram.getEntities()) {
            assertNotNull(e);
            assertTrue(testMap.containsKey(e.getName()));
            Consumer<Entity> test = testMap.remove(e.getName());
            test.accept(e);
        }
    }

    private void testAttributes(Entity entity, Set<String> expectedTitles, AttributeType expectedType) {
        List<Attribute> attributes = entity.getAttributes();
        int expectedCount = expectedTitles.size();

        assertNotNull(attributes);
        assertEquals(expectedCount, attributes.size());

        Set<String> titles = new HashSet<>(expectedTitles);
        for (Attribute a : attributes) {
            assertEquals(expectedType, a.getType());
            assertTrue(titles.contains(a.getName()));
            titles.remove(a.getName());
        }
    }

    @AllArgsConstructor
    private static class EntityTest {
        private String entityName;
        private Consumer<Entity> entityTest;
    }
}