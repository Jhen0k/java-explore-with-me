package ru.practicum.ewm.comments.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.comments.dto.CommentDto;
import ru.practicum.ewm.comments.dto.NewCommentDto;
import ru.practicum.ewm.comments.dto.UpdateCommentDto;
import ru.practicum.ewm.comments.mapper.CommentMapper;
import ru.practicum.ewm.comments.model.Comment;
import ru.practicum.ewm.comments.repository.CommentRepository;
import ru.practicum.ewm.comments.validation.CommentsValidation;
import ru.practicum.ewm.event.enums.EventStatus;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.paginator.Paginator;
import ru.practicum.ewm.user.model.User;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CommentServiceImpl implements CommentService {

    CommentRepository commentRepository;
    CommentMapper commentMapper;
    CommentsValidation commentsValidation;


    @Override
    @Transactional
    public CommentDto patchByUser(Long userId, Long commentId, UpdateCommentDto updateCommentDto) {
        User user = commentsValidation.getUserAfterCheck(userId);
        Comment comment = commentsValidation.getCommentAfterCheck(commentId);
        commentsValidation.checkAuthorComment(user, comment);
        LocalDateTime updateTime = LocalDateTime.now();

        if (updateTime.isAfter(comment.getCreated().plusMinutes(30L))) {
            throw new DataIntegrityViolationException("Редактировать комментарий можно не позже, " +
                    "чем через 30 минут после публикации");
        }

        comment.setText(updateCommentDto.getText());
        comment.setIsEdited(true);
        return commentMapper.toCommentDto(commentRepository.save(comment), comment.getEvent().getId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentDto> getUserComments(Long userId) {
        commentsValidation.getUserAfterCheck(userId);
        List<Comment> commentList = commentRepository.findByAuthor_Id(userId);
        return commentList.stream()
                .map((comment -> commentMapper.toCommentDto(comment, comment.getEvent().getId())))
                .collect(Collectors.toList());
    }


    @Override
    @Transactional(readOnly = true)
    public List<CommentDto> getEventComments(Long eventId, Integer from, Integer size) {
        commentsValidation.getCommentAfterCheck(eventId);
        Pageable pageable = Paginator.getPageable(from, size);
        List<Comment> comments = commentRepository.findAllByEvent_Id(eventId, pageable);

        return comments.stream()
                .map((comment -> commentMapper.toCommentDto(comment, comment.getEvent().getId())))
                .collect(Collectors.toList());

    }

    @Transactional
    @Override
    public void deleteComment(Long userId, Long commentId) {
        User user = commentsValidation.getUserAfterCheck(userId);
        Comment comment = commentsValidation.getCommentAfterCheck(commentId);
        commentsValidation.checkAuthorComment(user, comment);
        commentRepository.deleteById(commentId);
    }

    @Transactional
    @Override
    public void deleteCommentByAdmin(Long commentId) {
        commentsValidation.getCommentAfterCheck(commentId);
        commentRepository.deleteById(commentId);
    }

    @Override
    @Transactional
    public List<CommentDto> search(String text, Integer from, Integer size) {
        Pageable pageable = Paginator.getPageable(from, size);
        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }
        List<Comment> comments = commentRepository.search(text, pageable);

        return comments.stream()
                .map((comment -> commentMapper.toCommentDto(comment, comment.getEvent().getId())))
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public CommentDto createComment(Long userId, Long eventId, NewCommentDto newCommentDto) {
        Event event = commentsValidation.getEventAfterCheck(eventId);
        User user = commentsValidation.getUserAfterCheck(userId);
        if (!event.getEventStatus().equals(EventStatus.PUBLISHED)) {
            throw new DataIntegrityViolationException("Чтобы добавить комментарий событие должно быть опубликовано");
        }
        Comment comment = commentRepository.save(commentMapper.toComment(newCommentDto, event, user));
        return commentMapper.toCommentDto(comment, eventId);
    }
}