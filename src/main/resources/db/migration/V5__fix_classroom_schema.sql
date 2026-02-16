-- garante gen_random_uuid()
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- adiciona coluna active se não existir
ALTER TABLE classroom
    ADD COLUMN IF NOT EXISTS active BOOLEAN NOT NULL DEFAULT TRUE;

-- cria unique de name (equivalente ao uk_classroom_name)
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'uk_classroom_name'
    ) THEN
ALTER TABLE classroom
    ADD CONSTRAINT uk_classroom_name UNIQUE (name);
END IF;
END $$;

-- opcional: colocar default no id (não quebra dados existentes)
ALTER TABLE classroom
    ALTER COLUMN id SET DEFAULT gen_random_uuid();
