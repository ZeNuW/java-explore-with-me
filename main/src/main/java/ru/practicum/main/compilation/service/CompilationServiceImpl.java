package ru.practicum.main.compilation.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.main.compilation.dto.NewCompilationDto;
import ru.practicum.main.compilation.dto.CompilationDto;
import ru.practicum.main.compilation.dto.UpdateCompilationRequest;
import ru.practicum.main.compilation.mapper.CompilationMapper;
import ru.practicum.main.compilation.model.Compilation;
import ru.practicum.main.compilation.repository.CompilationRepository;
import ru.practicum.main.event.model.Event;
import ru.practicum.main.event.repository.EventRepository;
import ru.practicum.main.exception.ObjectValidationException;
import ru.practicum.main.util.StatisticsUtil;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CompilationServiceImpl implements CompilationService {

    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;
    private final StatisticsUtil statisticsUtil;

    @Override
    public CompilationDto createCompilation(NewCompilationDto newCompilationDto) {
        List<Event> eventList = eventRepository.findAllByIdIn(
                newCompilationDto.getEvents() == null ? Collections.emptySet() : newCompilationDto.getEvents());
        Compilation compilation = compilationRepository.save(
                CompilationMapper.compilationFromCreateDto(newCompilationDto, eventList));
        if (eventList.isEmpty()) {
            return CompilationMapper.compilationToDto(compilation, Map.of());
        }
        LocalDateTime minStartTime = getMinTimeFromEventList(eventList);
        String[] uri = eventList.stream().map(event -> "/events/" + event.getId()).toArray(String[]::new);
        return CompilationMapper.compilationToDto(compilation, statisticsUtil.getMapOfViews(minStartTime, uri));
    }

    @Override
    public CompilationDto updateCompilation(Long compId, UpdateCompilationRequest updateCompilationRequest) {
        Compilation compilation = getCompilation(compId);
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
        return ifEventListIsEmpty(compilation, updatedCompilation);
    }

    private CompilationDto ifEventListIsEmpty(Compilation compilation, Compilation updatedCompilation) {
        if (updatedCompilation.getEvents().isEmpty()) {
            return CompilationMapper.compilationToDto(compilation, Map.of());
        }
        LocalDateTime minStartTime = getMinTimeFromEventList(updatedCompilation.getEvents());
        String[] uri = updatedCompilation.getEvents().stream().map(event -> "/events/" + event.getId()).toArray(String[]::new);
        return CompilationMapper.compilationToDto(updatedCompilation, statisticsUtil.getMapOfViews(minStartTime, uri));
    }

    @Override
    public void deleteCompilation(Long compId) {
        compilationRepository.delete(getCompilation(compId));
    }

    @Override
    @Transactional(readOnly = true)
    public CompilationDto getCompilationById(Long compId) {
        Compilation compilation = getCompilation(compId);
        return ifEventListIsEmpty(compilation, compilation);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CompilationDto> getAllCompilations(Boolean pinned, Integer from, Integer size) {
        PageRequest pageRequest = PageRequest.of(from, size);
        Page<Compilation> compilationPage;
        compilationPage = (pinned == null) ?
                compilationRepository.findAll(pageRequest) : compilationRepository.findAllByPinned(pinned, pageRequest);
        return compilationPage.getContent()
                .stream()
                .map(compilation -> ifEventListIsEmpty(compilation, compilation))
                .collect(Collectors.toList());
    }

    private Compilation getCompilation(Long compId) {
        return compilationRepository.findById(compId)
                .orElseThrow(() -> new ObjectValidationException(String.format("Подборка с id = %d не найдена", compId)));
    }

    private LocalDateTime getMinTimeFromEventList(List<Event> events) {
        return events.parallelStream()
                .sorted(Comparator.comparing(Event::getCreatedOn))
                .collect(Collectors.toList()).get(0).getPublishedOn();
    }
}