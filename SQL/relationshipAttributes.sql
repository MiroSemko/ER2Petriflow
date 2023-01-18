CREATE TABLE students (
    student_id INT PRIMARY KEY,
    name VARCHAR(255)
);

CREATE TABLE courses (
    course_id INT PRIMARY KEY,
    title VARCHAR(255)
);

CREATE TABLE student_courses (
    student_id INT,
    course_id INT,
    PRIMARY KEY (student_id, course_id),
    FOREIGN KEY (student_id) REFERENCES students(student_id),
    FOREIGN KEY (course_id) REFERENCES courses(course_id)
);
