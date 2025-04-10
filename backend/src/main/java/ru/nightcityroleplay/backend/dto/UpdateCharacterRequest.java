package ru.nightcityroleplay.backend.dto;

import lombok.Data;

@Data
public class UpdateCharacterRequest implements SaveCharacterRequest {
    private String name;
    private Integer height;
    private Integer weight;
    private Integer age;
    private String organization;
    private String characterClass;
    private Integer reputation;
}
