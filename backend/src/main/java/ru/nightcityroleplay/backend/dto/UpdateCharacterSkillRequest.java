package ru.nightcityroleplay.backend.dto;

import lombok.Data;
import ru.nightcityroleplay.backend.entity.Skill;

import java.util.UUID;

@Data
public class UpdateCharacterSkillRequest {
    private UUID skillId;
}
