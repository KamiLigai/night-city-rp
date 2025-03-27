package ru.nightcityroleplay.tests.dto;


import lombok.Builder;


@Builder
public record CreateCharacterRequest(
    String name,
    String height,
    String weigt,
    Integer age,
    String organisation,
    String characterClass,
    Integer reputation
) {
}
