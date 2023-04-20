CREATE TABLE E1 (
    Attr1 varchar PRIMARY KEY
);
CREATE TABLE E2 (
    Attr2 varchar PRIMARY KEY
);
CREATE TABLE E3 (
    Attr3 varchar PRIMARY KEY
);
CREATE TABLE E4 (
    Attr4 varchar PRIMARY KEY
);
CREATE TABLE E5 (
    Attr5 varchar PRIMARY KEY
);

CREATE TABLE Multirelation (
    f1 int,
    f2 int,
    f3 int,
    f4 int,
    f5 int,
    FOREIGN KEY (f1) REFERENCES E1(Attr1),
    FOREIGN KEY (f2) REFERENCES E2(Attr2),
    FOREIGN KEY (f3) REFERENCES E3(Attr3),
    FOREIGN KEY (f4) REFERENCES E4(Attr4),
    FOREIGN KEY (f5) REFERENCES E5(Attr5)
);