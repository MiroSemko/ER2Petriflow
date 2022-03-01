package org.example.er2petriflow.er;

import org.example.er2petriflow.er.domain.ERDiagram;
import org.example.er2petriflow.er.domain.Entity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class ImporterTests {

    Importer importer;

    @BeforeEach
    void setUp() {
        importer = new Importer();
    }

    @Test
    @DisplayName("Should import single entity")
    void importSingleCompactEntity() {
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
    }

    private InputStream getTestFile(String fileName) {
        return ImporterTests.class.getResourceAsStream("/" + fileName + ".erdplus");
    }
}
