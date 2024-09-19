package ru.nightcityroleplay.backend.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class UpdateCharacterWeaponRequest {
    private UUID weaponId;
}
