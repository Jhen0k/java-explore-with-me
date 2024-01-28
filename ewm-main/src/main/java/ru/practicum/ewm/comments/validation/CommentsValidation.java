package ru.practicum.ewm.comments.validation;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.comments.model.Comment;
import ru.practicum.ewm.comments.repository.CommentRepository;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CommentsValidation {

    UserRepository userRepository;
    EventRepository eventRepository;
    CommentRepository commentRepository;

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

    @Transactional(readOnly = true)
    public Comment getCommentAfterCheck(Long id) {
        return commentRepository.findById(id).orElseThrow(() -> new NotFoundException(
                String.format("Комментарий c id= %s не найден", id)));
    }

    @Transactional(readOnly = true)
    public void checkAuthorComment(User user, Comment comment) {
        if (!comment.getAuthor().equals(user)) {
            throw new DataIntegrityViolationException("Пользователь не является автором комментария");
        }
    }
}
