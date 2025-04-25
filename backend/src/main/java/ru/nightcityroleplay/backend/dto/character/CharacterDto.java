package ru.nightcityroleplay.backend.dto.character;

import lombok.Data;

import java.util.List;
import java.util.UUID;


@Data
public class CharacterDto {
    private UUID id;
    private UUID ownerId;
    private String name;
    private Integer height;
    private Integer weight;
    private Integer age;
    private String organization;
    private String characterClass;
    private Integer reputation;
    private Integer implantPoints;
    private Integer specialImplantPoints;
    private Integer battlePoints;
    private Integer civilPoints;
    private List<UUID> weaponIds;
}

