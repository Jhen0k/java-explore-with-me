package ru.practicum.ewm.mapper;

import org.mapstruct.Mapper;
import ru.practicum.dto.ResponseStatsDto;
import ru.practicum.ewm.model.ResponseStat;

import java.util.List;

@Mapper(componentModel = "spring", uses = HitMapper.class)
public interface StatListMapper {

    List<ResponseStatsDto> toListDto(List<ResponseStat> hits);
}
