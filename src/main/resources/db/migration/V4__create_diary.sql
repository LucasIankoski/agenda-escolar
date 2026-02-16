CREATE TABLE IF NOT EXISTS diary (
                                     id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    student_id UUID NOT NULL,
    created_by UUID NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now(),

    -- Alimentação
    food_level VARCHAR(20) NOT NULL,
    food_notes TEXT,

    -- Sono
    sleep_start TIMESTAMP NULL,
    sleep_end TIMESTAMP NULL,

    -- Higiene/Banheiro
    hygiene_type VARCHAR(20) NULL,
    hygiene_count INT NULL,
    pee BOOLEAN NULL,
    poop BOOLEAN NULL,
    stool_aspect VARCHAR(20) NULL,
    hygiene_notes TEXT,

    -- Humor
    mood VARCHAR(20) NOT NULL,

    -- Atividades
    activities TEXT,

    -- Leitura do responsável
    read BOOLEAN NOT NULL DEFAULT FALSE,
    read_at TIMESTAMP NULL,
    read_by UUID NULL,

    CONSTRAINT fk_diary_student
    FOREIGN KEY (student_id) REFERENCES student(id),

    CONSTRAINT fk_diary_created_by
    FOREIGN KEY (created_by) REFERENCES "user_app"(id),

    CONSTRAINT fk_diary_read_by
    FOREIGN KEY (read_by) REFERENCES "user_app"(id)
    );

CREATE INDEX IF NOT EXISTS ix_diary_student_created_at ON diary(student_id, created_at DESC);
CREATE INDEX IF NOT EXISTS ix_diary_created_by ON diary(created_by);
