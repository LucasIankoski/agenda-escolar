ALTER TABLE student
    ADD COLUMN IF NOT EXISTS last_name VARCHAR(120);

ALTER TABLE student
    ADD COLUMN IF NOT EXISTS parent_user_id UUID;

DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1
    FROM pg_constraint
    WHERE conname = 'fk_student_parent_user'
  ) THEN
ALTER TABLE student
    ADD CONSTRAINT fk_student_parent_user
        FOREIGN KEY (parent_user_id)
            REFERENCES "user_app"(id);
END IF;
END $$;

CREATE UNIQUE INDEX IF NOT EXISTS uk_student_parent_user_id
    ON student(parent_user_id);
