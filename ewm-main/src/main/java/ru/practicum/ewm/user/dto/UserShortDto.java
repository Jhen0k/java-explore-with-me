package ru.practicum.ewm.user.dto;

import lombok.Value;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Value
public class UserShortDto {

    Integer id;

    @NotBlank
    @Size(min = 2, max = 250)
    @Pattern(regexp = "^\\w+\\s{1,2}\\w+(\\s{1,2}\\w+)?$")
    String name;
}
