ALTER TABLE task_group
    DROP COLUMN schema;

ALTER TABLE task_group
    ADD COLUMN create_statements TEXT NOT NULL,
    ADD COLUMN insert_statements_diagnose TEXT NOT NULL,
    ADD COLUMN insert_statements_submission TEXT NOT NULL;
