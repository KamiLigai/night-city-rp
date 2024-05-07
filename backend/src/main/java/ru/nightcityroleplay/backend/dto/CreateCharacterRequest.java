package ru.nightcityroleplay.backend.dto;


import lombok.Data;

@Data
public class CreateCharacterRequest {
    public String name;
    public Integer age;
}
