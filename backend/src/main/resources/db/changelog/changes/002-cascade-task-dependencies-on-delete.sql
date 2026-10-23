--liquibase formatted sql

--changeset task-manager:002-cascade-task-dependencies-on-delete
ALTER TABLE task_dependencies
  DROP CONSTRAINT fk_task_dependencies_task,
  DROP CONSTRAINT fk_task_dependencies_depends_on_task;

ALTER TABLE task_dependencies
  ADD CONSTRAINT fk_task_dependencies_task
    FOREIGN KEY (task_id) REFERENCES tasks (id) ON DELETE CASCADE,
  ADD CONSTRAINT fk_task_dependencies_depends_on_task
    FOREIGN KEY (depends_on_task_id) REFERENCES tasks (id) ON DELETE CASCADE;

--rollback ALTER TABLE task_dependencies DROP CONSTRAINT fk_task_dependencies_task, DROP CONSTRAINT fk_task_dependencies_depends_on_task;
--rollback ALTER TABLE task_dependencies ADD CONSTRAINT fk_task_dependencies_task FOREIGN KEY (task_id) REFERENCES tasks (id) ON DELETE RESTRICT, ADD CONSTRAINT fk_task_dependencies_depends_on_task FOREIGN KEY (depends_on_task_id) REFERENCES tasks (id) ON DELETE RESTRICT;
