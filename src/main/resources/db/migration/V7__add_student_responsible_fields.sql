ALTER TABLE student
    ADD COLUMN IF NOT EXISTS responsible_name VARCHAR(120);

ALTER TABLE student
    ADD COLUMN IF NOT EXISTS responsible_last_name VARCHAR(120);

ALTER TABLE student
    ADD COLUMN IF NOT EXISTS responsible_contact VARCHAR(32);
