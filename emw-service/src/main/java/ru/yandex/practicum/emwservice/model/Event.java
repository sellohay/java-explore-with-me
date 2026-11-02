package ru.yandex.practicum.emwservice.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.yandex.practicum.emwservice.model.util.enums.EventState;
import ru.yandex.practicum.emwservice.model.util.Location;

import java.time.LocalDateTime;

@Entity
@Table(name = "events")
@Getter
@Setter
@ToString
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String annotation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    private LocalDateTime createdOn;

    private String description;

    private LocalDateTime eventDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "initiator_id")
    private User initiator;

    @Embedded
    private Location location;

    private boolean paid = false;

    private int participantLimit = 10;

    private LocalDateTime publishedOn;

    private boolean requestModeration = true;

    @Enumerated(EnumType.STRING)
    private EventState state;

    private String title;

}
