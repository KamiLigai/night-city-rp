package ru.nightcityroleplay.backend.dto.weapons;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateWeaponRequest implements SaveWeaponRequest {
    private Boolean isMelee;
    private String name;
    private String weaponType;
    private Integer penetration;
    private Integer reputationRequirement;
}
