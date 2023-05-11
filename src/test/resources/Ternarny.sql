CREATE TABLE Student (
    studentId INT PRIMARY KEY,
    meno VARCHAR(255),
    priezvisko VARCHAR(255)
);

CREATE TABLE Kurz (
    kurzId INT PRIMARY KEY,
    nazov VARCHAR(255),
    kredity INT
);

CREATE TABLE Semester (
    semesterId INT PRIMARY KEY,
    rok INT
);

CREATE TABLE Zapis (
    zapisId INT PRIMARY KEY,
    studentId INT,
    kurzId INT,
    semesterId INT,
    FOREIGN KEY (studentId) REFERENCES Student(studentId),
    FOREIGN KEY (kurzId) REFERENCES Kurz(kurzId),
    FOREIGN KEY (semesterId) REFERENCES Semester(semesterId)
);