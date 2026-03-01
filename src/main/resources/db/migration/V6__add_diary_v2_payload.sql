ALTER TABLE diary
    ADD COLUMN IF NOT EXISTS diary_version VARCHAR(10) NOT NULL DEFAULT 'V1';

ALTER TABLE diary
    ADD COLUMN IF NOT EXISTS v2_payload TEXT NULL;

CREATE INDEX IF NOT EXISTS ix_diary_student_version_created_at
    ON diary(student_id, diary_version, created_at DESC);
