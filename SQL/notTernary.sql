CREATE TABLE person (
    person_id INT PRIMARY KEY,
    name VARCHAR(255),
    age INT
);

CREATE TABLE dog (
    dog_id INT PRIMARY KEY,
    name VARCHAR(255),
    breed VARCHAR(255),
    person_id INT,
    FOREIGN KEY (person_id) REFERENCES person(person_id)
);

CREATE TABLE car (
    car_id INT PRIMARY KEY,
    make VARCHAR(255),
    model VARCHAR(255),
    year INT,
    person_id INT,
    FOREIGN KEY (person_id) REFERENCES person(person_id)
);

CREATE TABLE house (
    house_id INT PRIMARY KEY,
    address VARCHAR(255),
    square_footage INT,
    person_id INT,
    FOREIGN KEY (person_id) REFERENCES person(person_id)
);
