package ru.nightcityroleplay.tests.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class WeaponDto {
    private UUID id;
    private Boolean isMelee;
    private String name;
    private String weaponType;
    private int penetration;
    private int reputationRequirement;
}
