package ru.nightcityroleplay.backend.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class ImplantDto {
    private UUID id;
    private String name;
    private String implantType;
    private String description;
    private int reputationRequirement;
    private int implantPointsCost;
    private int specialImplantPointsCost;
}
