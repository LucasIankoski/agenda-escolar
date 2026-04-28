CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE IF NOT EXISTS school (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(160) NOT NULL,
    slug VARCHAR(80) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT uk_school_slug UNIQUE (slug)
);

INSERT INTO school (id, name, slug, active)
VALUES ('00000000-0000-0000-0000-000000000001', 'Escola Padrao', 'default', TRUE)
ON CONFLICT (slug) DO NOTHING;

ALTER TABLE user_app
    ADD COLUMN IF NOT EXISTS school_id UUID;

ALTER TABLE classroom
    ADD COLUMN IF NOT EXISTS school_id UUID;

ALTER TABLE student
    ADD COLUMN IF NOT EXISTS school_id UUID;

ALTER TABLE diary
    ADD COLUMN IF NOT EXISTS school_id UUID;

ALTER TABLE parent_note
    ADD COLUMN IF NOT EXISTS school_id UUID;

ALTER TABLE student_gallery_photo
    ADD COLUMN IF NOT EXISTS school_id UUID;

UPDATE user_app
SET school_id = '00000000-0000-0000-0000-000000000001'
WHERE school_id IS NULL;

UPDATE classroom
SET school_id = '00000000-0000-0000-0000-000000000001'
WHERE school_id IS NULL;

UPDATE student s
SET school_id = COALESCE(c.school_id, '00000000-0000-0000-0000-000000000001')
FROM classroom c
WHERE s.classroom_id = c.id
  AND s.school_id IS NULL;

UPDATE student
SET school_id = '00000000-0000-0000-0000-000000000001'
WHERE school_id IS NULL;

UPDATE diary d
SET school_id = COALESCE(s.school_id, u.school_id, '00000000-0000-0000-0000-000000000001')
FROM student s, user_app u
WHERE d.student_id = s.id
  AND d.created_by = u.id
  AND d.school_id IS NULL;

UPDATE diary
SET school_id = '00000000-0000-0000-0000-000000000001'
WHERE school_id IS NULL;

UPDATE parent_note pn
SET school_id = COALESCE(s.school_id, u.school_id, '00000000-0000-0000-0000-000000000001')
FROM student s, user_app u
WHERE pn.student_id = s.id
  AND pn.created_by = u.id
  AND pn.school_id IS NULL;

UPDATE parent_note
SET school_id = '00000000-0000-0000-0000-000000000001'
WHERE school_id IS NULL;

UPDATE student_gallery_photo sgp
SET school_id = COALESCE(s.school_id, u.school_id, '00000000-0000-0000-0000-000000000001')
FROM student s, user_app u
WHERE sgp.student_id = s.id
  AND sgp.created_by = u.id
  AND sgp.school_id IS NULL;

UPDATE student_gallery_photo
SET school_id = '00000000-0000-0000-0000-000000000001'
WHERE school_id IS NULL;

ALTER TABLE user_app
    ALTER COLUMN school_id SET NOT NULL;

ALTER TABLE classroom
    ALTER COLUMN school_id SET NOT NULL;

ALTER TABLE student
    ALTER COLUMN school_id SET NOT NULL;

ALTER TABLE diary
    ALTER COLUMN school_id SET NOT NULL;

ALTER TABLE parent_note
    ALTER COLUMN school_id SET NOT NULL;

ALTER TABLE student_gallery_photo
    ALTER COLUMN school_id SET NOT NULL;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_user_app_school') THEN
        ALTER TABLE user_app
            ADD CONSTRAINT fk_user_app_school FOREIGN KEY (school_id) REFERENCES school(id);
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_classroom_school') THEN
        ALTER TABLE classroom
            ADD CONSTRAINT fk_classroom_school FOREIGN KEY (school_id) REFERENCES school(id);
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_student_school') THEN
        ALTER TABLE student
            ADD CONSTRAINT fk_student_school FOREIGN KEY (school_id) REFERENCES school(id);
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_diary_school') THEN
        ALTER TABLE diary
            ADD CONSTRAINT fk_diary_school FOREIGN KEY (school_id) REFERENCES school(id);
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_parent_note_school') THEN
        ALTER TABLE parent_note
            ADD CONSTRAINT fk_parent_note_school FOREIGN KEY (school_id) REFERENCES school(id);
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_student_gallery_photo_school') THEN
        ALTER TABLE student_gallery_photo
            ADD CONSTRAINT fk_student_gallery_photo_school FOREIGN KEY (school_id) REFERENCES school(id);
    END IF;
END $$;

ALTER TABLE user_app
    DROP CONSTRAINT IF EXISTS user_app_email_key;

ALTER TABLE classroom
    DROP CONSTRAINT IF EXISTS uk_classroom_name;

DO $$
DECLARE
    constraint_name TEXT;
BEGIN
    FOR constraint_name IN
        SELECT con.conname
        FROM pg_constraint con
        JOIN pg_class rel ON rel.oid = con.conrelid
        JOIN pg_namespace nsp ON nsp.oid = rel.relnamespace
        WHERE nsp.nspname = current_schema()
          AND rel.relname = 'user_app'
          AND con.contype = 'u'
          AND (
              SELECT array_agg(att.attname::TEXT ORDER BY att.attnum)
              FROM unnest(con.conkey) AS cols(attnum)
              JOIN pg_attribute att ON att.attrelid = rel.oid AND att.attnum = cols.attnum
          ) = ARRAY['email']::TEXT[]
    LOOP
        EXECUTE format('ALTER TABLE user_app DROP CONSTRAINT %I', constraint_name);
    END LOOP;

    FOR constraint_name IN
        SELECT con.conname
        FROM pg_constraint con
        JOIN pg_class rel ON rel.oid = con.conrelid
        JOIN pg_namespace nsp ON nsp.oid = rel.relnamespace
        WHERE nsp.nspname = current_schema()
          AND rel.relname = 'classroom'
          AND con.contype = 'u'
          AND (
              SELECT array_agg(att.attname::TEXT ORDER BY att.attnum)
              FROM unnest(con.conkey) AS cols(attnum)
              JOIN pg_attribute att ON att.attrelid = rel.oid AND att.attnum = cols.attnum
          ) = ARRAY['name']::TEXT[]
    LOOP
        EXECUTE format('ALTER TABLE classroom DROP CONSTRAINT %I', constraint_name);
    END LOOP;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'uk_user_school_email') THEN
        ALTER TABLE user_app
            ADD CONSTRAINT uk_user_school_email UNIQUE (school_id, email);
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'uk_classroom_school_name') THEN
        ALTER TABLE classroom
            ADD CONSTRAINT uk_classroom_school_name UNIQUE (school_id, name);
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS ix_user_app_school ON user_app(school_id);
CREATE INDEX IF NOT EXISTS ix_classroom_school ON classroom(school_id);
CREATE INDEX IF NOT EXISTS ix_student_school ON student(school_id);
CREATE INDEX IF NOT EXISTS ix_diary_school_student_created_at ON diary(school_id, student_id, created_at DESC);
CREATE INDEX IF NOT EXISTS ix_parent_note_school_student_created_at ON parent_note(school_id, student_id, created_at DESC);
CREATE INDEX IF NOT EXISTS ix_student_gallery_photo_school_student_created_at
    ON student_gallery_photo(school_id, student_id, created_at DESC);
