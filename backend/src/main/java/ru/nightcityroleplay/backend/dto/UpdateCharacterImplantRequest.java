package ru.nightcityroleplay.backend.dto;


import lombok.Data;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Data
public class UpdateCharacterImplantRequest {
    private List<UUID> implantId;
    @Getter
    private List<UUID> implantIds;
}
