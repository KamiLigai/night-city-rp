package ru.nightcityroleplay.tests.dto;


import lombok.Builder;


@Builder
public record CreateCharacterRequest(
    String name,
    Integer age
) {
}
