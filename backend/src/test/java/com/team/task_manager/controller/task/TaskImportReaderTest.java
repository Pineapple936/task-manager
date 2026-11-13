package com.team.task_manager.controller.task;

import java.nio.charset.StandardCharsets;

import com.team.task_manager.controller.error.exception.task.TaskImportException;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import tools.jackson.databind.json.JsonMapper;

import static org.junit.jupiter.api.Assertions.*;

class TaskImportReaderTest {
    private static final ValidatorFactory FACTORY = Validation.buildDefaultValidatorFactory();
    private final TaskImportReader reader = new TaskImportReader(JsonMapper.builder().findAndAddModules().build(), FACTORY.getValidator());

    @AfterAll
    static void closeValidator() {
        FACTORY.close();
    }

    @Test
    void readsJsonWithNullableFieldsAndPriorityBoundaries() {
        var tasks = reader.read(file("tasks.JSON", """
                [{"name":"Отчёт","priority":0,"deadline":"2026-11-01T18:00:00+03:00"},
                 {"name":"Проверка","description":null,"priority":10,"deadline":null}]
                """));
        assertEquals(2, tasks.size());
        assertEquals("Отчёт", tasks.getFirst().name());
        assertEquals(0, tasks.getFirst().priority());
        assertEquals(3 * 3600, tasks.getFirst().deadline().getOffset().getTotalSeconds());
        assertNull(tasks.getLast().description());
        assertNull(tasks.getLast().deadline());
    }

    @Test
    void readsCsvWithBomQuotesCommasAndMultilineText() {
        var tasks = reader.read(file("tasks.csv", "\uFEFF" + """
                name,description,priority,deadline
                "Отчёт, итоговый","Строка 1
                Строка 2 с ""кавычками""\",3,2026-11-01T18:00:00+03:00
                Проверка,,2,
                """));
        assertEquals(2, tasks.size());
        assertEquals("Отчёт, итоговый", tasks.getFirst().name());
        assertEquals("Строка 1\nСтрока 2 с \"кавычками\"", tasks.getFirst().description());
        assertNull(tasks.getLast().description());
        assertNull(tasks.getLast().deadline());
    }

    @Test
    void reportsJsonIndexAndExistingValidationRules() {
        var exception = assertThrows(TaskImportException.class, () -> reader.read(file("tasks.json", """
                [{"name":"Верная","priority":1},{"name":" ","priority":11,"description":"%s"}]
                """.formatted("a".repeat(5001)))));
        assertTrue(exception.getMessage().contains("JSON[1]"));
        assertTrue(exception.getMessage().contains("name: Название не должно быть пустым"));
        assertTrue(exception.getMessage().contains("priority: Приоритет должен быть от 0 до 10"));
        assertTrue(exception.getMessage().contains("description: Описание должно содержать не больше 5000 символов"));
    }

    @Test
    void reportsPhysicalCsvLineAfterMultilineRecord() {
        var exception = assertThrows(TaskImportException.class, () -> reader.read(file("tasks.csv", """
                name,description,priority,deadline
                Первая,"Две
                строки",1,
                Вторая,,abc,
                """)));
        assertTrue(exception.getMessage().contains("CSV, строка 4"));
        assertTrue(exception.getMessage().contains("priority: приоритет должен быть целым числом"));
    }

    @Test
    void rejectsInvalidJsonShapesAndTypes() {
        for (String content : new String[]{"{}", "null", "[null]", "[1]", "[", "[] {}",
                "[{\"name\":\"A\",\"priority\":1.5}]",
                "[{\"name\":\"A\",\"priority\":1,\"deadline\":123}]",
                "[{\"name\":{},\"priority\":1}]"}) {
            assertThrows(TaskImportException.class, () -> reader.read(file("tasks.json", content)), content);
        }
    }

    @Test
    void rejectsEmptyFilesUnsupportedFormatsAndWrongContent() {
        for (String name : new String[]{"tasks.txt", "tasks.csv", "tasks.json"}) {
            assertThrows(TaskImportException.class, () -> reader.read(file(name, "")));
        }
        assertThrows(TaskImportException.class, () -> reader.read(file("tasks.json", "[]")));
        assertThrows(TaskImportException.class, () -> reader.read(file("tasks.csv", "name,description,priority,deadline\n")));
        assertThrows(TaskImportException.class, () -> reader.read(file("tasks.csv", "[{\"name\":\"A\"}]")));
        assertThrows(TaskImportException.class, () -> reader.read(file("tasks.json", "name,description,priority,deadline\nA,,1,")));
    }

    @Test
    void rejectsMissingDuplicateHeadersAndBrokenCsv() {
        for (String content : new String[]{"name,priority\nA,1", "name,,priority,deadline\nA,,1,", "name,name,priority,deadline\nA,A,1,",
                "name,description,priority,deadline\nA,,1,extra,field",
                "name,description,priority,deadline\n\"unfinished"}) {
            assertThrows(TaskImportException.class, () -> reader.read(file("tasks.csv", content)), content);
        }
    }

