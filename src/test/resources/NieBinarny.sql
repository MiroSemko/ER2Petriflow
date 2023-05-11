CREATE TABLE Projekt (
    pno int, manazer varchar, budget int, iname varchar, PRIMARY KEY (pno),
    FOREIGN KEY (manazer) REFERENCES Riaditel(m_name),
    FOREIGN KEY (iname) REFERENCES Institut(iname)
);
CREATE TABLE Institut(
    iname varchar, lokacia varchar, riaditel_meno varchar,PRIMARY KEY (iname)
);
CREATE TABLE Riaditel(
    m_name varchar, skusenosti varchar, PRIMARY KEY (m_name)
);
CREATE TABLE Ponuka(
    sname varchar, pno int, part_no int, pocet int,
    FOREIGN KEY (pno) REFERENCES Projekt(pno)
);