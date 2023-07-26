package ru.practicum.main.compilation.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.main.compilation.dto.NewCompilationDto;
import ru.practicum.main.compilation.dto.CompilationDto;
import ru.practicum.main.compilation.dto.UpdateCompilationRequest;
import ru.practicum.main.compilation.mapper.CompilationMapper;
import ru.practicum.main.compilation.model.Compilation;
import ru.practicum.main.compilation.repository.CompilationRepository;
import ru.practicum.main.event.model.Event;
import ru.practicum.main.event.repository.EventRepository;
import ru.practicum.main.exception.ObjectValidationException;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CompilationServiceImpl implements CompilationService {

    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;

    public CompilationDto createCompilation(NewCompilationDto newCompilationDto) {
        List<Event> eventList = eventRepository.findAllByIdIn(
                newCompilationDto.getEvents() == null ? Collections.emptySet() : newCompilationDto.getEvents());
        Compilation compilation = compilationRepository.save(
                CompilationMapper.compilationFromCreateDto(newCompilationDto, eventList));
        return CompilationMapper.compilationToDto(compilation);
    }

    public CompilationDto updateCompilation(Long compId, UpdateCompilationRequest updateCompilationRequest) {
        Compilation compilation = checkCompilation(compId);
        if (updateCompilationRequest.getTitle() != null) {
            compilation.setTitle(updateCompilationRequest.getTitle());
        }
        if (updateCompilationRequest.getEvents() != null) {
            List<Event> eventList = eventRepository.findAllByIdIn(updateCompilationRequest.getEvents());
            compilation.setEvents(eventList);
        }
        if (updateCompilationRequest.getPinned() != null) {
            compilation.setPinned(updateCompilationRequest.getPinned());
        }
        Compilation updatedCompilation = compilationRepository.save(compilation);
        return CompilationMapper.compilationToDto(updatedCompilation);
    }

    public void deleteCompilation(Long compId) {
        compilationRepository.delete(checkCompilation(compId));
    }

    @Override
    public CompilationDto getCompilation(Long compId) {
        return CompilationMapper.compilationToDto(checkCompilation(compId));
    }

    @Override
    public List<CompilationDto> getAllCompilations(Boolean pinned, Integer from, Integer size) {
        PageRequest pageRequest = PageRequest.of(from, size);
        Page<Compilation> compilationPage;
        compilationPage = (pinned == null) ?
                compilationRepository.findAll(pageRequest) : compilationRepository.findAllByPinned(pinned, pageRequest);
        return CompilationMapper.compilationToDto(compilationPage.getContent());
    }

    private Compilation checkCompilation(Long compId) {
        return compilationRepository.findById(compId)
                .orElseThrow(() -> new ObjectValidationException(String.format("Подборка с id = %d не найдена", compId)));
    }
}