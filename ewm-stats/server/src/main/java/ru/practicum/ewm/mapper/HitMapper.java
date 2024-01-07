package ru.practicum.ewm.mapper;

import org.mapstruct.Mapper;
import ru.practicum.dto.HitDto;
import ru.practicum.dto.NewHitDto;
import ru.practicum.dto.StatDto;
import ru.practicum.ewm.model.Hit;

@Mapper(componentModel = "spring")
public interface HitMapper {

    NewHitDto toDto(Hit hit);

    Hit toEntity(HitDto hitDto);

    StatDto toStatDto(Hit hit);
}
