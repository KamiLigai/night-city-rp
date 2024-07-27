package ru.nightcityroleplay.backend.dto;


import lombok.Data;

@Data
public class CreateCharacterRequest {
    private String name;
    private int age;
    private Integer reputation;
}
