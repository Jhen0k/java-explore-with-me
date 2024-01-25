package ru.practicum.ewm.mapper;

import org.mapstruct.Mapper;
import ru.practicum.dto.RequestStatsDto;
import ru.practicum.dto.ResponseStatsDto;
import ru.practicum.ewm.model.ResponseStat;
import ru.practicum.ewm.model.Stat;

@Mapper(componentModel = "spring")
public interface HitMapper {

    RequestStatsDto toRequestStatDto(Stat stat);

    Stat toStatRequest(RequestStatsDto dto);

    ResponseStatsDto toResponseStatsDto(ResponseStat stat);
}
