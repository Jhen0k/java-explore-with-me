package ru.practicum.ewm.event.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.event.dto.EventFullDto;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.dto.NewEventDto;
import ru.practicum.ewm.event.dto.UpdateEventUserRequest;
import ru.practicum.ewm.event.service.EventService;
import ru.practicum.ewm.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.ewm.request.dto.ParticipationRequestDto;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.util.List;


@RestController
@RequestMapping(path = "/users/{userId}/events")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EventPrivateController {

    EventService eventService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto createEvent(@Valid @RequestBody NewEventDto newEventDto,
                                    @PathVariable @Min(1) Integer userId) {
        return eventService.createEvent(newEventDto, userId);
    }

    @PatchMapping("/{eventId}")
    public EventFullDto updateEventAddedByCurrentUser(@Valid @RequestBody UpdateEventUserRequest updateEventRequest,
                                                      @PathVariable @Min(1) Integer userId,
                                                      @PathVariable @Min(1) Integer eventId) {

        return eventService.updateEventAddedByCurrentUser(updateEventRequest, userId, eventId);
    }

    @GetMapping
    List<EventShortDto> getAllEventsAddedByCurrentUser(@PathVariable @Min(1) Integer userId,
                                                       @RequestParam(defaultValue = "0") Integer from,
                                                       @RequestParam(defaultValue = "10") Integer size) {

        return eventService.getAllEventsAddedByCurrentUser(userId, from, size);
    }

    @GetMapping("/{eventId}")
    EventFullDto getFullEventAddedByCurrentUser(@PathVariable @Min(1) Integer userId,
                                                @PathVariable @Min(1) Integer eventId) {

        return eventService.getFullEventAddedByCurrentUser(userId, eventId);
    }

    @GetMapping("/{eventId}/requests")
    public List<ParticipationRequestDto> getAllRequestByEventCurrentUser(@PathVariable @Min(1) Integer userId,
                                                                         @PathVariable @Min(1) Integer eventId) {
        return eventService.getAllParticipationRequestsFromEventByOwner(userId, eventId);
    }

    @PatchMapping("/{eventId}/requests")
    public EventRequestStatusUpdateResult updateStatusRequestCurrentUser(@PathVariable @Min(1) Integer userId,
                                                                         @PathVariable @Min(1) Integer eventId,
                                                                         @RequestBody EventRequestStatusUpdateRequest inputUpdate) {
        return eventService.updateStatusRequest(userId, eventId, inputUpdate);
    }
}
