package ru.nightcityroleplay.backend.dto;

import lombok.Data;

@Data
public class UpdateWeaponRequest {
    private Boolean isMelee;
    private String name;
    private String weaponType;
    private int penetration;
    private int reputationRequirement;
}
