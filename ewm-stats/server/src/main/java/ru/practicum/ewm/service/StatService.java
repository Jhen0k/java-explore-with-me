package ru.practicum.ewm.service;

import ru.practicum.dto.RequestStatsDto;
import ru.practicum.dto.ResponseStatsDto;

import java.time.LocalDateTime;
import java.util.List;

public interface StatService {

    RequestStatsDto postStat(RequestStatsDto requestStatsDto);

    List<ResponseStatsDto> getStats(LocalDateTime start,
                                    LocalDateTime end,
                                    List<String> uris,
                                    boolean unique);
}
