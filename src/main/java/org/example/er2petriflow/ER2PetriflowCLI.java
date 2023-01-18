package org.example.er2petriflow;

import org.example.er2petriflow.er.Converter;
import org.example.er2petriflow.er.Exporter;
import org.example.er2petriflow.er.Importer;
import org.example.er2petriflow.er.ImporterSql;
import org.example.er2petriflow.er.domain.ERDiagram;
import org.example.er2petriflow.generated.petriflow.Document;

import javax.xml.bind.JAXBException;
import java.io.*;
import java.util.List;
import java.util.Optional;

public class ER2PetriflowCLI {

    private static final String DEFAULT_OUTPUT_NAME = "out.zip";

    public static void main(String[] args) {
        String inputPath;
        String outputPath = "." + File.separator;

        if (args.length < 1) {
            System.out.println("Please enter at least one argument: <input-file-path> [output-file-path]");
            return;
        }
        inputPath = args[0].trim();
        if (args.length >= 2) {
            outputPath = args[1].trim();
        }

        FileInputStream inputStream;
        try {
            inputStream = new FileInputStream(inputPath);
        } catch (FileNotFoundException e) {
            System.err.println("Input file located at '" + inputPath + "' could not be found!");
            return;
        }

        File output = new File(outputPath);
        if (output.isDirectory()) {
            output = new File(outputPath + DEFAULT_OUTPUT_NAME);
        }
        FileOutputStream outputStream;
        try {
            outputStream = new FileOutputStream(output);
        } catch (FileNotFoundException e) {
            System.err.println("Output file located at '" + output.getPath() + "' could not be found!");
            return;
        }

        String extension = inputPath.substring(inputPath.lastIndexOf('.') + 1);
        Optional<ERDiagram> diagram;
        if (extension.equals("sql")){
            ImporterSql importerSql = new ImporterSql();
            diagram = importerSql.convert(inputStream);
        } else{
            Importer importer = new Importer();
            diagram = importer.importDiagram(inputStream);
        }
        closeStream(inputStream);
        if (diagram.isEmpty()) {
            System.err.println("The diagram file could not be read! Look above for Exception stacktrace.");
            closeStream(outputStream);
            return;
        }

        Converter converter = new Converter();
        List<Document> petriflows = converter.convertToPetriflows(diagram.get());

        Exporter exporter = new Exporter();
        try {
            exporter.exportToZip(outputStream, petriflows);
        } catch (JAXBException e) {
            e.printStackTrace();
            System.err.println("Petriflows could not be serialized into XML! Look above for Exception stacktrace.");
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("An error has occurred while writing the output file.");
        } finally {
            closeStream(outputStream);
        }
    }

    private static void closeStream(InputStream s) {
        try {
            s.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void closeStream(OutputStream s) {
        try {
            s.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
