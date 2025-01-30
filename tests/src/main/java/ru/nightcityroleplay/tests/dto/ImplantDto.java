package ru.nightcityroleplay.tests.dto;

import java.util.UUID;

public record ImplantDto(UUID id,
    String name,
    String implantType,
    String description,
    int reputationRequirement,
    int implantPointsCost,
    int specialImplantPointsCost) {
}
