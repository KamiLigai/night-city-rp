package ru.nightcityroleplay.backend.dto.weapons;

import lombok.Data;

@Data
public class UpdateWeaponRequest implements SaveWeaponRequest {
    private Boolean isMelee;
    private String name;
    private String weaponType;
    private Integer penetration;
    private Integer reputationRequirement;
}
