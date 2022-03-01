package org.example.er2petriflow.er;

import org.example.er2petriflow.er.domain.Attribute;
import org.example.er2petriflow.er.domain.AttributeType;
import org.example.er2petriflow.er.domain.ERDiagram;
import org.example.er2petriflow.er.domain.Entity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class ImporterTests {

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
        assertEquals("Entity", entity.getName());

        List<Attribute> attributes = entity.getAttributes();
        assertNotNull(attributes);
        assertEquals(2, attributes.size());

        Set<String> titles = new java.util.HashSet<>(Set.of("Attribute1", "Attribute2"));

        for (Attribute a : attributes) {
            assertEquals(AttributeType.TEXT, a.getType());
            assertTrue(titles.contains(a.getName()));
            titles.remove(a.getName());
        }
    }

    private InputStream getTestFile(String fileName) {
        return ImporterTests.class.getResourceAsStream("/" + fileName + ".erdplus");
    }
}
