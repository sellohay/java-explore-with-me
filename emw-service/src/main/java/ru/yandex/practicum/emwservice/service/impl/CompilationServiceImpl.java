package ru.yandex.practicum.emwservice.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.emwservice.dtos.compilation.CompilationDto;
import ru.yandex.practicum.emwservice.dtos.compilation.NewCompilationDto;
import ru.yandex.practicum.emwservice.dtos.compilation.UpdateCompilationRequest;
import ru.yandex.practicum.emwservice.dtos.event.EventShortDto;
import ru.yandex.practicum.emwservice.dtos.mappers.CompilationMapper;
import ru.yandex.practicum.emwservice.exception.NotFoundException;
import ru.yandex.practicum.emwservice.model.Compilation;
import ru.yandex.practicum.emwservice.model.Event;
import ru.yandex.practicum.emwservice.repository.CompilationRepository;
import ru.yandex.practicum.emwservice.service.interfaces.CompilationService;
import ru.yandex.practicum.emwservice.service.interfaces.EventService;

import java.util.*;

@Service
@Transactional(readOnly = true)
public class CompilationServiceImpl implements CompilationService {

    private final CompilationRepository compilationRepository;
    private final EventService eventService;

    public CompilationServiceImpl(CompilationRepository compilationRepository, EventService eventService) {
        this.compilationRepository = compilationRepository;
        this.eventService = eventService;
    }

    @Override
    @Transactional
    public CompilationDto createCompilation(NewCompilationDto newCompilationDto) {

        Set<Event> events = new HashSet<>();
        if (newCompilationDto.getEventIds() != null && !newCompilationDto.getEventIds().isEmpty()) {
            events.addAll(eventService.getByIds(newCompilationDto.getEventIds()));
        }

        Compilation compilation = CompilationMapper.newDtoToCompilation(newCompilationDto, events);
        compilation = compilationRepository.save(compilation);

        List<EventShortDto> eventShortDtos = new ArrayList<>();
        if (!events.isEmpty()) {
            eventShortDtos = eventService.mapToEventShortDtoList(new ArrayList<>(events));
        }
        return CompilationMapper.compilationToDto(compilation, eventShortDtos);
    }

    @Override
    @Transactional
    public void deleteCompilation(Long id) {
        findCompById(id);
        compilationRepository.deleteById(id);
    }

    @Override
    @Transactional
    public CompilationDto updateCompilation(Long id, UpdateCompilationRequest request) {
        Compilation compilation = findCompById(id);
        compilation = CompilationMapper.updateFields(compilation, request);
        List<Long> eventIds = request.getEventIds();
        if (eventIds != null) {
            Set<Event> events = new HashSet<>();
            if (!eventIds.isEmpty()) {
                events.addAll(eventService.getByIds(eventIds));
            }
            compilation.setEvents(events);
        }
        compilation = compilationRepository.save(compilation);
        return convertToDto(compilation);
    }

    @Override
    public CompilationDto getCompilationById(Long compId) {
        Compilation compilation = findCompById(compId);
        return convertToDto(compilation);
    }

    @Override
    public List<CompilationDto> getCompilations(Boolean pinned, int from, int size) {
        List<Compilation> compilations;
        if (pinned != null) {
            compilations = compilationRepository.findCompilationsWithPinnedFilter(pinned, from, size);
        } else {
            compilations = compilationRepository.findCompilationWithFilters(from, size);
        }

        Set<Event> events = new HashSet<>();
        for (Compilation comp : compilations) {
            events.addAll(comp.getEvents());
        }

        Map<Long, EventShortDto> eventShortDtoMap = new HashMap<>();
        if (!events.isEmpty()) {
            List<EventShortDto> shortDtos = eventService.mapToEventShortDtoList(new ArrayList<>(events));
            for (EventShortDto dto : shortDtos) {
                eventShortDtoMap.put(dto.getId(), dto);
            }
        }

        return compilations
                .stream()
                .map(compilation -> {
                    List<EventShortDto> compEventDtos = compilation.getEvents()
                            .stream()
                            .map(event -> eventShortDtoMap.get(event.getId()))
                            .toList();
                    return CompilationMapper.compilationToDto(compilation, compEventDtos);
                })
                .toList();
    }

    private Compilation findCompById(Long id) {
        return compilationRepository.findById(id).orElseThrow(
                () -> new NotFoundException("Compilation with id=" + id + " was not found"));
    }

    private CompilationDto convertToDto(Compilation compilation) {
        List<EventShortDto> eventShortDtos = new ArrayList<>();
        if (!compilation.getEvents().isEmpty()) {
            eventShortDtos = eventService.mapToEventShortDtoList(new ArrayList<>(compilation.getEvents()));
        }
        return CompilationMapper.compilationToDto(compilation, eventShortDtos);

    }
}
