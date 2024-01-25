package ru.practicum.ewm.request.validation;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.event.enums.EventStatus;
import ru.practicum.ewm.event.enums.RequestStatus;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.exception.ValidationException;
import ru.practicum.ewm.request.repository.RequestRepository;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RequestValidation {

    RequestRepository requestRepository;
    UserRepository userRepository;
    EventRepository eventRepository;

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

    public void checkStatus(RequestStatus status) {
        if (status.equals(RequestStatus.CANCELED) || status.equals(RequestStatus.REJECTED)) {
            throw new ValidationException("Запрос не подтвержден");
        }
    }

    @Transactional(readOnly = true)
    public void validateNewRequest(Event event, Long userId, Long eventId) {
        if (event.getInitiator().getId().equals(userId)) {
            throw new DataIntegrityViolationException("Пользователь с id= " + userId + " не инициатор события");
        }
        if (event.getParticipantLimit() > 0 && event.getParticipantLimit() <= requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED)) {
            throw new DataIntegrityViolationException("Превышен лимит участников события");
        }
        if (!event.getEventStatus().equals(EventStatus.PUBLISHED)) {
            throw new DataIntegrityViolationException("Событие не опубликовано");
        }
        if (requestRepository.existsByEventIdAndRequesterId(eventId, userId)) {
            throw new DataIntegrityViolationException("Попытка добавления дубликата");
        }
    }
}
