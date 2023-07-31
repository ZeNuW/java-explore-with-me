package ru.practicum.main.comment.service;

import ru.practicum.main.comment.dto.CommentCreateDto;
import ru.practicum.main.comment.dto.CommentDto;
import ru.practicum.main.comment.dto.CommentUpdateDto;

public interface CommentService {

    CommentDto createComment(Long eventId, Long userId, CommentCreateDto commentCreateDto);

    CommentDto updateComment(Long userId, Long commentId, CommentUpdateDto commentUpdateDto);

    void deleteCommentByUser(Long commentId, Long userId);

    void deleteCommentByAdmin(Long commentId);
}