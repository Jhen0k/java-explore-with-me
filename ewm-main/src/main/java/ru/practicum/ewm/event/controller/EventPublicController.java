package ru.practicum.ewm.event.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.event.dto.EventFullDto;
import ru.practicum.ewm.event.dto.EventParams;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.dto.SearchParamsForEvents;
import ru.practicum.ewm.event.service.EventService;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@Validated
@RequestMapping(path = "/events")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EventPublicController {

    EventService eventService;

    @GetMapping
    public List<EventShortDto> getAllEvents(@Valid SearchParamsForEvents searchParamsForEvents,
                                            HttpServletRequest request) {
        return eventService.getAllEventFromPublic(searchParamsForEvents, request);
    }

    @GetMapping("/{eventId}")
    public EventFullDto getEventById(@PathVariable(value = "eventId") Long eventId,
                                     HttpServletRequest request) {
        return eventService.getEventById(eventId, request);
    }
}
