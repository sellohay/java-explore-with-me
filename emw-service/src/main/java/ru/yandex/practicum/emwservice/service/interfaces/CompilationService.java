package ru.yandex.practicum.emwservice.service.interfaces;

import ru.yandex.practicum.emwservice.dtos.compilation.CompilationDto;
import ru.yandex.practicum.emwservice.dtos.compilation.NewCompilationDto;
import ru.yandex.practicum.emwservice.dtos.compilation.UpdateCompilationRequest;

import java.util.List;

public interface CompilationService {
    CompilationDto createCompilation(NewCompilationDto newCompilationDto);

    void deleteCompilation(Long id);

    CompilationDto updateCompilation(Long id, UpdateCompilationRequest request);

    CompilationDto getCompilationById(Long compId);

    List<CompilationDto> getCompilations(Boolean pinned, int from, int size);
}
