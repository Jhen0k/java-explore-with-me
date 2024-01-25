package ru.practicum.ewm.event.service;

import ru.practicum.ewm.event.dto.EventFullDto;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.dto.NewEventDto;
import ru.practicum.ewm.event.dto.ParamsSearchForAdmin;
import ru.practicum.ewm.event.dto.SearchParamsForEvents;
import ru.practicum.ewm.event.dto.UpdateEventAdminRequest;
import ru.practicum.ewm.event.dto.UpdateEventUserRequest;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.ewm.request.dto.ParticipationRequestDto;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface EventService {

    EventFullDto postEvent(Long userId, NewEventDto newEventDto);

    List<EventShortDto> getEventForOwner(long userId, int from, int size);

    EventFullDto getFullEventForOwner(long userId, long eventId);

    EventFullDto updateEventOwner(long userId, long eventId, UpdateEventUserRequest eventUserRequest);

    List<EventFullDto> getAllEventFromAdmin(ParamsSearchForAdmin params);

    EventFullDto updateEventFromAdmin(long eventId, UpdateEventAdminRequest inputUpdate);

    List<ParticipationRequestDto> getAllParticipationRequestsFromEventByOwner(Long userId, Long eventId);

    EventRequestStatusUpdateResult updateStatusRequest(Long userId, Long eventId, EventRequestStatusUpdateRequest inputUpdate);

    Event checkExistEvent(long eventId);

    List<EventShortDto> getAllEventFromPublic(SearchParamsForEvents searchParamsForEvents, HttpServletRequest request);

    EventFullDto getEventById(Long eventId, HttpServletRequest request);

}
