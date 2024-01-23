package ru.practicum.ewm.event.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import ru.practicum.ewm.event.enums.EventStatus;
import ru.practicum.ewm.model.Category;
import ru.practicum.ewm.user.model.User;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.time.LocalDateTime;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity(name = "events")
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @Column(name = "annotation", nullable = false, length = 2000)
    String annotation;
    @ManyToOne
    @JoinColumn(name = "category_id")
    Category category;
    @Column(name = "create_date")
    LocalDateTime createdDate;
    @Column(name = "description", length = 7000)
    String description;
    @Column(name = "event_date", nullable = false)
    LocalDateTime eventDate;
    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "initiator_id")
    User initiator;
    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "location_id")
    Location location;
    @Column(name = "paid")
    boolean paid;
    @Column(name = "participant_limit")
    int participantLimit;
    @Column(name = "published_date")
    LocalDateTime publisherDate;
    @Column(name = "request_moderation")
    boolean requestModeration;
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    EventStatus eventStatus;
    @Column(name = "title", length = 120)
    String title;

}
