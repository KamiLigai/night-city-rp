package ru.nightcityroleplay.tests.dto;

import lombok.Builder;

@Builder
public record CreateWeaponRequest(
    Boolean isMelee,
    String name,
    String weaponType,
    int penetration,
    int reputationRequirement
) {
}
