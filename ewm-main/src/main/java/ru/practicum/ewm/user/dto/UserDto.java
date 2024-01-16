package ru.practicum.ewm.user.dto;

import lombok.Builder;
import lombok.Value;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Builder
@Value
public class UserDto {

    Integer id;

    @NotBlank
    @Email
    @Size(min = 6, max = 254)
    String email;

    @NotBlank
    @Size(min = 2, max = 250)
    @Pattern(regexp = "^\\w+\\s{1,2}\\w+(\\s{1,2}\\w+)?$")
    String name;
}
