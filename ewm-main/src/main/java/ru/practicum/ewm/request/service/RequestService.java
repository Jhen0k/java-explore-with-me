package ru.practicum.ewm.request.service;

import ru.practicum.ewm.request.dto.ParticipationRequestDto;

import java.util.List;

public interface RequestService {

    ParticipationRequestDto addNewRequest(Integer userId, Integer eventId);

    List<ParticipationRequestDto> getRequestsByUserId(Integer userId);

    ParticipationRequestDto cancelRequest(Integer userId, Integer requestId);
}
