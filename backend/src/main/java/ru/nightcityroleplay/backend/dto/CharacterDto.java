package ru.nightcityroleplay.backend.dto;

import lombok.Data;

import java.util.UUID;


@Data
public class CharacterDto {
    private UUID id;
    private UUID ownerId;
    private String name;
    private int age;
    private Integer reputation;
    private Integer implant_points;
    private Integer special_implant_points;
    private Integer battle_points;
    private Integer civil_points;
}

