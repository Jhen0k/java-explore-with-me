package ru.practicum.ewm.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.RequestStatsDto;
import ru.practicum.dto.ResponseStatsDto;
import ru.practicum.ewm.exception.ValidTimeException;
import ru.practicum.ewm.mapper.HitMapper;
import ru.practicum.ewm.model.Stat;
import ru.practicum.ewm.model.ResponseStat;
import ru.practicum.ewm.repository.StatRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StatServiceImpl implements StatService {

    StatRepository statRepository;
    HitMapper hitMapper;


    @Transactional
    @Override
    public RequestStatsDto postStat(RequestStatsDto requestStatsDto) {
        Stat stat = hitMapper.toStatRequest(requestStatsDto);
        return hitMapper.toRequestStatDto(statRepository.save(stat));
    }

    @Transactional(readOnly = true)
    @Override
    public List<ResponseStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        checkValidTime(start, end);

        List<ResponseStat> stats;

        if (unique) {
            stats = statRepository.getStatByUrisAndTimeIsUnique(uris, start, end);
        } else {
            stats = statRepository.getStatByUrisAndTime(uris, start, end);
        }

        return stats.stream().map(hitMapper::toResponseStatsDto).collect(Collectors.toList());
    }

    private void checkValidTime(LocalDateTime start, LocalDateTime end) {
        if (end.isBefore(start)) {
            throw new ValidTimeException("Время начала не может быть позже окончания мероприятия");
        }
    }
}
