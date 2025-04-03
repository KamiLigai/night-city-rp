package ru.nightcityroleplay.backend.service;

import org.springframework.stereotype.Service;
import ru.nightcityroleplay.backend.entity.CharacterEntity;

@Service
public class CharacterClassService {
    // Соло
    public int bonusFromSolo(CharacterEntity character) {
        if (!character.getCharacterClass().equals("Соло")) {
            return 0;
        }
        if (character.getReputation() < 50) {
            return 0;
        }
        if (character.getReputation() < 80) {
            return 1;
        }
        return 2;
    }
}

