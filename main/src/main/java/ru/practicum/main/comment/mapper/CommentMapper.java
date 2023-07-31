package ru.practicum.main.comment.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.main.comment.dto.CommentCreateDto;
import ru.practicum.main.comment.dto.CommentDto;
import ru.practicum.main.comment.model.Comment;
import ru.practicum.main.event.model.Event;
import ru.practicum.main.user.model.User;

import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CommentMapper {
    public static CommentDto commentToDto(Comment comment) {
        return new CommentDto(
                comment.getId(),
                comment.getCommentator().getId(),
                comment.getEvent().getId(),
                comment.getText(),
                comment.getCreated(),
                comment.getLastUpdate()
        );
    }

    public static Comment commentFromCreateDto(CommentCreateDto commentDto, User commentator, Event event) {
        Comment comment = new Comment();
        comment.setText(commentDto.getText());
        comment.setEvent(event);
        comment.setCommentator(commentator);
        comment.setCreated(LocalDateTime.now());
        return new Comment();
    }
}