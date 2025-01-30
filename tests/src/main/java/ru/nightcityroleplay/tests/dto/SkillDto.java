package ru.nightcityroleplay.tests.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class SkillDto {
    private UUID id;
    private String name;
    private String description;
    private Integer level;
    private String type;
    private Integer cost;
}
