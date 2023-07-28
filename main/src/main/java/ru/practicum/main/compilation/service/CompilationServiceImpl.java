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
import ru.practicum.statisticclient.StatisticClient;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CompilationServiceImpl implements CompilationService {

    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;
    private final StatisticClient statisticClient;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private int getAmountOfViews(LocalDateTime eventPublishedOn, String uri) {
        return statisticClient.getStatistic(
                        eventPublishedOn.format(formatter),
                        LocalDateTime.now().format(formatter),
                        true,
                        new String[]{uri})
                .size();
    }

    @Override
    public CompilationDto createCompilation(NewCompilationDto newCompilationDto) {
        List<Event> eventList = eventRepository.findAllByIdIn(
                newCompilationDto.getEvents() == null ? Collections.emptySet() : newCompilationDto.getEvents());
        Compilation compilation = compilationRepository.save(
                CompilationMapper.compilationFromCreateDto(newCompilationDto, eventList));
        Map<Long, Integer> views = eventList.stream()
                .collect(Collectors.toMap(
                        Event::getId,
                        (event -> getAmountOfViews(event.getPublishedOn(), String.format("/events/%d", event.getId()))
                        )));
        return CompilationMapper.compilationToDto(compilation, views);
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
        Map<Long, Integer> views = compilation.getEvents().stream()
                .collect(Collectors.toMap(
                        Event::getId,
                        (event -> getAmountOfViews(event.getPublishedOn(), String.format("/events/%d", event.getId()))
                        )));
        return CompilationMapper.compilationToDto(updatedCompilation, views);
    }

    @Override
    public void deleteCompilation(Long compId) {
        compilationRepository.delete(getCompilation(compId));
    }

    @Override
    @Transactional(readOnly = true)
    public CompilationDto getCompilationById(Long compId) {
        Compilation compilation = getCompilation(compId);
        Map<Long, Integer> views = compilation.getEvents().stream()
                .collect(Collectors.toMap(
                        Event::getId,
                        (event -> getAmountOfViews(event.getPublishedOn(), String.format("/events/%d", event.getId()))
                        )));
        return CompilationMapper.compilationToDto(compilation, views);
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
                .map(compilation -> CompilationMapper.compilationToDto(compilation, compilation.getEvents()
                        .stream()
                        .collect(Collectors.toMap(
                                Event::getId,
                                (event -> getAmountOfViews(event.getPublishedOn(), String.format("/events/%d", event.getId()))
                                )))))
                .collect(Collectors.toList());
    }

    private Compilation getCompilation(Long compId) {
        return compilationRepository.findById(compId)
                .orElseThrow(() -> new ObjectValidationException(String.format("Подборка с id = %d не найдена", compId)));
    }
}