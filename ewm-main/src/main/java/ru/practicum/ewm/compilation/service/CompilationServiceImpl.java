package ru.practicum.ewm.compilation.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.compilation.dto.CompilationDto;
import ru.practicum.ewm.compilation.dto.NewCompilationDto;
import ru.practicum.ewm.compilation.dto.UpdateCompilationDto;
import ru.practicum.ewm.compilation.mapper.CompilationMapper;
import ru.practicum.ewm.compilation.model.Compilation;
import ru.practicum.ewm.compilation.repository.CompilationRepository;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.exception.ValidationException;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CompilationServiceImpl implements CompilationService {

    CompilationRepository compilationRepository;
    EventRepository eventRepository;
    CompilationMapper compilationMapper;

    @Transactional
    @Override
    public CompilationDto addCompilation(NewCompilationDto compilationDto) {
        Compilation compilation = compilationMapper.toEntity(compilationDto);
        compilation.setPinned(Optional.ofNullable(compilation.getPinned()).orElse(false));

        Set<Integer> compEventIds = (compilationDto.getEvents() != null) ? compilationDto.getEvents() : Collections.emptySet();
        List<Integer> eventIds = new ArrayList<>(compEventIds);
        List<Event> events = eventRepository.findAllByIdIn(eventIds);
        Set<Event> eventsSet = new HashSet<>(events);
        compilation.setEvents(eventsSet);

        Compilation compilationAfterSave = compilationRepository.save(compilation);
        return compilationMapper.toDto(compilationAfterSave);
    }

    @Transactional
    @Override
    public CompilationDto updateCompilation(Integer compId, UpdateCompilationDto update) {
        Compilation compilation = checkCompilation(compId);

        Set<Integer> eventIds = update.getEvents();

        if (eventIds != null) {
            List<Event> events = eventRepository.findAllByIdIn(new ArrayList<>(eventIds));
            Set<Event> eventSet = new HashSet<>(events);
            compilation.setEvents(eventSet);
        }

        compilation.setPinned(Optional.ofNullable(update.getPinned()).orElse(compilation.getPinned()));
        if (compilation.getTitle().isBlank()) {
            throw new ValidationException("Title не может состоять из пробелов");
        }
        compilation.setTitle(Optional.ofNullable(update.getTitle()).orElse(compilation.getTitle()));

        return compilationMapper.toDto(compilation);
    }

    @Transactional
    @Override
    public void deleteCompilation(Integer compId) {
        checkCompilation(compId);
        compilationRepository.deleteById(compId);
    }

    @Override
    public List<CompilationDto> getCompilations(Boolean pinned, Integer from, Integer size) {

        PageRequest pageRequest = PageRequest.of(from, size);
        List<Compilation> compilations;
        if (pinned == null) {
            compilations = compilationRepository.findAll(pageRequest).getContent();
        } else {
            compilations = compilationRepository.findAllByPinned(pinned, pageRequest);
        }

        return compilations.stream()
                .map(compilationMapper::toDto)
                .collect(Collectors.toList());
    }


    @Override
    public CompilationDto findByIdCompilation(Integer compId) {
        return compilationMapper.toDto(checkCompilation(compId));
    }

    private Compilation checkCompilation(Integer compId) {
        return compilationRepository.findById(compId).orElseThrow(
                () -> new NotFoundException("Compilation с id = " + compId + " не найден"));
    }
}
