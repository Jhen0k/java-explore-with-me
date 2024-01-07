package ru.practicum.ewm.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.dto.HitDto;
import ru.practicum.dto.NewHitDto;
import ru.practicum.dto.StatDto;
import ru.practicum.ewm.service.StatService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class StatController {

    private final StatService statService;

    @PostMapping("/hit")
    @ResponseStatus(HttpStatus.CREATED)
    public NewHitDto createHit(@RequestBody HitDto hitDto) {
        return statService.createHit(hitDto);
    }

    @GetMapping("/stats")
    public List<StatDto> getStats(@RequestParam("start") LocalDateTime start,
                                  @RequestParam("end") LocalDateTime end,
                                  @RequestParam(value = "uris", required = false, defaultValue = "") List<String> uris,
                                  @RequestParam(value = "unique", required = false, defaultValue = "false") Boolean unique) {
        return statService.getStats(start, end, uris, unique);
    }
}
