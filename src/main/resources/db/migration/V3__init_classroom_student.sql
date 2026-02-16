-- Initial schema for Classroom and Student
-- Uses pgcrypto for gen_random_uuid()

CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE IF NOT EXISTS classroom (
                                         id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(120) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uk_classroom_name UNIQUE (name)
    );

CREATE TABLE IF NOT EXISTS student (
                                       id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(120) NOT NULL,
    birth_date TIMESTAMP NULL,
    classroom_id UUID NOT NULL,
    CONSTRAINT fk_student_classroom
    FOREIGN KEY (classroom_id)
    REFERENCES classroom (id)
    );

CREATE INDEX IF NOT EXISTS idx_student_classroom_id ON student(classroom_id);