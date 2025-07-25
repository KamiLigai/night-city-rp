package ru.nightcityroleplay.tests.dto;

import lombok.Builder;

import java.util.UUID;

@Builder
public record CreateSkillRequest(
    UUID id,
    String skillFamily,
    String name,
    String description,
    String skillClass,
    Boolean typeIsBattle
) {
}
