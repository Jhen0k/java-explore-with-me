package ru.practicum.ewm.event.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.event.enums.State;
import ru.practicum.ewm.user.model.User;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.time.LocalDateTime;


@Setter
@Getter
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "events")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @Column
    String annotation;

    @OneToOne
    @JoinColumn(name = "category_id")
    Category category;

    @Column(name = "confirmed_requests")
    Integer confirmedRequests;

    @Column(name = "created_on")
    LocalDateTime createdOn;

    @Column
    String description;

    @Column(name = "event_date")
    LocalDateTime eventDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "initiator_id")
    User initiator;

    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "location_id")
    Location location;

    @Column
    Boolean paid;

    @Column(name = "participant_limit")
    Integer participantLimit;

    @Column(name = "published_on")
    LocalDateTime publishedOn;

    @Column(name = "request_moderation")
    Boolean requestModeration;

    @Column
    @Enumerated(EnumType.STRING)
    State state;

    @Column
    String title;

    @Column
    Integer views;

}
