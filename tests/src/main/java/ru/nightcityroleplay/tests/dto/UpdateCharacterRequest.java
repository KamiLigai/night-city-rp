package ru.nightcityroleplay.tests.dto;

import lombok.Builder;

@Builder
public record UpdateCharacterRequest(
    String name,
    Integer height,
    Integer weight,
    Integer age,
    String organization,
    String characterClass,
    Integer reputation
) {
}
