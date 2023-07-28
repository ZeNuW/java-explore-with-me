package ru.practicum.main.compilation.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.main.compilation.dto.NewCompilationDto;
import ru.practicum.main.compilation.dto.CompilationDto;
import ru.practicum.main.compilation.model.Compilation;
import ru.practicum.main.event.mapper.EventMapper;
import ru.practicum.main.event.model.Event;

import java.util.List;
import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CompilationMapper {

    public static CompilationDto compilationToDto(Compilation compilation, Map<Long, Integer> views) {
        return new CompilationDto(compilation.getId(),
                EventMapper.eventToShort(compilation.getEvents(), views),
                compilation.getPinned(),
                compilation.getTitle()
        );
    }

    public static Compilation compilationFromCreateDto(NewCompilationDto newCompilationDto, List<Event> eventList) {
        return new Compilation(
                null,
                eventList,
                newCompilationDto.getPinned(),
                newCompilationDto.getTitle()
        );
    }
}