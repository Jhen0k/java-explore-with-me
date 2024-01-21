package ru.practicum.ewm.request.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.request.dto.ParticipationRequestDto;
import ru.practicum.ewm.request.service.RequestService;

import javax.validation.constraints.Min;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping(path = "users/{userId}/requests")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RequestPrivateController {

    RequestService requestService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipationRequestDto addRequest(@PathVariable(value = "userId") @Min(0) Integer userId,
                                              @RequestParam(name = "eventId") @Min(0) Integer eventId) {
        return requestService.addNewRequest(userId, eventId);
    }

    @GetMapping
    public List<ParticipationRequestDto> getAllRequests(@PathVariable(value = "userId") @Min(0) Integer userId) {
        return requestService.getRequestsByUserId(userId);
    }

    @PatchMapping("/{requestId}/cancel")
    public ParticipationRequestDto canceledRequest(@PathVariable(value = "userId") @Min(0) Integer userId,
                                                   @PathVariable(value = "requestId") @Min(0) Integer requestId) {
        return requestService.cancelRequest(userId, requestId);
    }
}
