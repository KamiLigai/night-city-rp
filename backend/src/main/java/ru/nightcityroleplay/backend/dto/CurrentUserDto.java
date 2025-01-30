package ru.nightcityroleplay.backend.dto;

import lombok.Builder;

import java.util.List;
import java.util.UUID;

public record CurrentUserDto(
    UUID id,
    String username,
    List<String> roles
) {
    @Builder
    public CurrentUserDto {
    }
}
