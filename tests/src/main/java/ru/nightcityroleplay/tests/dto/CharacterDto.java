package ru.nightcityroleplay.tests.dto;

import lombok.Data;

import java.util.UUID;


@Data
public class CharacterDto {
    private UUID id;
    private UUID ownerId;
    private String name;
    private int age;
}

