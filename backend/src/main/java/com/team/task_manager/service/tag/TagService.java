package com.team.task_manager.service.tag;

import java.util.List;

import com.team.task_manager.controller.error.exception.tag.TagNotFoundException;
import com.team.task_manager.repository.tag.TagRepository;
import com.team.task_manager.repository.tag.entity.TagDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TagService {
    private final TagRepository tagRepository;

    @Transactional(readOnly = true)
    public List<TagDto> findAll() {
        return tagRepository.findAll();
    }

    @Transactional(readOnly = true)
    public TagDto findById(Long tagId) {
        return tagRepository.findById(tagId)
                .orElseThrow(() -> new TagNotFoundException(tagId));
    }
}
