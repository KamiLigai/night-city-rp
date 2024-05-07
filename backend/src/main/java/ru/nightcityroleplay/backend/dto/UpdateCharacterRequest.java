package ru.nightcityroleplay.backend.dto;

import lombok.Data;

@Data
public class UpdateCharacterRequest {
    public String name;
    public Integer age;
}
