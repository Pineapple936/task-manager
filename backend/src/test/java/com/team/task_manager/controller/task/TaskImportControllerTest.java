package com.team.task_manager.controller.task;

import java.util.List;

import com.team.task_manager.controller.error.GlobalExceptionHandler;
import com.team.task_manager.controller.task.entity.TaskMapper;
import com.team.task_manager.jooq.generated.enums.TaskStatus;
import com.team.task_manager.service.security.entity.AuthenticatedUser;
import com.team.task_manager.service.task.TaskService;
import com.team.task_manager.service.task.TaskMemberService;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.jackson.databind.json.JsonMapper;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static com.team.task_manager.controller.task.TaskImportReaderTest.file;

class TaskImportControllerTest {
    private final TaskService taskService = mock(TaskService.class);
    private ValidatorFactory factory;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        factory = Validation.buildDefaultValidatorFactory();
        var reader = new TaskImportReader(JsonMapper.builder().findAndAddModules().build(), factory.getValidator());
        mvc = MockMvcBuilders.standaloneSetup(new TaskController(taskService, mock(TaskMemberService.class), new TaskMapper(), reader))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver()).build();
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated(new AuthenticatedUser(10L, "egor"), null, List.of()));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        factory.close();
    }

    @Test
    void returnsCreatedWithoutBodyForBothFormats() throws Exception {
        mvc.perform(multipart("/tasks/import").file(file("tasks.json", "[{\"name\":\"A\",\"priority\":1}]")))
                .andExpect(status().isCreated()).andExpect(content().string(""));
        mvc.perform(multipart("/tasks/import").file(file("tasks.csv", "name,description,priority,deadline\nA,,1,")))
                .andExpect(status().isCreated()).andExpect(content().string(""));
        verify(taskService, times(2)).createAll(argThat(tasks -> tasks.size() == 1
                && tasks.getFirst().status() == TaskStatus.planned
                && tasks.getFirst().description().equals("")), eq(10L));
    }

    @Test
    void returnsRecordErrorsAndDoesNotSaveValidTasksEither() throws Exception {
        mvc.perform(multipart("/tasks/import").file(file("tasks.json", """
                [{"name":"Верная","priority":1},{"name":"","priority":1}]
                """)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("IMPORT_VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value(containsString("JSON[1]")))
                .andExpect(jsonPath("$.message").value(containsString("name: Название не должно быть пустым")))
                .andExpect(jsonPath("$.items").doesNotExist());
        verifyNoInteractions(taskService);
    }

    @Test
    void reportsCsvLineAndDoesNotSaveTasks() throws Exception {
        mvc.perform(multipart("/tasks/import").file(file("tasks.csv",
                        "name,description,priority,deadline\nA,,1,\nB,,invalid,")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("CSV, строка 3")))
                .andExpect(jsonPath("$.message").value(containsString("priority:")))
                .andExpect(jsonPath("$.items").doesNotExist());
        verifyNoInteractions(taskService);
    }

    @Test
    void returnsAllErrorsInOneMessageWithoutSaving() throws Exception {
        mvc.perform(multipart("/tasks/import").file(file("tasks.csv", """
                name,description,priority,deadline
                Верная,,1,
                A,,11,
                ,,1,
                """)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("IMPORT_VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value(containsString("CSV, строка 3")))
                .andExpect(jsonPath("$.message").value(containsString("priority:")))
                .andExpect(jsonPath("$.message").value(containsString("CSV, строка 4")))
                .andExpect(jsonPath("$.message").value(containsString("name:")))
                .andExpect(jsonPath("$.items").doesNotExist());
        verifyNoInteractions(taskService);
    }

    @Test
    void rejectsMissingAndEmptyFiles() throws Exception {
        mvc.perform(multipart("/tasks/import")).andExpect(status().isBadRequest());
        mvc.perform(multipart("/tasks/import").file(file("tasks.json", "")))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(taskService);
    }
}
