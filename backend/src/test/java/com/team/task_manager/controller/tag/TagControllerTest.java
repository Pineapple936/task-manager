package com.team.task_manager.controller.tag;

import java.util.List;

import com.team.task_manager.repository.tag.entity.TagDto;
import com.team.task_manager.service.tag.TagService;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TagControllerTest {
    @Test
    void returnsAllAvailableTags() throws Exception {
        TagService service = mock(TagService.class);
        when(service.findAll()).thenReturn(List.of(
                new TagDto(1L, "Работа"),
                new TagDto(2L, "Учёба")
        ));
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new TagController(service)).build();

        mockMvc.perform(get("/tags"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Работа"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("Учёба"));

        verify(service).findAll();
    }
}
