package ru.practicum.ewm.controller;

import io.micrometer.core.lang.Nullable;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.dto.RequestStatsDto;
import ru.practicum.dto.ResponseStatsDto;
import ru.practicum.ewm.exception.ValidTimeException;
import ru.practicum.ewm.service.StatService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StatController {

    StatService statService;

    @PostMapping("/hit")
    @ResponseStatus(HttpStatus.CREATED)
    public RequestStatsDto postStat(@RequestBody RequestStatsDto requestStatsDto) {
        return statService.postStat(requestStatsDto);
    }

    @GetMapping("/stats")
    public List<ResponseStatsDto> getStat(@RequestParam("start") @Nullable @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime start,
                                          @RequestParam("end") @Nullable @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime end,
                                          @RequestParam(name = "uris", required = false) ArrayList<String> uris,
                                          @RequestParam(value = "unique", defaultValue = "false") boolean unique) {
        if (start == null || end == null) throw new ValidTimeException("Отсутствует дата старта или конца");
        return statService.getStats(start, end, uris, unique);
    }
}
