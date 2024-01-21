package ru.practicum.ewm.request.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.ewm.request.enums.RequestStatus;
import ru.practicum.ewm.request.model.Request;

import java.util.List;
import java.util.Optional;

public interface RequestRepository extends JpaRepository<Request, Integer> {

    @Query
    List<Request> findAllByEventId(Integer eventId);

    @Query
    Optional<Request> findByEventIdAndId(Integer eventId, Integer id);

    int countByEventIdAndStatus(Integer eventId, RequestStatus status);

    @Query
    List<Request> findAllByEventIdInAndStatus(List<Integer> eventIds, RequestStatus status);

    @Query
    Boolean existsByEventIdAndRequesterId(Integer eventId, Integer userId);

    @Query
    Optional<Request> findByIdAndRequesterId(Integer id, Integer requesterId);

    @Query
    List<Request> findAllByRequesterId(Integer userId);

    @Query
    Optional<List<Request>> findByEventIdAndIdIn(Integer eventId, List<Integer> id);
}
