package ru.nightcityroleplay.backend.dto;

import lombok.Data;

@Data
public class UpdateSkillRequest {

    private String name;
    private String description;
    private int level;
    private String type;
    private int cost;
}
