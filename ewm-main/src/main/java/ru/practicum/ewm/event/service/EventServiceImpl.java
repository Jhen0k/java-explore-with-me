package ru.practicum.ewm.event.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.category.dto.CategoryDto;
import ru.practicum.ewm.event.dto.EventFullDto;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.dto.NewEventDto;
import ru.practicum.ewm.event.enums.State;
import ru.practicum.ewm.event.mapper.AbstractEventMapper;
import ru.practicum.ewm.event.mapper.EventMapper;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.user.dto.UserShortDto;
import ru.practicum.ewm.user.mapper.UserMapper;
import ru.practicum.ewm.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EventServiceImpl implements EventService {

    EventRepository eventRepository;
    UserRepository userRepository;
    EventMapper eventMapper;
    AbstractEventMapper abstractEventMapper;
    UserMapper userMapper;

    @Override
    public EventFullDto createEvent(NewEventDto newEventDto, Integer userId) {
        userRepository.existsById(userId);
        UserShortDto userShortDto = userMapper.toShortDto(userRepository.findById(userId).orElseThrow());
        EventFullDto eventFullDto = abstractEventMapper.toDtoInNewEvent(newEventDto);
        eventFullDto.setInitiator(userShortDto);

        return eventMapper.toDtoInFullEvent(eventRepository.save(eventMapper.toEntityFromFullEvent(eventFullDto)));
    }

    @Override
    public EventFullDto updateEvent(NewEventDto newEventDto, Integer userId, Integer eventId) {
        return null;
    }

    @Override
    public EventFullDto updateEventInAdmin(NewEventDto newEventDto, Integer eventId) {
        return null;
    }

    @Override
    public List<EventShortDto> getAllEventsAddByCurrentUser(Integer userId, Integer from, Integer size) {
        return null;
    }

    @Override
    public EventFullDto getFullEventAddByCurrentUser(Integer userId, Integer eventId) {
        return null;
    }

    @Override
    public List<EventFullDto> getEventsForAdmin(List<UserShortDto> users, List<State> states, List<CategoryDto> categories, LocalDateTime rangeStart, LocalDateTime rangeEnd, Integer from, Integer size) {
        return null;
    }

    @Override
    public List<EventFullDto> getEventsBySort(String text, List<State> states, Boolean paid, LocalDateTime rangeStart, LocalDateTime rangeEnd, Boolean onlyAvailable, String sort, Integer from, Integer size) {
        return null;
    }

    @Override
    public EventFullDto getFullEventById(Integer id) {
        return null;
    }
}
