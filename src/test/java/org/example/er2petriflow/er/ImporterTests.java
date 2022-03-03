package org.example.er2petriflow.er;

import org.example.er2petriflow.er.domain.Attribute;
import org.example.er2petriflow.er.domain.AttributeType;
import org.example.er2petriflow.er.domain.ERDiagram;
import org.example.er2petriflow.er.domain.Entity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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
        assertNotNull(diagram.getRelations());
        assertEquals(0, diagram.getRelations().size());
        assertNotNull(diagram.getEntities());
        assertEquals(1, diagram.getEntities().size());

        Entity entity = diagram.getEntities().get(0);
        assertNotNull(entity);
        assertEquals("Entity1", entity.getName());
        testEntity1(entity);
    }

    @Test
    @DisplayName("Should import multiple entities")
    void importMultipleEntities() {
        Optional<ERDiagram> result = importer.importDiagram(getTestFile("MultipleEntities"));
        assertTrue(result.isPresent());

        ERDiagram diagram = result.get();
        assertNotNull(diagram.getRelations());
        assertEquals(0, diagram.getRelations().size());
        assertNotNull(diagram.getEntities());
        assertEquals(2, diagram.getEntities().size());

        Set<String> titles = new HashSet<>(Set.of(ENTITY_1_NAME, ENTITY_2_NAME));

        for (Entity e : diagram.getEntities()) {
            assertNotNull(e);
            assertTrue(titles.contains(e.getName()));
            titles.remove(e.getName());
            switch (e.getName()) {
                case ENTITY_1_NAME:
                    testEntity1(e);
                    break;
                case ENTITY_2_NAME:
                    testEntity2(e);
                    break;
            }
        }
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
}