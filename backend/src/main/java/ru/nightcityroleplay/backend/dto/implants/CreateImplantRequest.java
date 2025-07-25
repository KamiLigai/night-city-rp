package ru.nightcityroleplay.backend.dto.implants;

import lombok.Data;

@Data
public class CreateImplantRequest {
    private String name;
    private ImplantType implantType;
    private String description;
    private int reputationRequirement;
    private int implantPointsCost;
    private int specialImplantPointsCost;
}
