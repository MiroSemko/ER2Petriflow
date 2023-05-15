package org.example.er2petriflow.er;

import org.example.er2petriflow.er.domain.*;
import org.example.er2petriflow.generated.petriflow.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.*;
import java.util.stream.Stream;

import static org.example.er2petriflow.er.TestHelper.getTestFile;
import static org.example.er2petriflow.er.TestHelper.getTestFileSql;
import static org.junit.jupiter.api.Assertions.*;

public class SqlImporterTests {


    private Importer importer;
    private ImporterSql sqlImporter;
    private Converter converter;

    @BeforeEach
    void setUp() {
        importer = new Importer();
        sqlImporter = new ImporterSql();
        converter = new Converter();

    }

    static Stream<String> testFileNames() {
//        return Stream.of("SingleEntity", "SingleRelation2","MultipleEntities2", "5relation");
        return Stream.of("Vztah", "Ternarny", "Binarny", "NieBinarny");
    }

    @ParameterizedTest
    @MethodSource("testFileNames")
    @DisplayName("Should import sql and erdplus file and the final ERDiagram should be the same")
    void importCompare(String fileName) {
        Optional<ERDiagram> result = importer.importDiagram(getTestFile(fileName));
        Optional<ERDiagram> sqlResult = sqlImporter.convert(getTestFileSql(fileName));
        assertTrue(result.isPresent());
        assertTrue(sqlResult.isPresent());

        ERDiagram diagram = result.get();
        assertNotNull(diagram.getEntities());
        ERDiagram sqlDiagram = sqlResult.get();
        assertNotNull(sqlDiagram.getEntities());

        System.out.println(diagram.toVisualString());
        System.out.println(sqlDiagram.toVisualString());

        assertTrue(diagram.getEntities().containsAll(sqlDiagram.getEntities()));
        assertTrue(diagram.getRelations().containsAll(sqlDiagram.getRelations()));

        assertEquals(diagram, sqlDiagram);
    }

    @ParameterizedTest
    @MethodSource("testFileNames")
    @DisplayName("Should import sql and erdplus file and the final converted ERD should be the same")
    void convertCompare(String fileName) {
        Optional<ERDiagram> result = importer.importDiagram(getTestFile(fileName));
        Optional<ERDiagram> sqlResult = sqlImporter.convert(getTestFileSql(fileName));

        ERDiagram diagram = result.get();
        ERDiagram sqlDiagram = sqlResult.get();

        assertEquals(diagram, sqlDiagram);


        List<Document> petriflows = converter.convertToPetriflows(diagram);
        List<Document> sqlPetriflows = converter.convertToPetriflows(sqlDiagram);

        assertEquals(petriflows, sqlPetriflows);

    }
}