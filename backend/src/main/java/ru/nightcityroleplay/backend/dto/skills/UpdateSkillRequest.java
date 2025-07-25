package ru.nightcityroleplay.backend.dto.skills;

import lombok.Data;

@Data
public class UpdateSkillRequest {
    private String skillFamily;
    private String name;
    private String description;
    private String skillClass;
    private Boolean typeIsBattle;
}
