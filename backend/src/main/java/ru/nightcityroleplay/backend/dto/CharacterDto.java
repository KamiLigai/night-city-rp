package ru.nightcityroleplay.backend.dto;

import lombok.Data;

import java.util.UUID;


@Data
public class CharacterDto {
    private UUID id;
    private UUID ownerId;
    private String name;
    private Integer age;
    private Integer reputation;
    private Integer implantPoints;
    private Integer specialImplantPoints;
    private Integer battlePoints;
    private Integer civilPoints;

}

