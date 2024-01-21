package ru.practicum.ewm.event.service;

import ru.practicum.ewm.event.dto.EventFullDto;
import ru.practicum.ewm.event.dto.EventParams;
import ru.practicum.ewm.event.dto.EventParamsAdmin;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.dto.NewEventDto;
import ru.practicum.ewm.event.dto.UpdateEventAdminRequest;
import ru.practicum.ewm.event.dto.UpdateEventUserRequest;
import ru.practicum.ewm.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.ewm.request.dto.ParticipationRequestDto;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface EventService {

    EventFullDto createEvent(NewEventDto newEventDto, Integer userId);

    EventFullDto updateEventAddedByCurrentUser(UpdateEventUserRequest updateEventRequest, Integer userId, Integer eventId);

    EventFullDto updateEventByAdmin(UpdateEventAdminRequest updateEventRequest, Integer eventId);

    List<EventShortDto> getAllEventsAddedByCurrentUser(Integer userId, Integer from, Integer size);

    EventFullDto getFullEventAddedByCurrentUser(Integer userId, Integer eventId);

    List<EventFullDto> getEventsForAdmin(EventParamsAdmin eventParamsAdmin);

    List<ParticipationRequestDto> getAllParticipationRequestsFromEventByOwner(Integer userId, Integer eventId);

    EventRequestStatusUpdateResult updateStatusRequest(Integer userId, Integer eventId, EventRequestStatusUpdateRequest inputUpdate);

    List<EventShortDto> getAllEventFromPublic(EventParams searchEventParams, HttpServletRequest request);

    EventFullDto getEventById(Integer eventId, HttpServletRequest request);
}
