package ru.nightcityroleplay.backend.dto;

public record CreateUserRequest(
    String username,
    String password
) {
}
