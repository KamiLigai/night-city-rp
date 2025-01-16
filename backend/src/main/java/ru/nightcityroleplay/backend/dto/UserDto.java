package ru.nightcityroleplay.backend.dto;

import lombok.Builder;
import java.util.UUID;

public record UserDto(
    UUID id,
    String username
) {
    @Builder
    public UserDto {
    }
}
