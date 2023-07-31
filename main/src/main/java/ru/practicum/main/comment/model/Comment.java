package ru.practicum.main.comment.model;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import ru.practicum.main.event.model.Event;
import ru.practicum.main.user.model.User;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "comments")
@Data
@RequiredArgsConstructor
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    private Long id;
    @JoinColumn(name = "commentator_id")
    @OneToOne(fetch = FetchType.LAZY)
    private User commentator;
    @JoinColumn(name = "event_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Event event;
    private String text;
    @Column(name = "created_on")
    private LocalDateTime created;
    @Column(name = "last_update")
    private LocalDateTime lastUpdate;
}