package ru.nightcityroleplay.backend.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class UpdateWeaponRequest {
    private UUID id;
    private Boolean is_melee;
    private String name;
    private String weapon_type;
    private int penetration;
    private int reputation_requirement;

}
