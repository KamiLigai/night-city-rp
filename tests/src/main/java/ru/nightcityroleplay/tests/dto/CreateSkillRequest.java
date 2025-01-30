package ru.nightcityroleplay.tests.dto;

import lombok.Builder;

@Builder
public record CreateSkillRequest(
    String name,
    String description,
    Integer level,
    String type,
    Integer cost
) {
}
