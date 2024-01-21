package ru.practicum.ewm.event.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
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
import ru.practicum.ewm.event.dto.EventParams;
import ru.practicum.ewm.event.dto.EventParamsAdmin;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.dto.NewEventDto;
import ru.practicum.ewm.event.dto.UpdateEventAdminRequest;
import ru.practicum.ewm.event.dto.UpdateEventUserRequest;
import ru.practicum.ewm.event.enums.AdminState;
import ru.practicum.ewm.event.enums.State;
import ru.practicum.ewm.event.enums.UserState;
import ru.practicum.ewm.event.mapper.AbstractEventMapper;
import ru.practicum.ewm.event.mapper.EventMapper;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.event.validation.EventValidation;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.exception.ValidationException;
import ru.practicum.ewm.paginator.Paginator;
import ru.practicum.ewm.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.ewm.request.dto.ParticipationRequestDto;
import ru.practicum.ewm.request.enums.RequestStatus;
import ru.practicum.ewm.request.mapper.RequestMapper;
import ru.practicum.ewm.request.model.Request;
import ru.practicum.ewm.request.repository.RequestRepository;
import ru.practicum.ewm.user.dto.UserShortDto;
import ru.practicum.ewm.user.mapper.UserMapper;
import ru.practicum.ewm.user.repository.UserRepository;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EventServiceImpl implements EventService {

    EventRepository eventRepository;
    UserRepository userRepository;
    EventMapper eventMapper;
    AbstractEventMapper abstractEventMapper;
    UserMapper userMapper;
    EventValidation eventValidation;
    RequestRepository requestRepository;
    RequestMapper requestMapper;
    StatClient statsClient;
    ObjectMapper objectMapper;

    @NonFinal
    @Value("${server.application.name:ewm-service}")
    String applicationName;

    @Transactional
    @Override
    public EventFullDto createEvent(NewEventDto newEventDto, Integer userId) {
        LocalDateTime createdOn = LocalDateTime.now();
        eventValidation.existUser(userId);
        eventValidation.checkValidDataTimeForUser(newEventDto.getEventDate());
        UserShortDto userShortDto = userMapper.toShortDto(userRepository.findById(userId).orElseThrow());
        EventFullDto eventFullDto = abstractEventMapper.toDtoInNewEvent(newEventDto);
        eventFullDto.setInitiator(userShortDto);
        eventFullDto.setCreatedOn(createdOn);

        return eventMapper.toDtoInFullEvent(eventRepository.save(eventMapper.toEntityFromFullEvent(eventFullDto)));
    }

    @Transactional
    @Override
    public EventFullDto updateEventAddedByCurrentUser(UpdateEventUserRequest updateEventRequest, Integer userId, Integer eventId) {
        Event oldEvent = eventValidation.getEventCheckByInitiatorAndEventId(userId, eventId);
        if (oldEvent.getState().equals(State.PUBLISHED)) {
            throw new ConflictException("Статус события не может быть обновлен, так как со статусом PUBLISHED");
        }
        eventValidation.checkUserByOwnerEvent(oldEvent, userId);
        Event eventUpdate = eventValidation.getEventValidatedFields(oldEvent, updateEventRequest);

        boolean hasChange = false;

        if (eventUpdate == null) {
            eventUpdate = oldEvent;
        } else {
            hasChange = true;
        }

        if (updateEventRequest.getEventDate() != null) {
            eventUpdate.setEventDate(updateEventRequest.getEventDate());
        }

        UserState userState = updateEventRequest.getStateAction();
        if (userState != null) {
            switch (userState) {
                case SEND_TO_REVIEW:
                    eventUpdate.setState(State.PENDING);
                    hasChange = true;
                    break;
                case CANCEL_REVIEW:
                    eventUpdate.setState(State.CANCELED);
                    hasChange = true;
                    break;
            }
        }

        Event newEvent = null;
        if (hasChange) {
            newEvent = eventRepository.save(eventUpdate);
        }

        return newEvent != null ? eventMapper.toDtoInFullEvent(newEvent) : null;
    }

    @Override
    public EventFullDto updateEventByAdmin(UpdateEventAdminRequest updateEventRequest, Integer eventId) {
        Event oldEvent = eventValidation.existEvent(eventId);
        eventValidation.checkAllStatus(oldEvent.getState());
        Event eventUpdate = eventValidation.getEventValidatedFields(oldEvent, updateEventRequest);

        boolean hasChange = false;
        if (eventUpdate == null) {
            eventUpdate = oldEvent;
        } else {
            hasChange = true;
        }

        AdminState adminState = updateEventRequest.getStateAction();
        if (adminState != null) {
            if (AdminState.PUBLISH_EVENT.equals(adminState)) {
                eventUpdate.setState(State.PUBLISHED);
                hasChange = true;
            } else if (AdminState.REJECT_EVENT.equals(adminState)) {
                eventUpdate.setState(State.CANCELED);
                hasChange = true;
            }
        }

        Event newEvent = null;
        if (hasChange) {
            newEvent = eventRepository.save(eventUpdate);
        }
        return newEvent != null ? eventMapper.toDtoInFullEvent(newEvent) : null;
    }

    @Override
    public List<EventShortDto> getAllEventsAddedByCurrentUser(Integer userId, Integer from, Integer size) {
        eventValidation.existUser(userId);
        Pageable pageable = Paginator.getPageable(from, size, "id");

        return eventMapper.toEventShortDtoList(eventRepository.findAllByAndInitiatorId(userId, pageable));
    }

    @Override
    public EventFullDto getFullEventAddedByCurrentUser(Integer userId, Integer eventId) {
        return eventMapper.toDtoInFullEvent(eventValidation.getEventCheckByInitiatorAndEventId(userId, eventId));
    }

    @Override
    public List<EventFullDto> getEventsForAdmin(EventParamsAdmin eventParamsAdmin) {
        Pageable pageable = Paginator.getPageable(eventParamsAdmin.getFrom(), eventParamsAdmin.getSize());
        Specification<Event> specification = eventValidation.checkEventParamsAdmin(eventParamsAdmin);
        List<Event> events = eventRepository.findAll(specification, pageable).toList();
        List<EventFullDto> eventsFullDto = eventMapper.toEventFullDtoList(events);
        Map<Integer, List<Request>> requestsCountMap = getConfirmedRequestsCount(events);

        for (EventFullDto event : eventsFullDto) {
            List<Request> requests = requestsCountMap.getOrDefault(event.getId(), List.of());
            event.setConfirmedRequests(requests.size());
        }
        return eventsFullDto;
    }

    @Override
    public List<ParticipationRequestDto> getAllParticipationRequestsFromEventByOwner(Integer userId, Integer eventId) {
        eventValidation.getEventCheckByInitiatorAndEventId(userId, eventId);
        List<Request> requests = requestRepository.findAllByEventId(eventId);
        return requests.stream().map(requestMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public EventRequestStatusUpdateResult updateStatusRequest(Integer userId, Integer eventId, EventRequestStatusUpdateRequest inputUpdate) {
        Event event = eventValidation.getEventCheckByInitiatorAndEventId(userId, eventId);

        eventValidation.checkEventRequest(event);
        RequestStatus status = inputUpdate.getStatus();
        int confirmedRequestsCount = requestRepository.countByEventIdAndStatus(event.getId(), RequestStatus.CONFIRMED);

        switch (status) {
            case CONFIRMED:
                if (event.getParticipantLimit() == confirmedRequestsCount) {
                    throw new ConflictException("Лимит участников исчерпан");
                }
                CaseUpdatedStatusDto updatedStatusConfirmed = updatedStatusConfirmed(event,
                        CaseUpdatedStatusDto.builder()
                                .idsFromUpdateStatus(new ArrayList<>(inputUpdate.getRequestIds())).build(),
                        RequestStatus.CONFIRMED, confirmedRequestsCount);

                List<Request> confirmedRequests = requestRepository.findAllById(updatedStatusConfirmed.getProcessedIds());
                List<Request> rejectedRequests = new ArrayList<>();
                if (!updatedStatusConfirmed.getIdsFromUpdateStatus().isEmpty()) {
                    List<Integer> ids = updatedStatusConfirmed.getIdsFromUpdateStatus();
                    rejectedRequests = rejectRequest(ids, eventId);
                }

                return EventRequestStatusUpdateResult.builder()
                        .confirmedRequests(confirmedRequests
                                .stream()
                                .map(requestMapper::toDto).collect(Collectors.toList()))
                        .rejectedRequests(rejectedRequests
                                .stream()
                                .map(requestMapper::toDto).collect(Collectors.toList()))
                        .build();
            case REJECTED:
                if (event.getParticipantLimit() == confirmedRequestsCount) {
                    throw new ConflictException("Лимит участников исчерпан");
                }

                final CaseUpdatedStatusDto updatedStatusReject = updatedStatusConfirmed(event,
                        CaseUpdatedStatusDto.builder()
                                .idsFromUpdateStatus(new ArrayList<>(inputUpdate.getRequestIds())).build(),
                        RequestStatus.REJECTED, confirmedRequestsCount);
                List<Request> rejectRequest = requestRepository.findAllById(updatedStatusReject.getProcessedIds());

                return EventRequestStatusUpdateResult.builder()
                        .rejectedRequests(rejectRequest
                                .stream()
                                .map(requestMapper::toDto).collect(Collectors.toList()))
                        .build();
            default:
                throw new ValidationException("Некорректный статус - " + status);
        }
    }

    @Override
    public List<EventShortDto> getAllEventFromPublic(EventParams searchEventParams, HttpServletRequest request) {
        if (searchEventParams.getRangeEnd() != null && searchEventParams.getRangeStart() != null) {
            if (searchEventParams.getRangeEnd().isBefore(searchEventParams.getRangeStart())) {
                throw new ValidationException("Дата окончания не может быть раньше даты начала");
            }
        }

        addStatsClient(request);

        Pageable pageable = PageRequest.of(searchEventParams.getFrom() / searchEventParams.getSize(), searchEventParams.getSize());

        Specification<Event> specification = Specification.where(null);
        LocalDateTime now = LocalDateTime.now();

        if (searchEventParams.getText() != null) {
            String searchText = searchEventParams.getText().toLowerCase();
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.or(
                            criteriaBuilder.like(criteriaBuilder.lower(root.get("annotation")), "%" + searchText + "%"),
                            criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), "%" + searchText + "%")
                    ));
        }

        if (searchEventParams.getCategories() != null && !searchEventParams.getCategories().isEmpty()) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    root.get("category").get("id").in(searchEventParams.getCategories()));
        }

        LocalDateTime startDateTime = Objects.requireNonNullElse(searchEventParams.getRangeStart(), now);
        specification = specification.and((root, query, criteriaBuilder) ->
                criteriaBuilder.greaterThan(root.get("eventDate"), startDateTime));

        if (searchEventParams.getRangeEnd() != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.lessThan(root.get("eventDate"), searchEventParams.getRangeEnd()));
        }

        if (searchEventParams.getOnlyAvailable() != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.greaterThanOrEqualTo(root.get("participantLimit"), 0));
        }

        specification = specification.and((root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("state"), State.PUBLISHED));

        List<Event> resultEvents = eventRepository.findAll(specification, pageable).getContent();
        List<EventShortDto> result = resultEvents
                .stream().map(eventMapper::toDtoInShortEvent).collect(Collectors.toList());
        Map<Integer, Integer> viewStatsMap = getViewsAllEvents(resultEvents);

        for (EventShortDto event : result) {
            Integer viewsFromMap = viewStatsMap.getOrDefault(event.getId(), 0);
            event.setViews(viewsFromMap);
        }

        return result;
    }

    @Override
    public EventFullDto getEventById(Integer eventId, HttpServletRequest request) {
        Event event = eventValidation.existEvent(eventId);
        eventValidation.checkStatus(event.getState());
        addStatsClient(request);
        EventFullDto eventFullDto = eventMapper.toDtoInFullEvent(event);
        Map<Integer, Integer> viewStatsMap = getViewsAllEvents(List.of(event));
        Integer views = viewStatsMap.getOrDefault(event.getId(), 0);
        eventFullDto.setViews(views);
        return eventFullDto;
    }

    private Map<Integer, List<Request>> getConfirmedRequestsCount(List<Event> events) {
        List<Request> requests = requestRepository.findAllByEventIdInAndStatus(events
                .stream().map(Event::getId).collect(Collectors.toList()), RequestStatus.CONFIRMED);
        return requests.stream().collect(Collectors.groupingBy(r -> r.getEvent().getId()));
    }

    private CaseUpdatedStatusDto updatedStatusConfirmed(Event event, CaseUpdatedStatusDto caseUpdatedStatus,
                                                        RequestStatus status, int confirmedRequestsCount) {
        int freeRequest = event.getParticipantLimit() - confirmedRequestsCount;
        List<Integer> ids = caseUpdatedStatus.getIdsFromUpdateStatus();
        List<Integer> processedIds = new ArrayList<>();
        List<Request> requestListLoaded = checkRequestOrEventList(event.getId(), ids);
        List<Request> requestList = new ArrayList<>();

        for (Request request : requestListLoaded) {
            if (freeRequest == 0) {
                break;
            }

            request.setStatus(status);
            requestList.add(request);

            processedIds.add(request.getId());
            freeRequest--;
        }

        requestRepository.saveAll(requestList);
        caseUpdatedStatus.setProcessedIds(processedIds);
        return caseUpdatedStatus;
    }

    private List<Request> checkRequestOrEventList(Integer eventId, List<Integer> requestId) {
        return requestRepository.findByEventIdAndIdIn(eventId, requestId).orElseThrow(
                () -> new NotFoundException("Запроса с id = " + requestId + " или события с id = "
                        + eventId + "не существуют"));
    }

    private List<Request> rejectRequest(List<Integer> ids, Integer eventId) {
        List<Request> rejectedRequests = new ArrayList<>();
        List<Request> requestList = new ArrayList<>();
        List<Request> requestListLoaded = checkRequestOrEventList(eventId, ids);

        for (Request request : requestListLoaded) {
            if (!request.getStatus().equals(RequestStatus.PENDING)) {
                break;
            }
            request.setStatus(RequestStatus.REJECTED);
            requestList.add(request);
            rejectedRequests.add(request);
        }
        requestRepository.saveAll(requestList);
        return rejectedRequests;
    }

    private Map<Integer, Integer> getViewsAllEvents(List<Event> events) {
        List<String> uris = events.stream()
                .map(event -> String.format("/events/%s", event.getId()))
                .collect(Collectors.toList());

        List<LocalDateTime> startDates = events.stream()
                .map(Event::getCreatedOn)
                .collect(Collectors.toList());
        LocalDateTime earliestDate = startDates.stream()
                .min(LocalDateTime::compareTo)
                .orElse(null);
        Map<Integer, Integer> viewStatsMap = new HashMap<>();

        if (earliestDate != null) {
            ResponseEntity<Object> response = statsClient.getStats(earliestDate, LocalDateTime.now(),
                    uris, true);

            List<ResponseStatsDto> viewStatsList = objectMapper.convertValue(response.getBody(), new TypeReference<>() {
            });

            viewStatsMap = viewStatsList.stream()
                    .filter(statsDto -> statsDto.getUri().startsWith("/events/"))
                    .collect(Collectors.toMap(
                            statsDto -> Integer.parseInt(statsDto.getUri().substring("/events/".length())),
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
}

