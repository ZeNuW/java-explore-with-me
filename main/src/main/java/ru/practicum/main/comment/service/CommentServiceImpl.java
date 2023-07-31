package ru.practicum.main.comment.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.main.comment.mapper.CommentMapper;
import ru.practicum.main.comment.dto.CommentCreateDto;
import ru.practicum.main.comment.dto.CommentDto;
import ru.practicum.main.comment.dto.CommentUpdateDto;
import ru.practicum.main.comment.model.Comment;
import ru.practicum.main.comment.repository.CommentRepository;
import ru.practicum.main.enumeration.EventStatus;
import ru.practicum.main.enumeration.RequestStatus;
import ru.practicum.main.event.model.Event;
import ru.practicum.main.event.repository.EventRepository;
import ru.practicum.main.exception.ObjectConflictException;
import ru.practicum.main.exception.ObjectNotExistException;
import ru.practicum.main.exception.ObjectValidationException;
import ru.practicum.main.request.model.ParticipationRequest;
import ru.practicum.main.request.repository.RequestRepository;
import ru.practicum.main.user.model.User;
import ru.practicum.main.user.repository.UserRepository;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final RequestRepository requestRepository;

    @Override
    public CommentDto createComment(Long eventId, Long userId, CommentCreateDto commentCreateDto) {
        User commentator = userRepository.findById(userId).orElseThrow(
                () -> new ObjectValidationException(String.format("Пользователь с id = %d не найден", userId)));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ObjectNotExistException(String.format("Эвент с id = %d не был найден", eventId)));
        ParticipationRequest request = requestRepository.findByRequesterAndEvent(userId, eventId);
        if (event.getState() != EventStatus.PUBLISHED) {
            throw new ObjectConflictException("Нельзя оставить комментарий на неопубликованное событие!");
        }
        if (request == null || request.getStatus() != RequestStatus.CONFIRMED) {
            throw new ObjectConflictException("Вы не были участником события и не можете оставить комментарий!");
        }
        return CommentMapper.commentToDto(
                commentRepository.save(CommentMapper.commentFromCreateDto(commentCreateDto, commentator, event)));
    }

    @Override
    public CommentDto updateComment(Long userId, Long commentId, CommentUpdateDto commentUpdateDto) {
        Comment comment = getComment(commentId);
        if (!comment.getCommentator().getId().equals(userId)) {
            throw new ObjectConflictException("Вы не можете обновить чужой комментарий.");
        }
        if (Duration.between(comment.getCreated(), LocalDateTime.now()).toHours() >= 24) {
            throw new ObjectConflictException("Обновить комментарий можно только в течении первых 24.");
        }
        comment.setText(commentUpdateDto.getText());
        comment.setLastUpdate(LocalDateTime.now());
        return CommentMapper.commentToDto(commentRepository.save(comment));
    }

    @Override
    public void deleteCommentByUser(Long commentId, Long userId) {
        Comment comment = commentRepository.findByIdAndCommentatorId(commentId, userId);
        if (comment == null) {
            getComment(commentId);
            throw new ObjectConflictException("Вы не можете удалить чужой комментарий.");
        } else {
            commentRepository.delete(comment);
        }
    }

    @Override
    public void deleteCommentByAdmin(Long commentId) {
        commentRepository.delete(getComment(commentId));
    }

    private Comment getComment(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new ObjectNotExistException(String.format("Комментарий с id = %d не найден.", commentId)));
    }
}