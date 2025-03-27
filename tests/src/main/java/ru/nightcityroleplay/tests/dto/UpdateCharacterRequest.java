package ru.nightcityroleplay.tests.dto;

import lombok.Builder;

@Builder
public record UpdateCharacterRequest(
    String name,
    Integer height,
    Integer weight,
    Integer age,
    String organisation,
    String characterClass,
    Integer reputation
) {
}
