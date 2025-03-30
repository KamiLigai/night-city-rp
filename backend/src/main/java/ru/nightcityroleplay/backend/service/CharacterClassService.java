package ru.nightcityroleplay.backend.service;

import org.springframework.stereotype.Service;
import ru.nightcityroleplay.backend.entity.CharacterEntity;

@Service
public class CharacterClassService {

    private final CharacterStatsService characterStatsService;

    public CharacterClassService(CharacterStatsService characterStatsService) {
        this.characterStatsService = characterStatsService;
    }


    // Соло
    public int bonusFromSolo(CharacterEntity character) {
        if (character.getCharacterClass().equals("Соло")) {
            if (character.getReputation() > 50) {
                return 1;
            }
            if (character.getReputation() > 80) {
                return 2;
            }
        }
        return 0;
    }

}
