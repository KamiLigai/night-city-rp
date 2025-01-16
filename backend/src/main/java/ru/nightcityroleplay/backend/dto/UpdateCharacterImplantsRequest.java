package ru.nightcityroleplay.backend.dto;

import lombok.Data;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Data
public class UpdateCharacterImplantsRequest {
    @Getter
    private List<UUID> implantIds;
}
