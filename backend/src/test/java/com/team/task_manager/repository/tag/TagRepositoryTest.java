package com.team.task_manager.repository.tag;

import java.util.List;
import java.util.UUID;

import com.team.task_manager.jooq.generated.enums.TaskStatus;
import com.team.task_manager.repository.tag.entity.TagDto;
import com.team.task_manager.repository.task.TaskRepository;
import com.team.task_manager.repository.task.entity.TaskDto;
import org.jooq.DSLContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static com.team.task_manager.jooq.generated.Tables.USERS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class TagRepositoryTest {
    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private DSLContext dsl;

    @Test
    void returnsDefaultTags() {
        assertEquals(
                List.of("Работа", "Учёба", "Личное", "Важное"),
                tagRepository.findAll().stream().map(TagDto::name).toList()
        );
    }

    @Test
    void addsLoadsAndDeletesTaskTag() {
        Long userId = dsl.insertInto(USERS)
                .set(USERS.USERNAME, "tag-test-" + UUID.randomUUID())
                .set(USERS.PASSWORD_HASH, "hash")
                .returning(USERS.ID)
                .fetchSingle(USERS.ID);
        TaskDto task = taskRepository.create(new TaskDto(
                null,
                userId,
                "Task with tag",
                "",
                1,
                TaskStatus.planned,
                null,
                null,
                null
        ));
        taskRepository.addOwner(task.id(), userId);
        TagDto tag = tagRepository.findAll().getFirst();

        taskRepository.addTag(task.id(), tag.id());

        assertTrue(taskRepository.taskTagExists(task.id(), tag.id()));
        assertEquals(List.of(tag), taskRepository.findById(task.id()).orElseThrow().tags());
        assertTrue(taskRepository.deleteTag(task.id(), tag.id()));
        assertFalse(taskRepository.taskTagExists(task.id(), tag.id()));
    }
}
