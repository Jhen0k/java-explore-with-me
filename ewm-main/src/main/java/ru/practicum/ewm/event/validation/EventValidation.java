package ru.practicum.ewm.event.validation;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.category.repository.CategoryRepository;
import ru.practicum.ewm.event.dto.EventParamsAdmin;
import ru.practicum.ewm.event.dto.UpdateEventRequest;
import ru.practicum.ewm.event.enums.State;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.user.repository.UserRepository;
import ru.practicum.ewm.exception.ValidationException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EventValidation {

    EventRepository eventRepository;
    UserRepository userRepository;
    CategoryRepository categoryRepository;

    public void existUser(int userId) {
        if (!userRepository.existsById(userId)) {
            throw new ValidationException(String.format("Пользователь с id %s не найден.", userId));
        }
    }

    public Event existEvent(int eventId) {

        return eventRepository.findById(eventId).orElseThrow(
                () -> new NotFoundException(String.format("Событие с id %s не найдено.", eventId)));
    }

    public Event getEventCheckByInitiatorAndEventId(Integer userId, Integer eventId) {
        existUser(userId);
        existEvent(eventId);
        return eventRepository.findByInitiatorIdAndId(userId, eventId).orElseThrow(
                () -> new NotFoundException("События с id = " + eventId + "и с пользователем с id = " + userId +
                        " не существует"));
    }

    public void checkStatus(State state) {
        if (!state.equals(State.PUBLISHED)) {
            throw new NotFoundException("Событие не опубликовано");
        }
    }

    public void checkAllStatus(State state) {
        if (state.equals(State.PUBLISHED) || state.equals(State.CANCELED)) {
            throw new ConflictException("Можно изменить только неподтвержденное событие");
        }
    }

    public void checkUserByOwnerEvent(Event oldEvent, int userId) {
        if (!oldEvent.getInitiator().getId().equals(userId)) {
            throw new ConflictException("Пользователь с id= " + userId + " не автор события");
        }
    }

    public Specification<Event> checkEventParamsAdmin(EventParamsAdmin eventParamsAdmin) {
        Specification<Event> specification = Specification.where(null);

        List<Integer> users = eventParamsAdmin.getUsers();
        List<String> states = eventParamsAdmin.getStates();
        List<Integer> categories = eventParamsAdmin.getCategories();
        LocalDateTime rangeEnd = eventParamsAdmin.getRangeEnd();
        LocalDateTime rangeStart = eventParamsAdmin.getRangeStart();

        if (users != null && !users.isEmpty()) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    root.get("initiator").get("id").in(users));
        }
        if (states != null && !states.isEmpty()) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    root.get("state").as(String.class).in(states));
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

    public Event getEventValidatedFields(Event oldEvent, UpdateEventRequest updateEvent) {
        boolean hasChanges = false;
        String annotation = updateEvent.getAnnotation();
        if (annotation != null && !annotation.isBlank()) {
            oldEvent.setAnnotation(annotation);
            hasChanges = true;
        }
        Integer gotCategory = updateEvent.getCategory();
        if (gotCategory != null) {
            Category category = checkCategory(gotCategory);
            oldEvent.setCategory(category);
            hasChanges = true;
        }
        String gotDescription = updateEvent.getDescription();
        if (gotDescription != null && !gotDescription.isBlank()) {
            oldEvent.setDescription(gotDescription);
            hasChanges = true;
        }
        if (updateEvent.getLocation() != null) {
            oldEvent.setLocation(updateEvent.getLocation());
            hasChanges = true;
        }
        Integer gotParticipantLimit = updateEvent.getParticipantLimit();
        if (gotParticipantLimit != null) {
            oldEvent.setParticipantLimit(gotParticipantLimit);
            hasChanges = true;
        }
        if (updateEvent.getPaid() != null) {
            oldEvent.setPaid(updateEvent.getPaid());
            hasChanges = true;
        }
        Boolean requestModeration = updateEvent.getRequestModeration();
        if (requestModeration != null) {
            oldEvent.setRequestModeration(requestModeration);
            hasChanges = true;
        }
        String gotTitle = updateEvent.getTitle();
        if (gotTitle != null && !gotTitle.isBlank()) {
            oldEvent.setTitle(gotTitle);
            hasChanges = true;
        }
        if (!hasChanges) {

            oldEvent = null;
        }
        return oldEvent;

    }

    public void checkEventRequest(Event event) {
        if (!event.isRequestModeration() || event.getParticipantLimit() == 0) {
            throw new ConflictException("Это событие не требует подтверждения запросов");
        }
    }

    private Category checkCategory(Integer catId) {
        return categoryRepository.findById(catId).orElseThrow(
                () -> new NotFoundException("Категории с id = " + catId + " не существует"));
    }

    public void checkValidDataTimeForUser(LocalDateTime dateTime) {
        LocalDateTime now = LocalDateTime.now();

        if (dateTime.isBefore(now.plusHours(2))) {
            throw new ValidationException("Начало события не должно начинаться раньше, чем через два часа.");
        }
    }
}
