package com.team.task_manager.controller.task.entity.request;

import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;
import org.openapitools.jackson.nullable.JsonNullableJackson3Module;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UpdateTaskRequestTest {
    private final ObjectMapper objectMapper = JsonMapper.builder()
            .addModule(new JsonNullableJackson3Module())
            .build();

    @Test
    void distinguishesMissingDeadlineFromExplicitNull() throws Exception {
        UpdateTaskRequest withoutDeadline = objectMapper.readValue("{}", UpdateTaskRequest.class);
        UpdateTaskRequest withNullDeadline = objectMapper.readValue(
                "{\"deadline\":null}",
                UpdateTaskRequest.class
        );

        assertFalse(withoutDeadline.deadline().isPresent());
        assertTrue(withNullDeadline.deadline().isPresent());
        assertNull(withNullDeadline.deadline().orElseThrow());
    }

    @Test
    void rejectsExplicitNullForNonNullableField() throws Exception {
        UpdateTaskRequest request = objectMapper.readValue("{\"name\":null}", UpdateTaskRequest.class);

        Set<ConstraintViolation<UpdateTaskRequest>> violations;
        try (var validatorFactory = Validation.buildDefaultValidatorFactory()) {
            violations = validatorFactory.getValidator().validate(request);
        }

        assertTrue(violations.stream()
                .anyMatch(violation -> violation.getMessage().equals("Название не может быть null")));
    }

    @Test
    void rejectsPriorityOutsideAllowedRange() throws Exception {
        UpdateTaskRequest request = objectMapper.readValue("{\"priority\":11}", UpdateTaskRequest.class);

        Set<ConstraintViolation<UpdateTaskRequest>> violations;
        try (var validatorFactory = Validation.buildDefaultValidatorFactory()) {
            violations = validatorFactory.getValidator().validate(request);
        }

        assertTrue(violations.stream()
                .anyMatch(violation -> violation.getMessage().equals("Приоритет должен быть от 0 до 10")));
    }
}
