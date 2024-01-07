package ru.practicum.ewm.mapper;

import org.mapstruct.Mapper;
import ru.practicum.dto.StatDto;
import ru.practicum.ewm.model.Stat;

import java.util.List;

@Mapper(componentModel = "spring", uses = HitMapper.class)
public interface StatListMapper {

    List<StatDto> toListDto(List<Stat> hits);
}
