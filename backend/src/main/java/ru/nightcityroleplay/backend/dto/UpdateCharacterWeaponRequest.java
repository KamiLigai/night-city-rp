package ru.nightcityroleplay.backend.dto;

import lombok.Data;

import java.util.Set;
import java.util.UUID;

@Data
public class UpdateCharacterWeaponRequest {
    private Set<UUID> weaponIds;
}
