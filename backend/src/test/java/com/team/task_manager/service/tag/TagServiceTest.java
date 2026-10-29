package com.team.task_manager.service.tag;

import java.util.List;
import java.util.Optional;

import com.team.task_manager.controller.error.exception.tag.TagNotFoundException;
import com.team.task_manager.repository.tag.TagRepository;
import com.team.task_manager.repository.tag.entity.TagDto;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TagServiceTest {
    @Test
    void returnsAllAvailableTags() {
        List<TagDto> tags = List.of(
                new TagDto(1L, "Работа"),
                new TagDto(2L, "Учёба")
        );
        TagRepository repository = new TagRepository() {
            @Override
            public List<TagDto> findAll() {
                return tags;
            }

            @Override
            public Optional<TagDto> findById(Long tagId) {
                return Optional.empty();
            }
        };

        assertEquals(tags, new TagService(repository).findAll());
    }

    @Test
    void returnsTagById() {
        TagDto tag = new TagDto(1L, "Работа");
        TagRepository repository = repositoryWith(tag);

        assertSame(tag, new TagService(repository).findById(1L));
    }

    @Test
    void throwsWhenTagIsMissing() {
        TagRepository repository = repositoryWith(null);

        assertThrows(TagNotFoundException.class, () -> new TagService(repository).findById(1L));
    }

    private static TagRepository repositoryWith(TagDto tag) {
        return new TagRepository() {
            @Override
            public List<TagDto> findAll() {
                return tag == null ? List.of() : List.of(tag);
            }

            @Override
            public Optional<TagDto> findById(Long tagId) {
                return Optional.ofNullable(tag);
            }
        };
    }
}
