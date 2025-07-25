package ru.nightcityroleplay.backend.dto.implants;

import lombok.Data;

import java.util.UUID;

@Data
public class ImplantDto {
    private UUID id;
    private String name;
    private ImplType implantType;
    private String description;
    private int reputationRequirement;
    private int implantPointsCost;
    private int specialImplantPointsCost;
}

