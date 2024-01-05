package ru.practicum.dto;

import lombok.Value;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Value
public class StatDto {

    @NotBlank
    String app;
    @NotBlank
    String uri;
    @NotNull
    Long hits;
}
