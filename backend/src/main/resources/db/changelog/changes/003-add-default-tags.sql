--liquibase formatted sql

--changeset task-manager:003-add-default-tags
INSERT INTO tags (name)
VALUES
  ('Работа'),
  ('Учёба'),
  ('Личное'),
  ('Важное');

--rollback DELETE FROM tags WHERE name IN ('Работа', 'Учёба', 'Личное', 'Важное');
