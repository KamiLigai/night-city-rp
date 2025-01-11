package ru.nightcityroleplay.tests.dto;

import lombok.Builder;

@Builder
public record CreateSkillRequest(
    String name,
    String description,
    int level,
    String type,
    int cost
) {
}
