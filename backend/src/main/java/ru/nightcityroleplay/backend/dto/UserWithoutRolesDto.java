package ru.nightcityroleplay.backend.dto;

import lombok.Builder;
import java.util.UUID;

public record UserWithoutRolesDto(
    UUID id,
    String username
) {
    @Builder
    public UserWithoutRolesDto {
    }
}
