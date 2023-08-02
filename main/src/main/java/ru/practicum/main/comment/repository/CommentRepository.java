package ru.practicum.main.comment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.main.comment.model.Comment;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    Comment findByIdAndCommentatorId(Long commentId, Long userId);

    List<Comment> findAllByEventId(Long eventId);

    List<Comment> findAllByEventIdIn(List<Long> eventIds);
}