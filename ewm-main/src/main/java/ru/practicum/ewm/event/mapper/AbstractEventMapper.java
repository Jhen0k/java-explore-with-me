package ru.practicum.ewm.event.mapper;

import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import ru.practicum.ewm.category.dto.CategoryDto;
import ru.practicum.ewm.category.mapper.CategoryMapper;
import ru.practicum.ewm.category.repository.CategoryRepository;
import ru.practicum.ewm.event.dto.EventFullDto;
import ru.practicum.ewm.event.dto.NewEventDto;
import ru.practicum.ewm.event.enums.State;
import ru.practicum.ewm.event.repository.EventRepository;

@Mapper(componentModel = "spring")
public abstract class AbstractEventMapper {

    @Autowired
    protected EventRepository eventRepository;
    @Autowired
    protected CategoryRepository categoryRepository;
    @Autowired
    protected CategoryMapper categoryMapper;

    protected LocationMapper locationMapper;

    public EventFullDto toDtoInNewEvent(NewEventDto event) {
        categoryRepository.existsById(event.getCategory());
        CategoryDto categoryDto = categoryMapper.toDto(categoryRepository.findById(event.getCategory()).orElseThrow());


        return EventFullDto.builder()
                .annotation(event.getAnnotation())
                .category(categoryDto)
                .description(event.getDescription())
                .eventDate(event.getEventDate())
                .location(event.getLocation())
                .paid(event.getPaid())
                .participantLimit(event.getParticipantLimit())
                .requestModeration(event.getRequestModeration())
                .state(State.PENDING)
                .title(event.getTitle())
                .build();
    }
}
