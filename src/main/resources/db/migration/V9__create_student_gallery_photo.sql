CREATE TABLE IF NOT EXISTS student_gallery_photo (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    student_id UUID NOT NULL,
    created_by UUID NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    caption VARCHAR(240) NULL,
    original_file_name VARCHAR(255) NOT NULL,
    storage_path VARCHAR(600) NOT NULL,
    thumbnail_path VARCHAR(600) NOT NULL,
    content_type VARCHAR(120) NOT NULL,
    size_in_bytes BIGINT NOT NULL,
    width INTEGER NOT NULL,
    height INTEGER NOT NULL,

    CONSTRAINT fk_student_gallery_photo_student
        FOREIGN KEY (student_id) REFERENCES student(id),

    CONSTRAINT fk_student_gallery_photo_created_by
        FOREIGN KEY (created_by) REFERENCES "user_app"(id)
);

CREATE INDEX IF NOT EXISTS ix_student_gallery_photo_student_created_at
    ON student_gallery_photo(student_id, created_at DESC);
