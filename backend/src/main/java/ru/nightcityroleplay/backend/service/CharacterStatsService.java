package ru.nightcityroleplay.backend.service;

import org.springframework.stereotype.Service;
import ru.nightcityroleplay.backend.entity.CharacterEntity;


@Service
public class CharacterStatsService {


    public void updateCharacterStats(CharacterEntity character) {

        character.setImplantPoints(calculateImplantPoints(character.getReputation()));
        character.setSpecialImplantPoints(calculateSpecialImplantPoint(character.getReputation()));
        character.setBattlePoints(calculateBattlePoints(character.getAge()));
        character.setCivilPoints(calculateCivilPoints());
    }


    public int calculateImplantPoints(int reputation) {
        if (reputation < 20) {
            return 7;
        } else if (reputation < 30) {
            return 8;
        } else if (reputation < 40) {
            return 9;
        } else {
            return 10;
        }
    }

    public int calculateSpecialImplantPoint(int reputation) {
        if (reputation < 90) {
            return 0;
        } else if (reputation < 120) {
            return 1;
        } else if (reputation < 160) {
            return 2;
        } else if (reputation < 180) {
            return 3;
        } else {
            return 4;
        }
    }

    public int calculateBattlePoints(int age) {
        if (age <= 25) {
            return 13;
        } else if (age <= 40) {
            return 15;
        } else {
            return 17;
        }
    }

    public int calculateCivilPoints() {
        return 13;
    }

}
