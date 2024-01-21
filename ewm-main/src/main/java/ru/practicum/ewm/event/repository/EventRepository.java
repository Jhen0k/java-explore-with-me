package ru.practicum.ewm.event.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.ewm.event.model.Event;

import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Integer>, JpaSpecificationExecutor<Event> {

    Optional<Event> findByInitiatorIdAndId(Integer userId, Integer eventId);

    @Query
    List<Event> findAllByAndInitiatorId(Integer userId, Pageable pageable);

    List<Event> findAllByIdIn(List<Integer> ids);

    Page<Event> findAll(Specification<Event> specification, Pageable pageable);
}
