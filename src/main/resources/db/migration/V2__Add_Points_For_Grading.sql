ALTER TABLE task
    ADD COLUMN wrong_output_penalty INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN exception_handling_penalty INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN wrong_db_content_penalty INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN check_autocommit BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN autocommit_penalty INTEGER;
