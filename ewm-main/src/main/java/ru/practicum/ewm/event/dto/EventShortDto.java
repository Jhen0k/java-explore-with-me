package ru.practicum.ewm.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Value;
import ru.practicum.ewm.category.dto.CategoryDto;
import ru.practicum.ewm.user.dto.UserShortDto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Value
public class EventShortDto {

    Integer id;

    @NotBlank
    String annotation;

    @NotNull
    CategoryDto categoryDto;

    Integer confirmedRequests;

    @NotNull
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    String eventDate;

    @NotNull
    UserShortDto initiator;

    @NotNull
    Boolean paid;

    @NotBlank
    String title;

    Integer views;
}
