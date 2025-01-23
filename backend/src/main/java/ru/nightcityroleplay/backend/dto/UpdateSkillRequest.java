package ru.nightcityroleplay.backend.dto;

import lombok.Data;

@Data
public class UpdateSkillRequest {

    private String name;
    private String description;
    private String skillClass;
    private Boolean typeIsBattle;
}
