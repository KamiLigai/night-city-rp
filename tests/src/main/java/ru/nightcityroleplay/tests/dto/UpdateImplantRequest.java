package ru.nightcityroleplay.tests.dto;

import lombok.Builder;

@Builder
public record UpdateImplantRequest(
    String name,
    String implantType,
    String description,
    int reputationRequirement,
    int implantPointsCost,
    int specialImplantPointsCost) {
}

