package ru.nightcityroleplay.backend.dto;
import lombok.Data;
import ru.nightcityroleplay.backend.entity.WeaponEntity;
import java.util.UUID;
@Data
public class UpdateCharacterWeaponRequest {
    private UUID weaponId;
}