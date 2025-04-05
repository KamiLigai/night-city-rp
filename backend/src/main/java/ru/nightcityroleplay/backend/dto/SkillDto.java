package ru.nightcityroleplay.backend.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class SkillDto {

    private UUID id;
    private String skillFamily;
    private String name;
    private String description;
    private String skillClass;
    private Boolean typeIsBattle;
    private int level;
    private int battleCost;
    private int civilCost;
    private int reputationRequirement;
}
