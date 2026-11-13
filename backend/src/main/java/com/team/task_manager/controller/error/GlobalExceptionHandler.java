package com.team.task_manager.controller.error;

import com.team.task_manager.controller.error.exception.task.TaskImportException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import java.util.LinkedHashMap;
import java.util.Map;

import com.team.task_manager.controller.error.exception.auth.UsernameAlreadyExistsException;
import com.team.task_manager.controller.error.exception.tag.TagNotFoundException;
import com.team.task_manager.controller.error.exception.tag.TaskTagAlreadyExistsException;
import com.team.task_manager.controller.error.exception.task.InvalidTaskFilterException;
import com.team.task_manager.controller.error.exception.task.InvalidTaskMemberRoleException;
import com.team.task_manager.controller.error.exception.task.TaskAccessDeniedException;
import com.team.task_manager.controller.error.exception.task.TaskCompletionBlockedException;
import com.team.task_manager.controller.error.exception.task.TaskDependencyAlreadyExistsException;
import com.team.task_manager.controller.error.exception.task.TaskDependencyCycleException;
import com.team.task_manager.controller.error.exception.task.TaskDependencyNotFoundException;
import com.team.task_manager.controller.error.exception.task.TaskNotFoundException;
import com.team.task_manager.controller.error.exception.task.TaskMemberAlreadyExistsException;
import com.team.task_manager.controller.error.exception.task.TaskMemberNotFoundException;
import com.team.task_manager.controller.error.exception.task.TaskSelfDependencyException;
import com.team.task_manager.controller.error.entity.response.ErrorResponse;
import com.team.task_manager.controller.error.entity.response.ValidationErrorResponse;
import com.team.task_manager.controller.error.exception.user.UserNotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.team.task_manager.controller.error.exception.sort.EmptyTaskListException;
import com.team.task_manager.controller.error.exception.sort.NullComparatorException;
import com.team.task_manager.controller.error.exception.sort.UnsupportedSortAlgorithmException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(TaskImportException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleTaskImport(TaskImportException exception) {
        return new ErrorResponse(
                400, "IMPORT_VALIDATION_ERROR", exception.getMessage()
        );
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleMissingFile() {
        return new ErrorResponse(400, "VALIDATION_ERROR", "Необходимо передать файл в поле file");
    }

    @ExceptionHandler({ConstraintViolationException.class, HttpMessageNotReadableException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleInvalidRequest(Exception exception) {
        return new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "VALIDATION_ERROR",
                "Переданы некорректные данные"
        );
    }

    @ExceptionHandler(InvalidTaskFilterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleInvalidTaskFilter(InvalidTaskFilterException exception) {
        return new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "INVALID_TASK_FILTER",
                exception.getMessage()
        );
    }

    @ExceptionHandler(TaskSelfDependencyException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleTaskSelfDependency(TaskSelfDependencyException exception) {
        return new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "TASK_SELF_DEPENDENCY",
                "Задача не может зависеть сама от себя"
        );
    }

    @ExceptionHandler(TaskDependencyAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleTaskDependencyAlreadyExists(TaskDependencyAlreadyExistsException exception) {
        return new ErrorResponse(
                HttpStatus.CONFLICT.value(),
                "TASK_DEPENDENCY_ALREADY_EXISTS",
                "Такая зависимость уже существует"
        );
    }

    @ExceptionHandler(TaskCompletionBlockedException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleTaskCompletionBlocked(TaskCompletionBlockedException exception) {
        return new ErrorResponse(
                HttpStatus.CONFLICT.value(),
                "TASK_COMPLETION_BLOCKED",
                "Нельзя завершить задачу, пока не выполнены все её зависимости"
        );
    }

    @ExceptionHandler(TaskDependencyCycleException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleTaskDependencyCycle(TaskDependencyCycleException exception) {
        return new ErrorResponse(
                HttpStatus.CONFLICT.value(),
                "TASK_DEPENDENCY_CYCLE",
                "Зависимость создаёт цикл"
        );
    }

    @ExceptionHandler(TaskDependencyNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleTaskDependencyNotFound(TaskDependencyNotFoundException exception) {
        return new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                "TASK_DEPENDENCY_NOT_FOUND",
                "Зависимость между задачами не найдена"
        );
    }

    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse handleInvalidCredentials(AuthenticationException exception) {
        return new ErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                "INVALID_CREDENTIALS",
                "Неверное имя пользователя или пароль"
        );
    }

    @ExceptionHandler(UsernameAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleUsernameAlreadyExists(UsernameAlreadyExistsException exception) {
        return new ErrorResponse(
                HttpStatus.CONFLICT.value(),
                "USERNAME_ALREADY_EXISTS",
                "Пользователь с таким именем уже существует"
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ValidationErrorResponse handleValidationError(MethodArgumentNotValidException exception) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();

        for (FieldError fieldError : exception.getBindingResult().getFieldErrors()) {
            fieldErrors.putIfAbsent(fieldError.getField(), fieldError.getDefaultMessage());
        }

        return new ValidationErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "VALIDATION_ERROR",
                "Переданы некорректные данные",
                fieldErrors
        );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleDataIntegrityViolation(DataIntegrityViolationException exception) {
        return new ErrorResponse(
                HttpStatus.CONFLICT.value(),
                "DATA_INTEGRITY_VIOLATION",
                "Невозможно выполнить действие"
        );
    }

    @ExceptionHandler(TaskNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleTaskNotFound(TaskNotFoundException exception) {
        return new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                "TASK_NOT_FOUND",
                "Задача не найдена"
        );
    }

    @ExceptionHandler(TaskAccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponse handleTaskAccessDenied(TaskAccessDeniedException exception) {
        return new ErrorResponse(
                HttpStatus.FORBIDDEN.value(),
                "TASK_ACCESS_DENIED",
                "Недостаточно прав для выполнения действия"
        );
    }

    @ExceptionHandler(TagNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleTagNotFound(TagNotFoundException exception) {
        return new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                "TAG_NOT_FOUND",
                "Тег не найден"
        );
    }

    @ExceptionHandler(TaskTagAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleTaskTagAlreadyExists(TaskTagAlreadyExistsException exception) {
        return new ErrorResponse(
                HttpStatus.CONFLICT.value(),
                "TASK_TAG_ALREADY_EXISTS",
                "Тег уже добавлен к задаче"
        );
    }

    @ExceptionHandler(InvalidTaskMemberRoleException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleInvalidTaskMemberRole(InvalidTaskMemberRoleException exception) {
        return new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "INVALID_TASK_MEMBER_ROLE",
                "Нельзя назначить роль owner, изменить роль владельца или удалить его"
        );
    }

    @ExceptionHandler(TaskMemberAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleTaskMemberAlreadyExists(TaskMemberAlreadyExistsException exception) {
        return new ErrorResponse(
                HttpStatus.CONFLICT.value(),
                "TASK_MEMBER_ALREADY_EXISTS",
                "Пользователь уже является участником задачи"
        );
    }

    @ExceptionHandler(TaskMemberNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleTaskMemberNotFound(TaskMemberNotFoundException exception) {
        return new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                "TASK_MEMBER_NOT_FOUND",
                "Участник задачи не найден"
        );
    }

    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleUserNotFound(UserNotFoundException exception) {
        return new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                "USER_NOT_FOUND",
                "Пользователь не найден"
        );
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleUnexpectedError(Exception exception) {
        return new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "INTERNAL_ERROR",
                "Внутренняя ошибка сервера"
        );
    }

    @ExceptionHandler(EmptyTaskListException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleEmptyTaskListException(EmptyTaskListException exception) {
        return new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            "EMPTY_TASK_LIST",
            "Список задач не может быть null"
        );
    }

    @ExceptionHandler(NullComparatorException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleNullComparatorException(NullComparatorException exception) {
        return new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            "NULL_COMPARATOR",
            "Компаратор не может быть null"
        );
    }

    @ExceptionHandler(UnsupportedSortAlgorithmException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleUnsupportedSortAlgorithmException(UnsupportedSortAlgorithmException exception) {
        return new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            "UNSUPPORTED_SORT_ALGORITHM",
            "Неподдерживаемый алгоритм сортировки"
        );
    }
}
