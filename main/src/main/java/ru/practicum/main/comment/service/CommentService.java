package ru.practicum.main.comment.service;

import ru.practicum.main.comment.dto.CommentInputDto;
import ru.practicum.main.comment.dto.CommentDto;

public interface CommentService {

    CommentDto createComment(Long eventId, Long userId, CommentInputDto commentInputDto);

    CommentDto updateComment(Long userId, Long commentId, CommentInputDto commentInputDto);

    void deleteCommentByUser(Long commentId, Long userId);

    void deleteCommentByAdmin(Long commentId);
}