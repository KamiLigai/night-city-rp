package ru.nightcityroleplay.backend.dto;

public interface SaveWeaponRequest {

    Boolean getIsMelee();

    String getName();

    String getWeaponType();

    Integer getPenetration();

    Integer getReputationRequirement();
}
