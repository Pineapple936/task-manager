package com.team.task_manager.service.task;

import java.util.List;
import java.util.Optional;

import com.team.task_manager.controller.error.exception.task.InvalidTaskMemberRoleException;
import com.team.task_manager.controller.error.exception.task.TaskAccessDeniedException;
import com.team.task_manager.controller.error.exception.task.TaskMemberAlreadyExistsException;
import com.team.task_manager.controller.error.exception.task.TaskMemberNotFoundException;
import com.team.task_manager.controller.error.exception.user.UserNotFoundException;
import com.team.task_manager.jooq.generated.enums.TaskMemberRole;
import com.team.task_manager.repository.task.TaskRepository;
import com.team.task_manager.repository.task.entity.TaskDto;
import com.team.task_manager.repository.task.entity.TaskMemberDto;
import com.team.task_manager.repository.task.member.TaskMemberRepository;
import com.team.task_manager.repository.user.UserRepository;
import com.team.task_manager.repository.user.entity.UserDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TaskMemberServiceTest {
    private static final Long TASK_ID = 20L;
    private static final Long OWNER_ID = 10L;
    private static final Long MEMBER_ID = 30L;

    private TaskRepository taskRepository;
    private TaskMemberRepository taskMemberRepository;
    private UserRepository userRepository;
    private TaskMemberService service;

    @BeforeEach
    void setUp() {
        taskRepository = mock(TaskRepository.class);
        taskMemberRepository = mock(TaskMemberRepository.class);
        userRepository = mock(UserRepository.class);
        service = new TaskMemberService(taskRepository, taskMemberRepository, userRepository);
        when(taskRepository.findById(TASK_ID)).thenReturn(Optional.of(task()));
    }

    @Test
    void returnsMembersToTaskParticipant() {
        List<TaskMemberDto> members = List.of(
                new TaskMemberDto(TASK_ID, OWNER_ID, "egor", TaskMemberRole.owner)
        );
        when(taskMemberRepository.findRole(TASK_ID, MEMBER_ID))
                .thenReturn(Optional.of(TaskMemberRole.viewer));
        when(taskMemberRepository.findAll(TASK_ID)).thenReturn(members);

        assertEquals(members, service.findAll(TASK_ID, MEMBER_ID));
    }

    @Test
    void deniesMembersToUserWithoutAccess() {
        assertThrows(TaskAccessDeniedException.class, () -> service.findAll(TASK_ID, MEMBER_ID));
    }

    @Test
    void ownerAddsEditorByUserId() {
        allowOwner();
        UserDto member = new UserDto(MEMBER_ID, "stepa", "hash", null);
        when(userRepository.findById(MEMBER_ID)).thenReturn(Optional.of(member));

        service.add(TASK_ID, MEMBER_ID, TaskMemberRole.editor, OWNER_ID);

        verify(taskMemberRepository).add(TASK_ID, MEMBER_ID, TaskMemberRole.editor);
    }

    @Test
    void rejectsMissingUser() {
        allowOwner();

        assertThrows(
                UserNotFoundException.class,
                () -> service.add(TASK_ID, MEMBER_ID, TaskMemberRole.viewer, OWNER_ID)
        );
    }

    @Test
    void rejectsDuplicateMember() {
        allowOwner();
        UserDto member = new UserDto(MEMBER_ID, "stepa", "hash", null);
        when(userRepository.findById(MEMBER_ID)).thenReturn(Optional.of(member));
        when(taskMemberRepository.findRole(TASK_ID, MEMBER_ID))
                .thenReturn(Optional.of(TaskMemberRole.viewer));

        assertThrows(
                TaskMemberAlreadyExistsException.class,
                () -> service.add(TASK_ID, MEMBER_ID, TaskMemberRole.editor, OWNER_ID)
        );
    }

    @Test
    void rejectsAssigningOwnerRole() {
        allowOwner();

        assertThrows(
                InvalidTaskMemberRoleException.class,
                () -> service.add(TASK_ID, MEMBER_ID, TaskMemberRole.owner, OWNER_ID)
        );
        verify(userRepository, never()).findById(MEMBER_ID);
    }

    @Test
    void editorCannotManageMembers() {
        when(taskMemberRepository.findRole(TASK_ID, OWNER_ID))
                .thenReturn(Optional.of(TaskMemberRole.editor));

        assertThrows(
                TaskAccessDeniedException.class,
                () -> service.delete(TASK_ID, MEMBER_ID, OWNER_ID)
        );
    }

    @Test
    void ownerChangesEditorToViewer() {
        allowOwner();
        when(taskMemberRepository.findRole(TASK_ID, MEMBER_ID))
                .thenReturn(Optional.of(TaskMemberRole.editor));
        when(taskMemberRepository.updateRole(TASK_ID, MEMBER_ID, TaskMemberRole.viewer))
                .thenReturn(true);

        service.updateRole(TASK_ID, MEMBER_ID, TaskMemberRole.viewer, OWNER_ID);

        verify(taskMemberRepository).updateRole(TASK_ID, MEMBER_ID, TaskMemberRole.viewer);
    }

    @Test
    void ownerCannotChangeOwnRole() {
        allowOwner();

        assertThrows(
                InvalidTaskMemberRoleException.class,
                () -> service.updateRole(TASK_ID, OWNER_ID, TaskMemberRole.viewer, OWNER_ID)
        );
    }

    @Test
    void ownerCannotRemoveSelf() {
        allowOwner();

        assertThrows(
                InvalidTaskMemberRoleException.class,
                () -> service.delete(TASK_ID, OWNER_ID, OWNER_ID)
        );
        verify(taskMemberRepository, never()).delete(TASK_ID, OWNER_ID);
    }

    @Test
    void ownerRemovesMember() {
        allowOwner();
        when(taskMemberRepository.findRole(TASK_ID, MEMBER_ID))
                .thenReturn(Optional.of(TaskMemberRole.viewer));
        when(taskMemberRepository.delete(TASK_ID, MEMBER_ID)).thenReturn(true);

        service.delete(TASK_ID, MEMBER_ID, OWNER_ID);

        verify(taskMemberRepository).delete(TASK_ID, MEMBER_ID);
    }

    @Test
    void rejectsRemovingMissingMember() {
        allowOwner();

        assertThrows(
                TaskMemberNotFoundException.class,
                () -> service.delete(TASK_ID, MEMBER_ID, OWNER_ID)
        );
    }

    private void allowOwner() {
        when(taskMemberRepository.findRole(TASK_ID, OWNER_ID))
                .thenReturn(Optional.of(TaskMemberRole.owner));
    }

    private static TaskDto task() {
        return new TaskDto(TASK_ID, OWNER_ID, "Task", "Description", 1, null, null, null, null);
    }
}
