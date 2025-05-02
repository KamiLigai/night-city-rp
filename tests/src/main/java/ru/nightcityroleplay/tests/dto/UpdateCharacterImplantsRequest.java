package ru.nightcityroleplay.tests.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Set;
import java.util.UUID;

@Data
@Builder
public class UpdateCharacterImplantsRequest {
    private Set<UUID> implantIds;
}
