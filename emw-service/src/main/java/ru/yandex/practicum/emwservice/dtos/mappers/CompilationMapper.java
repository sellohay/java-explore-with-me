package ru.yandex.practicum.emwservice.dtos.mappers;

import ru.yandex.practicum.emwservice.dtos.compilation.CompilationDto;
import ru.yandex.practicum.emwservice.dtos.compilation.NewCompilationDto;
import ru.yandex.practicum.emwservice.dtos.compilation.UpdateCompilationRequest;
import ru.yandex.practicum.emwservice.dtos.event.EventShortDto;
import ru.yandex.practicum.emwservice.model.Compilation;
import ru.yandex.practicum.emwservice.model.Event;

import java.util.List;
import java.util.Set;

public class CompilationMapper {

    public static Compilation newDtoToCompilation(NewCompilationDto dto, Set<Event> events) {
        Compilation compilation = new Compilation();
        compilation.setPinned(dto.getPinned() != null ? dto.getPinned() : false);
        compilation.setTitle(dto.getTitle());
        compilation.setEvents(events);
        return compilation;
    }

    public static CompilationDto compilationToDto(Compilation compilation, List<EventShortDto> events) {
        CompilationDto dto = new CompilationDto();
        dto.setId(compilation.getId());
        dto.setPinned(compilation.getPinned());
        dto.setTitle(compilation.getTitle());
        dto.setEvents(events);
        return dto;
    }

    public static Compilation updateFields(Compilation compilation, UpdateCompilationRequest request) {
        if (request.getPinned() != null) {
            compilation.setPinned(request.getPinned());
        }
        if (request.getTitle() != null) {
            compilation.setTitle(request.getTitle());
        }
        return compilation;
    }
}
