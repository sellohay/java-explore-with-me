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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
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
    public void deleteCompilation(Long id) {
        findCompById(id);
        compilationRepository.deleteById(id);
    }

    @Override
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
        return compilations
                .stream()
                .map(this::convertToDto)
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
