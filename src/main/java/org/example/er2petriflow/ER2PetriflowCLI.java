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

        String sqlString = "CREATE TABLE Entity1(Attribute1 INT NOT NULL,Attribute2 text NOT NULL);";

        String sqlString2 = "CREATE TABLE Persons (\n" +
                "    PersonId int NOT NULL,\n" +
                "    LastName varchar(255) NOT NULL,\n" +
                "    FirstName varchar(255) NOT NULL,\n" +
                "    Age int,\n" +
                "    PRIMARY KEY (PersonId, LastName)\n"+
                ");" +
                "CREATE TABLE Cars (\n" +
                "   CarId int,\n" +
                "   CarName varchar(255) NOT NULL,\n" +
                "   PRIMARY KEY (CardId)\n" +
                ");";

        String sqlString3 = "CREATE TABLE Persons (\n" +
                "    PersonId int NOT NULL,\n" +
                "    LastName varchar(255) NOT NULL,\n" +
                "    FirstName varchar(255) NOT NULL,\n" +
                "    Age int,\n" +
                "    PRIMARY KEY (PersonId, LastName)\n"+
                ");" +
                "CREATE TABLE Cars (\n" +
                "   PersonId int\n" +
                "   CarId int,\n" +
                "   CarName varchar(255) NOT NULL,\n" +
                "   PRIMARY KEY (CardId),\n" +
                "   FOREIGN KEY (PersonId) REFERENCES Persons(PersonId)\n" +
                ");";

        String sqlString4 = "CREATE TABLE Employee (\n" +
                "    EmployeeID INT PRIMARY KEY,\n" +
                "    EmployeeName VARCHAR(255) NOT NULL,\n" +
                "    Salary DECIMAL(10,2) NOT NULL\n" +
                ");\n" +
                "\n" +
                "CREATE TABLE Department (\n" +
                "    DepartmentID INT PRIMARY KEY,\n" +
                "    DepartmentName VARCHAR(255) NOT NULL\n" +
                ");\n" +
                "\n" +
                "CREATE TABLE WorksIn (\n" +
                "    EmployeeID INT,\n" +
                "    DepartmentID INT,\n" +
                "    StartDate DATE,\n" +
                "    PRIMARY KEY (EmployeeID, DepartmentID),\n" +
                "    FOREIGN KEY (EmployeeID) REFERENCES Employee(EmployeeID),\n" +
                "    FOREIGN KEY (DepartmentID) REFERENCES Department(DepartmentID)\n" +
                ");" +
                "CREATE TABLE Cars (\n" +
                "   CarId int,\n" +
                "   CarName varchar(255) NOT NULL,\n" +
                "   PRIMARY KEY (CardId),\n" +
                "   FOREIGN KEY (PersonId) REFERENCES Employee(EmployeeID)\n" +
                ");";

        String sqlStringTernary = "CREATE TABLE Student (\n" +
                "    StudentID INT PRIMARY KEY,\n" +
                "    FirstName VARCHAR(255),\n" +
                "    LastName VARCHAR(255),\n" +
                "    Email VARCHAR(255)\n" +
                ");\n" +
                "\n" +
                "CREATE TABLE Course (\n" +
                "    CourseID INT PRIMARY KEY,\n" +
                "    CourseName VARCHAR(255),\n" +
                "    Credits INT\n" +
                ");\n" +
                "\n" +
                "CREATE TABLE Semester (\n" +
                "    SemesterID INT PRIMARY KEY,\n" +
                "    Season VARCHAR(255),\n" +
                "    Year INT\n" +
                ");\n" +
                "\n" +
                "CREATE TABLE Enrollment (\n" +
                "    EnrollmentID INT PRIMARY KEY,\n" +
                "    StudentID INT,\n" +
                "    CourseID INT,\n" +
                "    SemesterID INT,\n" +
                "    Grade INT,\n" +
                "    FOREIGN KEY (StudentID) REFERENCES Student(StudentID),\n" +
                "    FOREIGN KEY (CourseID) REFERENCES Course(CourseID),\n" +
                "    FOREIGN KEY (SemesterID) REFERENCES Semester(SemesterID)\n" +
                ");";

        String sqlNotTernary = "CREATE TABLE person (\n" +
                "    person_id INT PRIMARY KEY,\n" +
                "    name VARCHAR(255),\n" +
                "    age INT\n" +
                ");\n" +
                "\n" +
                "CREATE TABLE dog (\n" +
                "    dog_id INT PRIMARY KEY,\n" +
                "    name VARCHAR(255),\n" +
                "    breed VARCHAR(255),\n" +
                "    person_id INT,\n" +
                "    FOREIGN KEY (person_id) REFERENCES person(person_id)\n" +
                ");\n" +
                "\n" +
                "CREATE TABLE car (\n" +
                "    car_id INT PRIMARY KEY,\n" +
                "    make VARCHAR(255),\n" +
                "    model VARCHAR(255),\n" +
                "    year INT,\n" +
                "    person_id INT,\n" +
                "    FOREIGN KEY (person_id) REFERENCES person(person_id)\n" +
                ");\n" +
                "\n" +
                "CREATE TABLE house (\n" +
                "    house_id INT PRIMARY KEY,\n" +
                "    address VARCHAR(255),\n" +
                "    square_footage INT,\n" +
                "    person_id INT,\n" +
                "    FOREIGN KEY (person_id) REFERENCES person(person_id)\n" +
                ");\n";

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

//        Importer importer = new Importer();
//        Optional<ERDiagram> diagram = importer.importDiagram(inputStream);

        ImporterSql importerSql = new ImporterSql();
        Optional<ERDiagram> diagram = importerSql.convert(sqlStringTernary);

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
