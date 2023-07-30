package ru.practicum.main.compilation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.practicum.main.event.dto.EventShort;

import java.util.List;

@Data
@AllArgsConstructor
public class CompilationDto {
    private Long id;
    private List<EventShort> events;
    private Boolean pinned;
    private String title;
}