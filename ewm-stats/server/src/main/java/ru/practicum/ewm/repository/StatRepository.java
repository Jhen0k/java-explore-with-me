package ru.practicum.ewm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.ewm.model.Stat;
import ru.practicum.ewm.model.ResponseStat;

import java.time.LocalDateTime;
import java.util.List;

public interface StatRepository extends JpaRepository<Stat, Long> {



    @Query("SELECT new ru.practicum.ewm.model.ResponseStat(s.app, s.uri, COUNT(DISTINCT s.ip)) " +
            "FROM Stat as s " +
            "WHERE  s.timestamp BETWEEN ?2 AND ?3 " +
            "AND (s.uri IN (?1) OR (?1) is NULL) " +
            "GROUP BY s.app, s.uri " +
            "ORDER BY COUNT(DISTINCT s.ip) DESC")
    List<ResponseStat> getStatByUrisAndTimeIsUnique(List<String> uri, LocalDateTime start, LocalDateTime end);

    @Query("SELECT new ru.practicum.ewm.model.ResponseStat(s.uri, s.app, COUNT(s.ip)) " +
            "FROM Stat as s " +
            "WHERE  s.timestamp BETWEEN ?2 AND ?3 " +
            "AND (s.uri IN (?1) OR (?1) is NULL) " +
            "GROUP BY s.app, s.uri " +
            "ORDER BY COUNT(s.ip) DESC")
    List<ResponseStat> getStatByUrisAndTime(List<String> uri, LocalDateTime start, LocalDateTime end);
}
