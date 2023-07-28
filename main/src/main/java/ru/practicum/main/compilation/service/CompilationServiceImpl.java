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
import ru.practicum.statisticdto.ViewStats;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CompilationServiceImpl implements CompilationService {

    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;
    private final StatisticClient statisticClient;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

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
        return CompilationMapper.compilationToDto(compilation, getMapOfViews(minStartTime, uri));
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
        if (updatedCompilation.getEvents().isEmpty()) {
            return CompilationMapper.compilationToDto(compilation, Map.of());
        }
        LocalDateTime minStartTime = getMinTimeFromEventList(updatedCompilation.getEvents());
        String[] uri = updatedCompilation.getEvents().stream().map(event -> "/events/" + event.getId()).toArray(String[]::new);
        return CompilationMapper.compilationToDto(updatedCompilation, getMapOfViews(minStartTime, uri));
    }

    @Override
    public void deleteCompilation(Long compId) {
        compilationRepository.delete(getCompilation(compId));
    }

    @Override
    @Transactional(readOnly = true)
    public CompilationDto getCompilationById(Long compId) {
        Compilation compilation = getCompilation(compId);
        if (compilation.getEvents().isEmpty()) {
            return CompilationMapper.compilationToDto(compilation, Map.of());
        }
        LocalDateTime minStartTime = getMinTimeFromEventList(compilation.getEvents());
        String[] uri = compilation.getEvents().stream().map(event -> "/events/" + event.getId()).toArray(String[]::new);
        return CompilationMapper.compilationToDto(compilation, getMapOfViews(minStartTime, uri));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CompilationDto> getAllCompilations(Boolean pinned, Integer from, Integer size) {
        PageRequest pageRequest = PageRequest.of(from, size);
        Page<Compilation> compilationPage;
        compilationPage = (pinned == null) ?
                compilationRepository.findAll(pageRequest) : compilationRepository.findAllByPinned(pinned, pageRequest);
        return compilationPage.getContent().stream().map(compilation -> {
            if (compilation.getEvents().isEmpty()) {
                return CompilationMapper.compilationToDto(compilation, Map.of());
            }
            LocalDateTime minStartTime = getMinTimeFromEventList(compilation.getEvents());
            String[] uri = compilation.getEvents().stream().map(event -> "/events/" + event.getId()).toArray(String[]::new);
            return CompilationMapper.compilationToDto(compilation, getMapOfViews(minStartTime, uri));
        }).collect(Collectors.toList());
    }

    private Compilation getCompilation(Long compId) {
        return compilationRepository.findById(compId)
                .orElseThrow(() -> new ObjectValidationException(String.format("Подборка с id = %d не найдена", compId)));
    }

    private Map<Long, Integer> getMapOfViews(LocalDateTime eventPublishedOn, String[] uri) {
        List<ViewStats> viewStatsList = statisticClient.getStatistic(
                eventPublishedOn.format(formatter),
                LocalDateTime.now().format(formatter),
                true,
                uri);
        Map<Long, Integer> idToCountMap = new HashMap<>();
        for (ViewStats viewStats : viewStatsList) {
            String viewStatsUri = viewStats.getUri();
            Long id = extractIdFromUri(viewStatsUri);
            idToCountMap.put(id, idToCountMap.getOrDefault(id, 0) + 1);
        }
        return idToCountMap;
    }

    private static Long extractIdFromUri(String uri) {
        int lastSlashIndex = uri.lastIndexOf('/');
        if (lastSlashIndex != -1 && lastSlashIndex < uri.length() - 1) {
            String idString = uri.substring(lastSlashIndex + 1);
            try {
                return Long.parseLong(idString);
            } catch (ObjectValidationException e) {
                throw new ObjectValidationException("Ошибка извлечения id из uri");
            }
        }
        return -1L;
    }

    private LocalDateTime getMinTimeFromEventList(List<Event> events) {
        return events.parallelStream()
                .sorted(Comparator.comparing(Event::getCreatedOn))
                .collect(Collectors.toList()).get(0).getPublishedOn();
    }
}