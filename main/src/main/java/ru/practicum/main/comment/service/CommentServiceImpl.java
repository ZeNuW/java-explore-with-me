package ru.practicum.main.comment.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.main.comment.mapper.CommentMapper;
import ru.practicum.main.comment.dto.CommentInputDto;
import ru.practicum.main.comment.dto.CommentDto;
import ru.practicum.main.comment.model.Comment;
import ru.practicum.main.comment.repository.CommentRepository;
import ru.practicum.main.enumeration.EventStatus;
import ru.practicum.main.enumeration.RequestStatus;
import ru.practicum.main.event.model.Event;
import ru.practicum.main.event.repository.EventRepository;
import ru.practicum.main.exception.ObjectConflictException;
import ru.practicum.main.exception.ObjectNotExistException;
import ru.practicum.main.exception.ObjectValidationException;
import ru.practicum.main.request.repository.RequestRepository;
import ru.practicum.main.user.model.User;
import ru.practicum.main.user.repository.UserRepository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final RequestRepository requestRepository;

    @Override
    public CommentDto createComment(Long eventId, Long userId, CommentInputDto commentInputDto) {
        User commentator = userRepository.findById(userId).orElseThrow(
                () -> new ObjectValidationException(String.format("Пользователь с id = %d не найден", userId)));
        Event event = Optional.ofNullable(eventRepository.findByIdAndState(eventId, EventStatus.PUBLISHED))
                .orElseThrow(() -> new ObjectConflictException("Событие не существует или оно ещё неопубликованно!"));
        Optional.ofNullable(requestRepository.findByRequesterAndEventAndStatus(userId, eventId, RequestStatus.CONFIRMED))
                .orElseThrow(() -> new ObjectConflictException("Вы не были участником события и не можете оставить комментарий!"));
        return CommentMapper.commentToDto(
                commentRepository.save(CommentMapper.commentFromCreateDto(commentInputDto, commentator, event)));
    }

    @Override
    public CommentDto updateComment(Long userId, Long commentId, CommentInputDto commentInputDto) {
        Comment comment = Optional.ofNullable(commentRepository.findByIdAndCommentatorId(commentId, userId))
                .orElseThrow(() -> new ObjectConflictException("Вы не можете обновить чужой комментарий."));
        if (Duration.between(comment.getCreated(), LocalDateTime.now()).toHours() >= 24) {
            throw new ObjectConflictException("Обновить комментарий можно только в течении первых 24 часов.");
        }
        comment.setText(commentInputDto.getText());
        comment.setLastUpdate(LocalDateTime.now());
        return CommentMapper.commentToDto(commentRepository.save(comment));
    }

    @Override
    public void deleteCommentByUser(Long commentId, Long userId) {
        Comment comment = Optional.ofNullable(commentRepository.findByIdAndCommentatorId(commentId, userId))
                .orElseThrow(() -> new ObjectConflictException(String.format(
                        "Комментарий с id = %d не существует или вы пытаетесь удалить чужой комментарий", commentId)));
        commentRepository.delete(comment);
    }

    @Override
    public void deleteCommentByAdmin(Long commentId) {
        commentRepository.delete(commentRepository.findById(commentId)
                .orElseThrow(() -> new ObjectNotExistException(String.format("Комментарий с id = %d не найден.", commentId))));
    }
}