package com.team.task_manager.repository.tag;

import java.util.List;
import java.util.Optional;

import com.team.task_manager.repository.tag.entity.TagDto;

public interface TagRepository {
    List<TagDto> findAll();

    Optional<TagDto> findById(Long tagId);
}
