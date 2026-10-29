package com.team.task_manager.controller.tag;

import java.util.List;

import com.team.task_manager.controller.tag.entity.response.TagResponse;
import com.team.task_manager.service.tag.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tags")
@RequiredArgsConstructor
public class TagController {
    private final TagService tagService;

    @GetMapping
    public List<TagResponse> findAll() {
        return tagService.findAll().stream()
                .map(tag -> new TagResponse(tag.id(), tag.name()))
                .toList();
    }
}
