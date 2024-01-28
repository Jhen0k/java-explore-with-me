package ru.practicum.ewm.event.validation;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.category.repository.CategoryRepository;
import ru.practicum.ewm.event.dto.CaseUpdatedStatusDto;
import ru.practicum.ewm.event.dto.ParamsSearchForAdmin;
import ru.practicum.ewm.event.dto.UpdateEventRequest;
import ru.practicum.ewm.event.enums.EventStatus;
import ru.practicum.ewm.event.enums.RequestStatus;
import ru.practicum.ewm.event.mapper.LocationMapper;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.Location;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.exception.ValidationException;
import ru.practicum.ewm.model.Category;
import ru.practicum.ewm.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.request.model.Request;
import ru.practicum.ewm.request.repository.RequestRepository;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EventValidation {

    UserRepository userRepository;
    EventRepository eventRepository;
    CategoryRepository categoryRepository;
    LocationMapper locationMapper;
    RequestRepository requestRepository;

    @Transactional(readOnly = true)
    public User getUserAfterCheck(long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new NotFoundException(
                String.format("Пользователь c id= %s не найден", userId)));
    }

    @Transactional(readOnly = true)
    public Event getEventAfterCheck(long eventId) {
        return eventRepository.findById(eventId).orElseThrow(() -> new NotFoundException(
                String.format("Событие c id= %s не найдено", eventId)));
    }

    @Transactional
    public Event updateEventFields(Event eventModifiable, UpdateEventRequest eventUpdate) {
        String gotAnnotation = eventUpdate.getAnnotation();
        if (gotAnnotation != null && !gotAnnotation.isBlank()) {
            eventModifiable.setAnnotation(gotAnnotation);
        }
        Long gotCategory = eventUpdate.getCategory();
        if (gotCategory != null) {
            Category category = getCategoryAfterCheck(gotCategory);
            eventModifiable.setCategory(category);
        }
        String gotDescription = eventUpdate.getDescription();
        if (gotDescription != null && !gotDescription.isBlank()) {
            eventModifiable.setDescription(gotDescription);
        }
        if (eventUpdate.getLocation() != null) {
            Location location = locationMapper.toLocation(eventUpdate.getLocation());
            eventModifiable.setLocation(location);
        }
        Integer gotParticipantLimit = eventUpdate.getParticipantLimit();
        if (gotParticipantLimit != null) {
            eventModifiable.setParticipantLimit(gotParticipantLimit);
        }
        if (eventUpdate.getPaid() != null) {
            eventModifiable.setPaid(eventUpdate.getPaid());
        }
        Boolean requestModeration = eventUpdate.getRequestModeration();
        if (requestModeration != null) {
            eventModifiable.setRequestModeration(requestModeration);
        }
        String gotTitle = eventUpdate.getTitle();
        if (gotTitle != null && !gotTitle.isBlank()) {
            eventModifiable.setTitle(gotTitle);
        }
        return eventModifiable;
    }

    @Transactional(readOnly = true)
    public void checkValidDataTimeForUser(LocalDateTime dateTime) {
        LocalDateTime now = LocalDateTime.now();

        if (dateTime.isBefore(now.plusHours(2))) {
            throw new ValidationException("Начало события не должно начинаться раньше, чем через два часа.");
        }
    }

    @Transactional(readOnly = true)
    public Category getCategoryAfterCheck(Long catId) {
        return categoryRepository.findById(catId).orElseThrow(() -> new NotFoundException(
                String.format("Категория c id= %s не найдена", catId)));
    }

    @Transactional(readOnly = true)
    public Event checkEvenByInitiatorAndEventId(Long userId, Long eventId) {
        Optional<Event> event = eventRepository.findFirstByIdAndInitiatorIdIs(eventId, userId);

        if (event.isEmpty()) {
            throw new NotFoundException(String.format("Событие c id= %s не найдено", eventId));
        } else {
            return event.get();
        }
    }

    public Specification<Event> getSpecificationAfterValidation(Specification<Event> specification,
                                                                ParamsSearchForAdmin params) {

        List<Long> users = params.getUsers();
        List<String> states = params.getStates();
        List<Long> categories = params.getCategories();
        LocalDateTime rangeEnd = params.getRangeEnd();
        LocalDateTime rangeStart = params.getRangeStart();

        if (users != null && !users.isEmpty()) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    root.get("initiator").get("id").in(users));
        }
        if (states != null && !states.isEmpty()) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    root.get("eventStatus").as(String.class).in(states));
        }
        if (categories != null && !categories.isEmpty()) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    root.get("category").get("id").in(categories));
        }
        if (rangeEnd != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.lessThanOrEqualTo(root.get("eventDate"), rangeEnd));
        }
        if (rangeStart != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.greaterThanOrEqualTo(root.get("eventDate"), rangeStart));
        }

        return specification;
    }

    @Transactional
    public List<Request> rejectRequest(List<Long> ids, Long eventId) {
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

    @Transactional
    public CaseUpdatedStatusDto updatedStatusConfirmed(Event event, CaseUpdatedStatusDto caseUpdatedStatus,
                                                       RequestStatus status, int confirmedRequestsCount) {
        int freeRequest = event.getParticipantLimit() - confirmedRequestsCount;
        List<Long> ids = caseUpdatedStatus.getIdsFromUpdateStatus();
        List<Long> processedIds = new ArrayList<>();
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

    @Transactional(readOnly = true)
    public CaseUpdatedStatusDto getUpdateStatus(int participantLimit, int confirmedRequestsCount, Event event,
                                                EventRequestStatusUpdateRequest inputUpdate, RequestStatus requestStatus) {

        if (participantLimit == confirmedRequestsCount) {
            throw new DataIntegrityViolationException("Лимит участников исчерпан");
        }

        return updatedStatusConfirmed(event,
                CaseUpdatedStatusDto.builder()
                        .idsFromUpdateStatus(new ArrayList<>(inputUpdate.getRequestIds())).build(),
                requestStatus, confirmedRequestsCount);
    }

    @Transactional(readOnly = true)
    public void checkStatusNotPublished(EventStatus status) {
        if (status.equals(EventStatus.PUBLISHED) || status.equals(EventStatus.CANCELED)) {
            throw new DataIntegrityViolationException("Можно изменить только неподтвержденное событие");
        }
    }

    @Transactional(readOnly = true)
    public void checkStatusPublished(EventStatus status, long eventId) {
        if (!status.equals(EventStatus.PUBLISHED)) {
            throw new NotFoundException(String.format("Событие с id = %s не опубликовано", eventId));
        }
    }

    private List<Request> checkRequestOrEventList(Long eventId, List<Long> requestId) {
        return requestRepository.findByEventIdAndIdIn(eventId, requestId).orElseThrow(
                () -> new NotFoundException(String.format("Запроса с id = %S или события с id = %S не существуют",
                        requestId, eventId)));
    }
}
