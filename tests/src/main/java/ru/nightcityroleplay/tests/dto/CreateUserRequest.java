package ru.nightcityroleplay.tests.dto;

import lombok.Builder;

@Builder
public record CreateUserRequest(
    String username,
    String password
) {
}
