package ru.nightcityroleplay.tests.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class UpdateCharacterWeaponRequest {
    private List<UUID> weaponIds;
}
