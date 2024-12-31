package ru.nightcityroleplay.backend.dto;


import lombok.Data;

@Data
public class CreateCharacterRequest implements SaveCharacterRequest {
    private String name;
    private Integer age;
    private Integer reputation;
}
