package ru.nightcityroleplay.backend.dto.character;


import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class UpdateCharacterImplantRequest {
    private List<UUID> implantId;
}
