CREATE TABLE Oddelenie (
    oddelenie_id INT PRIMARY KEY,
    meno VARCHAR(255)
);
CREATE TABLE Zamestnanec (
    zamestnanec_id INT PRIMARY KEY,
    meno VARCHAR(255),
    oddelenie_id INT,
    FOREIGN KEY (oddelenie_id) REFERENCES Oddelenie(oddelenie_id)
);