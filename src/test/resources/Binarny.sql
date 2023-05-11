CREATE TABLE Student (
    student_id INT PRIMARY KEY,
    meno VARCHAR(255)
);
CREATE TABLE Kurz (
    kurz_id INT PRIMARY KEY,
    nazov VARCHAR(255)
);
CREATE TABLE Zapis (
    student_id INT,
    kurz_id INT,
    PRIMARY KEY (student_id, kurz_id),
    FOREIGN KEY (student_id) REFERENCES Student(student_id),
    FOREIGN KEY (kurz_id) REFERENCES Kurz(kurz_id)
);