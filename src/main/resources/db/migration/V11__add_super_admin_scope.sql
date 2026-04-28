ALTER TABLE user_app
    ALTER COLUMN school_id DROP NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uk_user_global_email
    ON user_app(email)
    WHERE school_id IS NULL;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'ck_user_school_scope') THEN
        ALTER TABLE user_app
            ADD CONSTRAINT ck_user_school_scope CHECK (
                (type = 'SUPER_ADMIN' AND school_id IS NULL)
                OR
                (type <> 'SUPER_ADMIN' AND school_id IS NOT NULL)
            );
    END IF;
END $$;
