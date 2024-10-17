package ru.nightcityroleplay.backend.dto;


import lombok.Data;

@Data
public class CreateSkillRequest {

    private String name;
    private String description;
    private int level;
    private String type;
    private int cost;

}
