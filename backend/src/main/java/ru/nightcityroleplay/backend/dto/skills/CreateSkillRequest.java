package ru.nightcityroleplay.backend.dto.skills;


import lombok.Data;

@Data
public class CreateSkillRequest {
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