    @Test
    void collectsValidationErrorsFromAllCsvRecords() {
        var exception = assertThrows(TaskImportException.class, () -> reader.read(file("tasks.csv", """
                name,description,priority,deadline
                ,,1,
                A,,,
                B,,-1,
                """)));
        assertTrue(exception.getMessage().contains("CSV, строка 2"));
        assertTrue(exception.getMessage().contains("name: Название не должно быть пустым"));
        assertTrue(exception.getMessage().contains("CSV, строка 3"));
        assertTrue(exception.getMessage().contains("priority: Приоритет обязателен"));
        assertTrue(exception.getMessage().contains("CSV, строка 4"));
        assertTrue(exception.getMessage().contains("priority: Приоритет должен быть от 0 до 10"));
    }

    @Test
    void validatesRequiredCsvPriorityAndRange() {
        for (String priority : new String[]{"", "-1", "11"}) {
            var exception = assertThrows(TaskImportException.class, () -> reader.read(file("tasks.csv",
                    "name,description,priority,deadline\nA,," + priority + ",")));
            assertTrue(exception.getMessage().contains("CSV, строка 2"));
            assertTrue(exception.getMessage().contains("priority:"));
        }
    }

    @Test
    void collectsValidationErrorsFromAllJsonRecords() {
        var exception = assertThrows(TaskImportException.class, () -> reader.read(file("tasks.json", """
                [{"name":"","priority":1},{"name":"B","priority":11}]
                """)));
        assertTrue(exception.getMessage().contains("JSON[0]"));
        assertTrue(exception.getMessage().contains("name: Название не должно быть пустым"));
        assertTrue(exception.getMessage().contains("JSON[1]"));
        assertTrue(exception.getMessage().contains("priority: Приоритет должен быть от 0 до 10"));
    }

    @Test
    void rejectsJsonWithUnreadablePriority() {
        for (String value : new String[]{"1.5", "true", "2147483648", "[]", "{}"}) {
            var exception = assertThrows(TaskImportException.class, () -> reader.read(file("tasks.json",
                    "[{\"name\":\"A\",\"priority\":" + value + "}]")));
            assertTrue(exception.getMessage().contains("Некорректный JSON"));
            assertTrue(exception.getMessage().contains("priority"));
        }
    }

    @Test
    void reportsInvalidDeadlineInBothFormats() {
        var json = assertThrows(TaskImportException.class, () -> reader.read(file("tasks.json", """
                [{"name":"A","priority":1},{"name":"B","priority":2,"deadline":"yesterday"}]
                """)));
        assertTrue(json.getMessage().contains("Некорректный JSON"));
        assertTrue(json.getMessage().contains("deadline"));

        var csv = assertThrows(TaskImportException.class, () -> reader.read(file("tasks.csv", """
                name,description,priority,deadline
                A,,1,
                B,,2,yesterday
                """)));
        assertTrue(csv.getMessage().contains("CSV, строка 3"));
        assertTrue(csv.getMessage().contains("deadline: дедлайн должен быть датой ISO 8601 с часовым поясом"));
    }

    @Test
    void stopsAtFirstCsvConversionError() {
        var exception = assertThrows(TaskImportException.class, () -> reader.read(file("tasks.csv", """
                name,description,priority,deadline
                A,,abc,yesterday
                ,,1,
                """)));
        assertEquals("CSV, строка 2: priority: приоритет должен быть целым числом", exception.getMessage());
    }

    @Test
    void reportsNullJsonTaskWithoutNullPointerException() {
        var exception = assertThrows(TaskImportException.class, () -> reader.read(file("tasks.json", """
                [null,{"name":"","priority":11}]
                """)));
        assertTrue(exception.getMessage().contains("JSON[0]: задача не должна быть null"));
        assertTrue(exception.getMessage().contains("JSON[1]: name:"));
        assertTrue(exception.getMessage().contains("JSON[1]: priority:"));
    }

    @Test
    void appliesSameAnnotationsToBothFormats() {
        var json = assertThrows(TaskImportException.class, () -> reader.read(file("tasks.json", """
                [{"name":"","priority":11}]
                """)));
        var csv = assertThrows(TaskImportException.class, () -> reader.read(file("tasks.csv", """
                name,description,priority,deadline
                ,,11,
                """)));
        for (String message : new String[]{"name: Название не должно быть пустым",
                "priority: Приоритет должен быть от 0 до 10"}) {
            assertTrue(json.getMessage().contains(message));
            assertTrue(csv.getMessage().contains(message));
        }
    }

    static MockMultipartFile file(String filename, String content) {
        return new MockMultipartFile("file", filename, "application/octet-stream", content.getBytes(StandardCharsets.UTF_8));
    }
}
