package ru.practicum.ewm.comments.mapper;

import org.mapstruct.Mapper;
import ru.practicum.ewm.comments.dto.CommentDto;
import ru.practicum.ewm.comments.dto.NewCommentDto;
import ru.practicum.ewm.comments.model.Comment;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.user.model.User;

import java.time.LocalDateTime;

@Mapper(componentModel = "spring")
public abstract class CommentMapper {

    public CommentDto toCommentDto(Comment comment, Long eventId) {
        return CommentDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .authorId(comment.getAuthor().getId())
                .eventId(eventId)
                .created(comment.getCreated())
                .isEdited(comment.getIsEdited())
                .build();
    }

    public Comment toComment(NewCommentDto commentDto, Event event, User user) {
        return Comment.builder()
                .text(commentDto.getText())
                .event(event)
                .author(user)
                .created(LocalDateTime.now())
                .isEdited(false)
                .build();
    }
}
