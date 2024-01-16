package ru.practicum.ewm.event.mapper;

import org.mapstruct.Mapper;
import ru.practicum.ewm.category.mapper.CategoryMapper;
import ru.practicum.ewm.event.dto.EventFullDto;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.user.mapper.UserMapper;

@Mapper(componentModel = "spring", uses = {CategoryMapper.class, UserMapper.class})
public interface EventMapper {

    Event toEntityFromFullEvent(EventFullDto eventFullDto);

    EventFullDto toDtoInFullEvent(Event event);

    Event toEntityFromShortEvent(EventShortDto eventShortDto);

    EventShortDto toDtoInShortEvent(Event event);
}
