package com.team.task_manager.controller.task;

import java.io.IOException;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.team.task_manager.controller.error.exception.task.TaskImportException;
import com.team.task_manager.controller.task.entity.request.CreateTaskRequest;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class TaskImportReader {
    private static final List<String> HEADERS = List.of("name", "description", "priority", "deadline");
    private final ObjectMapper objectMapper;
    private final Validator validator;

    public List<CreateTaskRequest> read(MultipartFile file) {
        String filename = file.getOriginalFilename();
        String normalizedFilename = filename == null ? "" : filename.toLowerCase(Locale.ROOT);
        if (!normalizedFilename.endsWith(".json") && !normalizedFilename.endsWith(".csv")) {
            throw new TaskImportException("Поддерживаются только файлы .csv и .json");
        }

        String content = readContent(file);
        List<ImportedTask> records = normalizedFilename.endsWith(".json") ? readJson(content) : readCsv(content);
        if (records.isEmpty()) {
            throw new TaskImportException("Файл должен содержать хотя бы одну задачу");
        }

        StringBuilder errors = new StringBuilder();
        for (ImportedTask record : records) {
            if (record.task() == null) {
                errors.append(record.source()).append(": задача не должна быть null\n");
                continue;
            }
            for (var violation : validator.validate(record.task())) {
                errors.append(record.source()).append(": ")
                        .append(violation.getPropertyPath()).append(": ")
                        .append(violation.getMessage()).append("\n");
            }
        }
        if (!errors.isEmpty()) {
            throw new TaskImportException(errors.toString());
        }
        return records.stream().map(ImportedTask::task).toList();
    }

    private String readContent(MultipartFile file) {
        try {
            String content = new String(file.getBytes(), StandardCharsets.UTF_8);
            if (content.startsWith("\uFEFF")) {
                content = content.substring(1);
            }
            if (content.isBlank()) {
                throw new TaskImportException("Файл не должен быть пустым");
            }
            return content;
        } catch (IOException exception) {
            throw new TaskImportException("Не удалось прочитать файл");
        }
    }

    private List<ImportedTask> readJson(String content) {
        try {
            JsonTask[] tasks = objectMapper.readerFor(JsonTask[].class)
                    .with(DeserializationFeature.FAIL_ON_TRAILING_TOKENS)
                    .without(DeserializationFeature.ACCEPT_FLOAT_AS_INT)
                    .readValue(content);
            if (tasks == null) {
                throw new TaskImportException("JSON должен содержать массив задач");
            }
            List<ImportedTask> records = new ArrayList<>();
            for (int index = 0; index < tasks.length; index++) {
                JsonTask task = tasks[index];
                CreateTaskRequest request = task == null ? null : new CreateTaskRequest(
                        task.name(), task.description(), task.priority(), parseDeadline(task.deadline()));
                records.add(new ImportedTask("JSON[" + index + "]", request));
            }
            return records;
        } catch (JacksonException | DateTimeParseException exception) {
            throw new TaskImportException("Некорректный JSON: ожидается массив задач; "
                    + "priority — целое число, deadline — дата ISO 8601 с часовым поясом");
        }
    }

    private List<ImportedTask> readCsv(String content) {
        CSVFormat format = CSVFormat.RFC4180.builder().setHeader().setSkipHeaderRecord(true).get();
        List<ImportedTask> tasks = new ArrayList<>();
        long line = 1;
        try (var parser = format.parse(new StringReader(content))) {
            List<String> headers = parser.getHeaderNames();
            if (headers.size() != HEADERS.size() || !headers.containsAll(HEADERS)) {
                throw new TaskImportException("CSV должен содержать заголовки name,description,priority,deadline");
            }
            var records = parser.iterator();
            while (true) {
                line = parser.getCurrentLineNumber() + 1;
                if (!records.hasNext()) {
                    break;
                }
                CSVRecord record = records.next();
                if (!record.isConsistent()) {
                    throw new TaskImportException("CSV, строка " + line
                            + ": количество полей не совпадает с заголовками");
                }
                tasks.add(new ImportedTask("CSV, строка " + line, csvTask(record)));
            }
        } catch (NumberFormatException exception) {
            throw new TaskImportException("CSV, строка " + line + ": priority: приоритет должен быть целым числом");
        } catch (DateTimeParseException exception) {
            throw new TaskImportException("CSV, строка " + line
                    + ": deadline: дедлайн должен быть датой ISO 8601 с часовым поясом");
        } catch (IOException | UncheckedIOException | IllegalArgumentException exception) {
            throw new TaskImportException("CSV, строка " + line + ": проверьте заголовки, кавычки и разделители");
        }
        return tasks;
    }

    private CreateTaskRequest csvTask(CSVRecord record) {
        String priority = record.get("priority");
        String deadline = record.get("deadline");
        return new CreateTaskRequest(record.get("name"),
                record.get("description").isEmpty() ? null : record.get("description"),
                priority.isEmpty() ? null : Integer.valueOf(priority),
                parseDeadline(deadline.isEmpty() ? null : deadline));
    }

    private OffsetDateTime parseDeadline(String value) {
        return value == null ? null : OffsetDateTime.parse(value);
    }

    private record JsonTask(String name, String description, Integer priority, String deadline) {
    }

    private record ImportedTask(String source, CreateTaskRequest task) {
    }
}
