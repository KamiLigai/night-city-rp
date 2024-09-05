package ru.nightcityroleplay.backend.dto;


import lombok.Data;

@Data
public class CreateCharacterRequest {
    private String name;
    private Integer age;
}
