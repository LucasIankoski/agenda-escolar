CREATE TABLE IF NOT EXISTS parent_note (
                                     id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    student_id UUID NOT NULL,
    created_by UUID NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    message TEXT NOT NULL,
    read BOOLEAN NOT NULL DEFAULT FALSE,
    read_at TIMESTAMP NULL,
    read_by UUID NULL,

    CONSTRAINT fk_parent_note_student
        FOREIGN KEY (student_id) REFERENCES student(id),

    CONSTRAINT fk_parent_note_created_by
        FOREIGN KEY (created_by) REFERENCES "user_app"(id),

    CONSTRAINT fk_parent_note_read_by
        FOREIGN KEY (read_by) REFERENCES "user_app"(id)
);

CREATE INDEX IF NOT EXISTS ix_parent_note_student_created_at
    ON parent_note(student_id, created_at DESC);

CREATE INDEX IF NOT EXISTS ix_parent_note_student_read
    ON parent_note(student_id, read);
