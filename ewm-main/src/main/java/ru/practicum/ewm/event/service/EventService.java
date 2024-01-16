package ru.practicum.ewm.event.service;

import ru.practicum.ewm.category.dto.CategoryDto;
import ru.practicum.ewm.event.dto.EventFullDto;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.dto.NewEventDto;
import ru.practicum.ewm.event.enums.State;
import ru.practicum.ewm.user.dto.UserShortDto;

import java.time.LocalDateTime;
import java.util.List;

public interface EventService {

    EventFullDto createEvent(NewEventDto newEventDto, Integer userId);

    EventFullDto updateEvent(NewEventDto newEventDto, Integer userId, Integer eventId);

    EventFullDto updateEventInAdmin(NewEventDto newEventDto, Integer eventId);

    List<EventShortDto> getAllEventsAddByCurrentUser(Integer userId, Integer from, Integer size);

    EventFullDto getFullEventAddByCurrentUser(Integer userId, Integer eventId);

    List<EventFullDto> getEventsForAdmin(List<UserShortDto> users, List<State> states, List<CategoryDto> categories,
                                   LocalDateTime rangeStart, LocalDateTime rangeEnd, Integer from, Integer size);

    List<EventFullDto> getEventsBySort(String text, List<State> states, Boolean paid, LocalDateTime rangeStart,
                                 LocalDateTime rangeEnd, Boolean onlyAvailable, String sort, Integer from, Integer size);

    EventFullDto getFullEventById(Integer id);


}
