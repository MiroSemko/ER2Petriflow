CREATE TABLE department (
    department_id INT PRIMARY KEY,
    name VARCHAR(255)
);

CREATE TABLE employee (
    employee_id INT PRIMARY KEY,
    name VARCHAR(255),
    salary FLOAT,
    department_id INT,
    FOREIGN KEY (department_id) REFERENCES department(department_id)
);
