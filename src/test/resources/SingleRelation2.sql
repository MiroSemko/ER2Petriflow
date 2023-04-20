CREATE TABLE Entity1
(
  Attribute1 INT PRIMARY KEY,
  Attribute2 varchar NOT NULL
);

CREATE TABLE Entity_with_more_attributes
(
  New_Attribute INT NOT NULL,
  NewAttribute2 INT NOT NULL,
  NewAttribute3 INT NOT NULL,
  NewAttribute4 INT NOT NULL,
  FOREIGN KEY (NewAttribute4) REFERENCES Entity1(Attribute1)

);