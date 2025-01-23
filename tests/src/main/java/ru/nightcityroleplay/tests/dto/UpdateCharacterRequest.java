package ru.nightcityroleplay.tests.dto;

import lombok.Builder;

@Builder
public record UpdateCharacterRequest(
    String name,
    Integer reputation,
    Integer age
) {
}
