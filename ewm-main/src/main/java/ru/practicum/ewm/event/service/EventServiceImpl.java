package ru.practicum.ewm.event.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.RequestStatsDto;
import ru.practicum.dto.ResponseStatsDto;
import ru.practicum.ewm.StatClient;
import ru.practicum.ewm.event.dto.CaseUpdatedStatusDto;
import ru.practicum.ewm.event.dto.EventFullDto;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.dto.NewEventDto;
import ru.practicum.ewm.event.dto.ParamsSearchForAdmin;
import ru.practicum.ewm.event.dto.SearchParamsForEvents;
import ru.practicum.ewm.event.dto.UpdateEventAdminRequest;
import ru.practicum.ewm.event.dto.UpdateEventUserRequest;
import ru.practicum.ewm.event.enums.EventAdminState;
import ru.practicum.ewm.event.enums.EventStatus;
import ru.practicum.ewm.event.enums.EventUserState;
import ru.practicum.ewm.event.enums.RequestStatus;
import ru.practicum.ewm.event.mapper.EventMapper;
import ru.practicum.ewm.event.mapper.LocationMapper;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.Location;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.event.repository.LocationRepository;
import ru.practicum.ewm.event.validation.EventValidation;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.exception.ValidationException;
import ru.practicum.ewm.model.Category;
import ru.practicum.ewm.paginator.Paginator;
import ru.practicum.ewm.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.ewm.request.dto.ParticipationRequestDto;
import ru.practicum.ewm.request.mapper.RequestMapper;
import ru.practicum.ewm.request.model.Request;
import ru.practicum.ewm.request.repository.RequestRepository;
import ru.practicum.ewm.user.model.User;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EventServiceImpl implements EventService {

    EventRepository eventRepository;
    EventValidation eventValidation;
    LocationRepository locationRepository;
    LocationMapper locationMapper;
    EventMapper eventMapper;
    RequestRepository requestRepository;
    StatClient statsClient;
    ObjectMapper objectMapper;

    @NonFinal
    @Value("${server.application.name:ewm-service}")
    String applicationName;

    @Transactional
    @Override
    public EventFullDto postEvent(Long userId, NewEventDto newEventDto) {
        LocalDateTime createOn = LocalDateTime.now();
        User user = eventValidation.getUserAfterCheck(userId);
        Category category = eventValidation.getCategoryAfterCheck(newEventDto.getCategory());
        eventValidation.checkValidDataTimeForUser(newEventDto.getEventDate());

        if (newEventDto.getParticipantLimit() == null) {
            newEventDto.setParticipantLimit(0);
        } else if (newEventDto.getParticipantLimit() < 0) {
            throw new ValidationException("Значение ограничения участников не может быть отрицательным");
        }

        Event event = eventMapper.toEvent(newEventDto);
        event.setCategory(category);
        event.setInitiator(user);
        event.setEventStatus(EventStatus.PENDING);
        event.setCreatedDate(createOn);

        if (newEventDto.getLocation() != null) {
            Location location = locationRepository.save(locationMapper.toLocation(newEventDto.getLocation()));
            event.setLocation(location);
        }

        Event eventSaved = eventRepository.save(event);
        EventFullDto eventFullDto = eventMapper.toEventFullDto(eventSaved);
        eventFullDto.setViews(0L);
        eventFullDto.setConfirmedRequests(0);
        return eventFullDto;
    }

    @Transactional(readOnly = true)
    @Override
    public List<EventShortDto> getEventForOwner(long userId, int from, int size) {
        eventValidation.getUserAfterCheck(userId);
        Pageable pageable = Paginator.getPageable(from, size);


        Page<Event> events = eventRepository.findAllByInitiatorIdIs(userId, pageable);

        return events.stream().map(eventMapper::toEventShortDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Override
    public EventFullDto getFullEventForOwner(long userId, long eventId) {
        eventValidation.getUserAfterCheck(userId);
        Event event = eventValidation.getEventAfterCheck(eventId);

        return eventMapper.toEventFullDto(event);
    }

    @Transactional
    @Override
    public EventFullDto updateEventOwner(long userId, long eventId, UpdateEventUserRequest updateEventRequest) {
        eventValidation.getUserAfterCheck(userId);
        Event oldEvent = eventValidation.checkEvenByInitiatorAndEventId(userId, eventId);

        if (oldEvent.getEventStatus().equals(EventStatus.PUBLISHED)) {
            throw new DataIntegrityViolationException("Нельзя изменить уже опубликованное событие");
        }

        Event eventUpdate = eventValidation.updateEventFields(oldEvent, updateEventRequest);

        LocalDateTime newDate = updateEventRequest.getEventDate();
        if (newDate != null) {
            eventValidation.checkValidDataTimeForUser(newDate);
            eventUpdate.setEventDate(newDate);
        }

        EventUserState stateAction = updateEventRequest.getStateAction();
        if (stateAction != null) {
            switch (stateAction) {
                case SEND_TO_REVIEW:
                    eventUpdate.setEventStatus(EventStatus.PENDING);
                    break;
                case CANCEL_REVIEW:
                    eventUpdate.setEventStatus(EventStatus.CANCELED);
                    break;
            }
        }
        return eventMapper.toEventFullDto(eventRepository.save(eventUpdate));
    }

    @Transactional(readOnly = true)
    @Override
    public List<EventFullDto> getAllEventFromAdmin(ParamsSearchForAdmin params) {
        Pageable pageable = Paginator.getPageable(params.getFrom(), params.getSize());
        Specification<Event> specification = Specification.where(null);
        specification = eventValidation.getSpecificationAfterValidation(specification, params);

        Page<Event> events = eventRepository.findAll(specification, pageable);

        List<EventFullDto> result = events.getContent()
                .stream().map(eventMapper::toEventFullDto).collect(Collectors.toList());

        Map<Long, List<Request>> confirmedRequestsCountMap = getConfirmedRequestsCount(events.toList());
        for (EventFullDto event : result) {
            List<Request> requests = confirmedRequestsCountMap.getOrDefault(event.getId(), List.of());
            event.setConfirmedRequests(requests.size());
        }
        return result;
    }

    @Transactional
    @Override
    public EventFullDto updateEventFromAdmin(long eventId, UpdateEventAdminRequest inputEvent) {
        Event eventOld = eventValidation.getEventAfterCheck(eventId);
        eventValidation.checkStatusNotPublished(eventOld.getEventStatus());
        Event eventUpdate = eventValidation.updateEventFields(eventOld, inputEvent);
        LocalDateTime gotEventDate = inputEvent.getEventDate();

        if (gotEventDate != null) {
            if (gotEventDate.isBefore(LocalDateTime.now().plusHours(1))) {
                throw new ValidationException("Некорректные параметры даты.Дата начала изменяемого события должна " +
                        "быть не ранее чем за час от даты публикации.");
            }
            eventUpdate.setEventDate(inputEvent.getEventDate());
        }

        EventAdminState gotAction = inputEvent.getStateAction();
        if (gotAction != null) {
            if (EventAdminState.PUBLISH_EVENT.equals(gotAction)) {
                eventUpdate.setEventStatus(EventStatus.PUBLISHED);
            } else if (EventAdminState.REJECT_EVENT.equals(gotAction)) {
                eventUpdate.setEventStatus(EventStatus.CANCELED);
            }
        }

        return eventMapper.toEventFullDto(eventRepository.save(eventUpdate));
    }

    @Transactional(readOnly = true)
    @Override
    public List<ParticipationRequestDto> getAllParticipationRequestsFromEventByOwner(Long userId, Long eventId) {
        eventValidation.getUserAfterCheck(userId);
        eventValidation.checkEvenByInitiatorAndEventId(userId, eventId);

        List<Request> requests = requestRepository.findAllByEventId(eventId);
        return requests.stream().map(RequestMapper::toParticipationRequestDto).collect(Collectors.toList());
    }

    @Transactional
    @Override
    public EventRequestStatusUpdateResult updateStatusRequest(Long userId, Long eventId, EventRequestStatusUpdateRequest inputUpdate) {
        eventValidation.getUserAfterCheck(userId);
        Event event = eventValidation.checkEvenByInitiatorAndEventId(userId, eventId);


        if (!event.isRequestModeration() || event.getParticipantLimit() == 0) {
            throw new ValidationException("Это событие не требует подтверждения запросов");
        }

        RequestStatus status = inputUpdate.getStatus();

        int confirmedRequestsCount = requestRepository.countByEventIdAndStatus(event.getId(), RequestStatus.CONFIRMED);
        switch (status) {
            case CONFIRMED:
                CaseUpdatedStatusDto updatedStatusConfirmed = eventValidation.getUpdateStatus(event.getParticipantLimit(),
                        confirmedRequestsCount, event, inputUpdate, RequestStatus.CONFIRMED);

                List<Request> confirmedRequests = requestRepository.findAllById(updatedStatusConfirmed.getProcessedIds());

                List<Request> rejectedRequests = new ArrayList<>();
                if (!updatedStatusConfirmed.getIdsFromUpdateStatus().isEmpty()) {
                    List<Long> ids = updatedStatusConfirmed.getIdsFromUpdateStatus();
                    rejectedRequests = eventValidation.rejectRequest(ids, eventId);
                }

                return EventRequestStatusUpdateResult.builder()
                        .confirmedRequests(confirmedRequests
                                .stream()
                                .map(RequestMapper::toParticipationRequestDto).collect(Collectors.toList()))
                        .rejectedRequests(rejectedRequests
                                .stream()
                                .map(RequestMapper::toParticipationRequestDto).collect(Collectors.toList()))
                        .build();
            case REJECTED:
                final CaseUpdatedStatusDto updatedStatusReject = eventValidation.getUpdateStatus(event.getParticipantLimit(),
                        confirmedRequestsCount, event, inputUpdate, RequestStatus.REJECTED);

                List<Request> rejectRequest = requestRepository.findAllById(updatedStatusReject.getProcessedIds());

                return EventRequestStatusUpdateResult.builder()
                        .rejectedRequests(rejectRequest
                                .stream()
                                .map(RequestMapper::toParticipationRequestDto).collect(Collectors.toList()))
                        .build();
            default:
                throw new ValidationException("Некорректный статус - " + status);
        }
    }

    @Transactional(readOnly = true)
    @Override
    public Event checkExistEvent(long eventId) {
        Optional<Event> event = eventRepository.findById(eventId);

        if (event.isEmpty()) {
            throw new NotFoundException("Event with id=" + eventId + "was not found");
        } else {
            return event.get();
        }
    }

    @Transactional(readOnly = true)
    @Override
    public List<EventShortDto> getAllEventFromPublic(SearchParamsForEvents searchParamsForEvents, HttpServletRequest request) {

        if (searchParamsForEvents.getRangeEnd() != null && searchParamsForEvents.getRangeStart() != null) {
            if (searchParamsForEvents.getRangeEnd().isBefore(searchParamsForEvents.getRangeStart())) {
                throw new ValidationException("Дата окончания не может быть раньше даты начала");
            }
        }

        addStatsClient(request);

        Pageable pageable = PageRequest.of(searchParamsForEvents.getFrom() / searchParamsForEvents.getSize(), searchParamsForEvents.getSize());

        Specification<Event> specification = Specification.where(null);
        LocalDateTime now = LocalDateTime.now();

        if (searchParamsForEvents.getText() != null) {
            String searchText = searchParamsForEvents.getText().toLowerCase();
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.or(
                            criteriaBuilder.like(criteriaBuilder.lower(root.get("annotation")), "%" + searchText + "%"),
                            criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), "%" + searchText + "%")
                    ));
        }

        if (searchParamsForEvents.getCategories() != null && !searchParamsForEvents.getCategories().isEmpty()) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    root.get("category").get("id").in(searchParamsForEvents.getCategories()));
        }

        LocalDateTime startDateTime = Objects.requireNonNullElse(searchParamsForEvents.getRangeStart(), now);
        specification = specification.and((root, query, criteriaBuilder) ->
                criteriaBuilder.greaterThan(root.get("eventDate"), startDateTime));

        if (searchParamsForEvents.getRangeEnd() != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.lessThan(root.get("eventDate"), searchParamsForEvents.getRangeEnd()));
        }

        if (searchParamsForEvents.getOnlyAvailable() != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.greaterThanOrEqualTo(root.get("participantLimit"), 0));
        }

        specification = specification.and((root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("eventStatus"), EventStatus.PUBLISHED));

        List<Event> resultEvents = eventRepository.findAll(specification, pageable).getContent();
        List<EventShortDto> result = resultEvents
                .stream().map(eventMapper::toEventShortDto).collect(Collectors.toList());
        Map<Long, Long> viewStatsMap = getViewsAllEvents(resultEvents);

        for (EventShortDto event : result) {
            Long viewsFromMap = viewStatsMap.getOrDefault(event.getId(), 0L);
            event.setViews(viewsFromMap);
        }

        return result;
    }

    @Transactional(readOnly = true)
    @Override
    public EventFullDto getEventById(Long eventId, HttpServletRequest request) {
        Event event = eventValidation.getEventAfterCheck(eventId);
        eventValidation.checkStatusPublished(event.getEventStatus(), eventId);

        addStatsClient(request);
        EventFullDto eventFullDto = eventMapper.toEventFullDto(event);
        Map<Long, Long> viewStatsMap = getViewsAllEvents(List.of(event));
        Long views = viewStatsMap.getOrDefault(event.getId(), 0L);
        eventFullDto.setViews(views);

        return eventFullDto;
    }

    private Map<Long, Long> getViewsAllEvents(List<Event> events) {
        List<String> uris = events.stream()
                .map(event -> String.format("/events/%s", event.getId()))
                .collect(Collectors.toList());

        List<LocalDateTime> startDates = events.stream()
                .map(Event::getCreatedDate)
                .collect(Collectors.toList());
        LocalDateTime earliestDate = startDates.stream()
                .min(LocalDateTime::compareTo)
                .orElse(null);
        Map<Long, Long> viewStatsMap = new HashMap<>();

        if (earliestDate != null) {
            ResponseEntity<Object> response = statsClient.getStats(earliestDate, LocalDateTime.now(), uris, true);

            List<ResponseStatsDto> viewStatsList = objectMapper.convertValue(response.getBody(), new TypeReference<>() {
            });

            viewStatsMap = viewStatsList.stream()
                    .filter(responseStatsDto -> responseStatsDto.getUri().startsWith("/events/"))
                    .collect(Collectors.toMap(
                            responseStatsDto -> Long.parseLong(responseStatsDto.getUri().substring("/events/".length())),
                            ResponseStatsDto::getHits
                    ));
        }
        return viewStatsMap;
    }

    private void addStatsClient(HttpServletRequest request) {
        statsClient.postStat(RequestStatsDto.builder()
                .app(applicationName)
                .uri(request.getRequestURI())
                .ip(request.getRemoteAddr())
                .timestamp(LocalDateTime.now())
                .build());
    }

    private Map<Long, List<Request>> getConfirmedRequestsCount(List<Event> events) {
        List<Request> requests = requestRepository.findAllByEventIdInAndStatus(events.stream()
                        .map(Event::getId)
                        .collect(Collectors.toList()),
                RequestStatus.CONFIRMED);
        return requests.stream().collect(Collectors.groupingBy(r -> r.getEvent().getId()));
    }
}

