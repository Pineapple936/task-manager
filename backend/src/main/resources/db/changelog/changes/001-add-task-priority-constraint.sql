--liquibase formatted sql

--changeset task-manager:001-add-task-priority-constraint
ALTER TABLE tasks
  ADD CONSTRAINT chk_tasks_priority_range
  CHECK (priority BETWEEN 0 AND 10);

--rollback ALTER TABLE tasks DROP CONSTRAINT chk_tasks_priority_range;
