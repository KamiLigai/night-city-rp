package ru.nightcityroleplay.backend.dto.user;

public record CreateUserRequest(
    String username,
    String password
) {
}
