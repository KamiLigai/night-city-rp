package ru.nightcityroleplay.backend.dto.character;

import lombok.Data;

import java.util.Set;
import java.util.UUID;

@Data
public class UpdateCharacterWeaponRequest {
    private Set<UUID> weaponIds;
}
