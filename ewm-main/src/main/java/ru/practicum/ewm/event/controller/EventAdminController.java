package ru.practicum.ewm.event.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.event.dto.EventFullDto;
import ru.practicum.ewm.event.dto.EventParamsAdmin;
import ru.practicum.ewm.event.dto.UpdateEventAdminRequest;
import ru.practicum.ewm.event.service.EventService;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.util.List;

@Validated
@RestController
@RequestMapping(path = "/admin/events")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EventAdminController {

    EventService eventService;

    @GetMapping()
    public List<EventFullDto> getEvents(@Valid EventParamsAdmin eventParamsAdmin) {
        return eventService.getEventsForAdmin(eventParamsAdmin);
    }

    @PatchMapping("/{eventId}")
    public EventFullDto updateEventAddedByCurrentUser(@Valid @RequestBody UpdateEventAdminRequest updateEventRequest,
                                                      @PathVariable @Min(1) Integer eventId) {

        return eventService.updateEventByAdmin(updateEventRequest, eventId);
    }
}
