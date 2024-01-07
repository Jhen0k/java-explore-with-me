package ru.practicum.ewm.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.HitDto;
import ru.practicum.dto.NewHitDto;
import ru.practicum.dto.StatDto;
import ru.practicum.ewm.mapper.HitMapper;
import ru.practicum.ewm.mapper.StatListMapper;
import ru.practicum.ewm.model.Hit;
import ru.practicum.ewm.model.Stat;
import ru.practicum.ewm.repository.StatRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StatServiceImpl implements StatService {

    StatRepository statRepository;
    HitMapper hitMapper;
    StatListMapper statListMapper;

    @Override
    @Transactional
    public NewHitDto createHit(HitDto hitDto) {
        Hit hit = hitMapper.toEntity(hitDto);
        return hitMapper.toDto(statRepository.save(hit));
    }

    @Override
    @Transactional(readOnly = true)
    public List<StatDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        List<Stat> stats;

        if (unique) {
            stats = statRepository.getStatsIsUnique(uris, start, end);
        } else {
            stats = statRepository.getStatsIsNotUnique(uris, start, end);
        }

        return statListMapper.toListDto(stats);
    }
}
