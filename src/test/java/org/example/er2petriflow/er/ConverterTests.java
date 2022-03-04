package org.example.er2petriflow.er;

import org.example.er2petriflow.er.domain.ERDiagram;
import org.example.er2petriflow.generated.petriflow.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.example.er2petriflow.er.TestHelper.getTestFile;
import static org.junit.jupiter.api.Assertions.*;

public class ConverterTests {

    private Importer importer;
    private Converter converter;

    @BeforeEach
    void setUp() {
        importer = new Importer();
        converter = new Converter();
    }

    @Test
    @DisplayName("Should convert single entity")
    void convertSingleEntity() {
        Optional<ERDiagram> result = importer.importDiagram(getTestFile("SingleEntity"));
        assertTrue(result.isPresent());

        List<Document> petriflows = converter.convertToPetriflows(result.get());
        assertNotNull(petriflows);
        assertEquals(1, petriflows.size());
    }
}
