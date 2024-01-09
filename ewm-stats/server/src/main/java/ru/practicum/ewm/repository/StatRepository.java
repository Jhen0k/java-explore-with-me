package ru.practicum.ewm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.ewm.model.Hit;
import ru.practicum.ewm.model.Stat;

import java.time.LocalDateTime;
import java.util.List;

public interface StatRepository extends JpaRepository<Hit, Long> {

    @Query("SELECT new ru.practicum.ewm.model.Stat(h.uri, h.app, COUNT(DISTINCT h.ip))" +
            "FROM Hit h " +
            "WHERE h.timestamp BETWEEN ?2 AND ?3 " +
            "AND (h.uri IN (?1) OR (?1) is NULL) " +
            "GROUP BY h.app, h.uri " +
            "ORDER BY COUNT(h.ip) DESC")
    List<Stat> getStatsIsUnique(List<String> uris, LocalDateTime start, LocalDateTime end);



    @Query("SELECT new ru.practicum.ewm.model.Stat(h.uri, h.app, COUNT(h.ip))" +
            "FROM Hit h " +
            "WHERE h.timestamp BETWEEN ?2 AND ?3 " +
            "AND (h.uri IN (?1) OR (?1) is NULL) " +
            "GROUP BY h.app, h.uri " +
            "ORDER BY COUNT(h.ip) DESC")
    List<Stat> getStatsIsNotUnique(List<String> uris, LocalDateTime start, LocalDateTime end);
}
