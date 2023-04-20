CREATE TABLE Project (
    pno int, manager varchar, location varchar, delivery_date date, budget int, iname varchar, PRIMARY KEY (pno),
    FOREIGN KEY (manager) REFERENCES Director(m_name),
    FOREIGN KEY (iname) REFERENCES Institute(iname)
);
CREATE TABLE Employee (
    ename varchar, age int, nationality varchar, supervisor varchar, PRIMARY KEY (ename),
    FOREIGN KEY (supervisor) REFERENCES Employee(ename)
);
CREATE TABLE Works(
    ename varchar, pno int, instrument_no int,
    FOREIGN KEY (ename) REFERENCES Employee(ename),
    FOREIGN KEY (instrument_no) REFERENCES Instrument(instrument_no),
    FOREIGN KEY (pno) REFERENCES Project(pno)
);
CREATE TABLE Institute(
    iname varchar, location varchar, head_name varchar,PRIMARY KEY (iname),
    FOREIGN KEY (head_name) REFERENCES Head(h_name)
);
CREATE TABLE Head(
    h_name varchar, hphone int, city varchar, PRIMARY KEY (h_name),
    FOREIGN KEY (h_name) REFERENCES Employee(ename)
);
CREATE TABLE Director(
    m_name varchar, area_of_exp varchar, PRIMARY KEY (m_name),
    FOREIGN KEY (m_name) REFERENCES Employee(ename)
);
CREATE TABLE Supplier(
    sname varchar, location varchar, phone int, fax int, PRIMARY KEY (sname)
);
CREATE TABLE Supply(
    sname varchar, pno int, part_no int, quantity int,
    FOREIGN KEY (sname) REFERENCES Supplier(sname),
    FOREIGN KEY (part_no) REFERENCES Part(part_no),
    FOREIGN KEY (pno) REFERENCES Project(pno)
);
CREATE TABLE Instrument(
    instrument_no int, capacity int, no_of_wheels int, PRIMARY KEY (instrument_no),
    FOREIGN KEY (instrument_no) REFERENCES Part(part_no)
);
CREATE TABLE Part(
    part_no int, m_date date, part_color varchar, PRIMARY KEY (part_no)
);
