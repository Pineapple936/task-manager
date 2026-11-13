package com.team.task_manager.controller.task;

import java.time.OffsetDateTime;

import com.team.task_manager.controller.error.GlobalExceptionHandler;
import com.team.task_manager.controller.error.exception.task.TaskCompletionBlockedException;
import com.team.task_manager.controller.task.entity.TaskMapper;
import com.team.task_manager.jooq.generated.enums.TaskStatus;
import com.team.task_manager.repository.task.entity.TaskDto;
import com.team.task_manager.repository.task.entity.TaskMemberDto;
import com.team.task_manager.jooq.generated.enums.TaskMemberRole;
import com.team.task_manager.service.security.entity.AuthenticatedUser;
import com.team.task_manager.service.task.TaskService;
import com.team.task_manager.service.task.TaskMemberService;
import com.team.task_manager.service.task.entity.filter.TaskFilterCriteria;
import com.team.task_manager.service.task.entity.sort.SortAlgorithm;
import com.team.task_manager.service.task.entity.sort.SortDirection;
import com.team.task_manager.service.task.entity.sort.TaskSortField;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TaskControllerTest {
    private static final Long USER_ID = 10L;

    private TaskService taskService;
    private TaskMemberService taskMemberService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        taskService = mock(TaskService.class);
        taskMemberService = mock(TaskMemberService.class);
        TaskController controller = new TaskController(taskService, taskMemberService, new TaskMapper(), mock(TaskImportReader.class));
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();

        AuthenticatedUser principal = new AuthenticatedUser(USER_ID, "egor");
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated(principal, null, java.util.List.of())
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void returnsConflictWhenTaskDependenciesAreIncomplete() throws Exception {
        when(taskService.update(eq(20L), any(), eq(USER_ID)))
                .thenThrow(new TaskCompletionBlockedException(20L));

        mockMvc.perform(patch("/tasks/20")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"completed\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("TASK_COMPLETION_BLOCKED"))
                .andExpect(jsonPath("$.message").value(
                        "Нельзя завершить задачу, пока не выполнены все её зависимости"
                ));

        verifyNoInteractions(taskMemberService);
    }

    @Test
    void validatesRequestBeforeCallingService() throws Exception {
        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\",\"priority\":5}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));

        verifyNoInteractions(taskService);
    }

    @Test
    void doesNotRequestSortingWhenSortFieldIsMissing() throws Exception {
        TaskFilterCriteria emptyFilter = new TaskFilterCriteria(null, null, null, null);
        when(taskService.findAll(
                USER_ID,
                emptyFilter,
                null,
                SortDirection.ASC,
                SortAlgorithm.MERGE
        )).thenReturn(java.util.List.of());

        mockMvc.perform(get("/tasks"))
                .andExpect(status().isOk());

        verify(taskService).findAll(
                USER_ID,
                emptyFilter,
                null,
                SortDirection.ASC,
                SortAlgorithm.MERGE
        );
    }

    @Test
    void passesFiltersAndSortingToService() throws Exception {
        OffsetDateTime deadlineFrom = OffsetDateTime.parse("2026-10-01T00:00:00Z");
        OffsetDateTime deadlineTo = OffsetDateTime.parse("2026-10-31T23:59:59Z");

        when(taskService.findAll(
                USER_ID,
                new TaskFilterCriteria(TaskStatus.planned, 3, deadlineFrom, deadlineTo),
                TaskSortField.DEADLINE,
                SortDirection.DESC,
                SortAlgorithm.QUICK
        )).thenReturn(java.util.List.of());

        mockMvc.perform(get("/tasks")
                        .queryParam("status", "planned")
                        .queryParam("priority", "3")
                        .queryParam("deadlineFrom", deadlineFrom.toString())
                        .queryParam("deadlineTo", deadlineTo.toString())
                        .queryParam("sortBy", "DEADLINE")
                        .queryParam("direction", "DESC")
                        .queryParam("algorithm", "QUICK"))
                .andExpect(status().isOk());

        verify(taskService).findAll(
                USER_ID,
                new TaskFilterCriteria(TaskStatus.planned, 3, deadlineFrom, deadlineTo),
                TaskSortField.DEADLINE,
                SortDirection.DESC,
                SortAlgorithm.QUICK
        );
    }

    @Test
    void rejectsInvalidDeadlineRange() throws Exception {
        mockMvc.perform(get("/tasks")
                        .queryParam("deadlineFrom", "2026-10-31T00:00:00Z")
                        .queryParam("deadlineTo", "2026-10-01T00:00:00Z"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_TASK_FILTER"));

        verifyNoInteractions(taskService);
    }

    @Test
    void rejectsPriorityFilterOutsideAllowedRange() throws Exception {
        mockMvc.perform(get("/tasks")
                        .queryParam("priority", "11"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_TASK_FILTER"));

        verifyNoInteractions(taskService);
    }

    @Test
    void createsTaskForAuthenticatedUserAndReturnsApiResponse() throws Exception {
        OffsetDateTime createdAt = OffsetDateTime.parse("2026-10-23T00:00:00+03:00");
        TaskDto createdTask = new TaskDto(
                1L,
                USER_ID,
                "Task",
                "Description",
                0,
                TaskStatus.planned,
                null,
                createdAt,
                createdAt
        );
        when(taskService.create(any(), eq(USER_ID))).thenReturn(createdTask);
        when(taskMemberService.findAll(1L, USER_ID)).thenReturn(java.util.List.of(
                new TaskMemberDto(1L, USER_ID, "egor", TaskMemberRole.owner)
        ));

        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Task\",\"description\":\"Description\",\"priority\":0}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Task"))
                .andExpect(jsonPath("$.priority").value(0))
                .andExpect(jsonPath("$.status").value("planned"))
                .andExpect(jsonPath("$.description").value("Description"))
                .andExpect(jsonPath("$.members[0].userId").value(USER_ID))
                .andExpect(jsonPath("$.members[0].username").value("egor"))
                .andExpect(jsonPath("$.members[0].role").value("owner"));

        verify(taskService).create(any(), eq(USER_ID));
        verify(taskMemberService).findAll(1L, USER_ID);
    }

    @Test
    void updatesTaskWithoutResponseBody() throws Exception {
        mockMvc.perform(patch("/tasks/20")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Updated task\"}"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(taskService).update(eq(20L), any(), eq(USER_ID));
        verifyNoInteractions(taskMemberService);
    }

    @Test
    void returnsMembersOnlyInDetailedTaskResponse() throws Exception {
        OffsetDateTime createdAt = OffsetDateTime.parse("2026-10-23T00:00:00+03:00");
        TaskDto task = new TaskDto(
                20L,
                USER_ID,
                "Task",
                "Description",
                0,
                TaskStatus.planned,
                null,
                createdAt,
                createdAt
        );
        when(taskService.findById(20L, USER_ID)).thenReturn(task);
        when(taskMemberService.findAll(20L, USER_ID)).thenReturn(java.util.List.of(
                new TaskMemberDto(20L, USER_ID, "egor", TaskMemberRole.owner)
        ));

        mockMvc.perform(get("/tasks/20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.members[0].username").value("egor"))
                .andExpect(jsonPath("$.members[0].role").value("owner"));
    }

    @Test
    void addsTaskDependencyForAuthenticatedUser() throws Exception {
        mockMvc.perform(post("/tasks/20/dependencies/30"))
                .andExpect(status().isNoContent());

        verify(taskService).addDependency(20L, 30L, USER_ID);
    }

    @Test
    void deletesTaskDependencyForAuthenticatedUser() throws Exception {
        mockMvc.perform(delete("/tasks/20/dependencies/30"))
                .andExpect(status().isNoContent());

        verify(taskService).deleteDependency(20L, 30L, USER_ID);
    }

    @Test
    void addsTaskTagForAuthenticatedUser() throws Exception {
        mockMvc.perform(post("/tasks/20/tags/1"))
                .andExpect(status().isNoContent());

        verify(taskService).addTag(20L, 1L, USER_ID);
    }

    @Test
    void deletesTaskTagForAuthenticatedUser() throws Exception {
        mockMvc.perform(delete("/tasks/20/tags/1"))
                .andExpect(status().isNoContent());

        verify(taskService).deleteTag(20L, 1L, USER_ID);
    }

    @Test
    void returnsTaskMembersForAuthenticatedUser() throws Exception {
        when(taskMemberService.findAll(20L, USER_ID)).thenReturn(java.util.List.of(
                new TaskMemberDto(
                        20L,
                        USER_ID,
                        "egor",
                        TaskMemberRole.owner
                )
        ));

        mockMvc.perform(get("/tasks/20/members"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(USER_ID))
                .andExpect(jsonPath("$[0].username").value("egor"))
                .andExpect(jsonPath("$[0].role").value("owner"));
    }

    @Test
    void addsTaskMemberForAuthenticatedOwner() throws Exception {
        mockMvc.perform(post("/tasks/20/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":30,\"role\":\"editor\"}"))
                .andExpect(status().isCreated());

        verify(taskMemberService).add(
                20L,
                30L,
                TaskMemberRole.editor,
                USER_ID
        );
    }

    @Test
    void changesTaskMemberRoleForAuthenticatedOwner() throws Exception {
        mockMvc.perform(patch("/tasks/20/members/30")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\":\"viewer\"}"))
                .andExpect(status().isNoContent());

        verify(taskMemberService).updateRole(
                20L,
                30L,
                TaskMemberRole.viewer,
                USER_ID
        );
    }

    @Test
    void deletesTaskMemberForAuthenticatedOwner() throws Exception {
        mockMvc.perform(delete("/tasks/20/members/30"))
                .andExpect(status().isNoContent());

        verify(taskMemberService).delete(20L, 30L, USER_ID);
    }
}
