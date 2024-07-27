package ru.nightcityroleplay.backend.dto;

import lombok.Data;

@Data
public class UpdateCharacterRequest {
    private String name;
    private Integer age;
    private Integer reputation;
    private Integer implant_points;
    private Integer special_implant_points;
    private Integer battle_points;
    private Integer civil_points;
}
