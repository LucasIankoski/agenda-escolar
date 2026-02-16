CREATE TABLE IF NOT EXISTS classroom (
                                         id UUID PRIMARY KEY,
                                         name VARCHAR(120) NOT NULL
    );

CREATE TABLE IF NOT EXISTS user_app (
                                        id UUID PRIMARY KEY,
                                        name VARCHAR(255),
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    type VARCHAR(30) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE
    );

CREATE TABLE IF NOT EXISTS student (
                                       id UUID PRIMARY KEY,
                                       name VARCHAR(120) NOT NULL,
    last_name VARCHAR(120) NOT NULL,
    birth_date TIMESTAMP,
    classroom_id UUID NOT NULL,
    parent_user_id UUID,
    CONSTRAINT fk_student_classroom FOREIGN KEY (classroom_id) REFERENCES classroom(id),
    CONSTRAINT fk_student_parent_user FOREIGN KEY (parent_user_id) REFERENCES user_app(id)
    );
