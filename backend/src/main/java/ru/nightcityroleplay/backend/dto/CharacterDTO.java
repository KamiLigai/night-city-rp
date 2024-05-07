package ru.nightcityroleplay.backend.dto;

import lombok.Data;

import java.util.UUID;


@Data
public class CharacterDTO {
    private UUID id;
    private UUID owner_id;
    private String name;
    private int age;

}

