package com.team.task_manager.repository.tag;

import java.util.List;
import java.util.Optional;

import com.team.task_manager.repository.tag.entity.TagDto;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import static com.team.task_manager.jooq.generated.Tables.TAGS;

@Repository
@RequiredArgsConstructor
public class JooqTagRepository implements TagRepository {
    private final DSLContext dsl;

    @Override
    public List<TagDto> findAll() {
        return dsl.select(TAGS.ID, TAGS.NAME)
                .from(TAGS)
                .orderBy(TAGS.ID)
                .fetch(record -> new TagDto(record.get(TAGS.ID), record.get(TAGS.NAME)));
    }

    @Override
    public Optional<TagDto> findById(Long tagId) {
        return dsl.select(TAGS.ID, TAGS.NAME)
                .from(TAGS)
                .where(TAGS.ID.eq(tagId))
                .fetchOptional(record -> new TagDto(record.get(TAGS.ID), record.get(TAGS.NAME)));
    }
}
