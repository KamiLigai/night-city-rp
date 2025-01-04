package ru.nightcityroleplay.tests.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UpdateWeaponRequest {
    private Boolean isMelee;
    private String name;
    private String weaponType;
    private int penetration;
    private int reputationRequirement;
}


