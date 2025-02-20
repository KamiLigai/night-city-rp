package ru.nightcityroleplay.tests.dto;

import lombok.Builder;

@Builder
public record CreateSkillRequest(
    String name,
    String skillFamily,
    String skillClass,
    String description,
    int level,
    boolean typeIsBattle,
    int cost
) {
}
