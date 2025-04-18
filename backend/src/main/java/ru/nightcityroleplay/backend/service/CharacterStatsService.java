package ru.nightcityroleplay.backend.service;

import org.springframework.stereotype.Service;
import ru.nightcityroleplay.backend.entity.CharacterEntity;


@Service
public class CharacterStatsService {

    private static final int BP_COMPENSATION = 4;

    public void updateCharacterStats(CharacterEntity character) {

        character.setBattlePoints(calculateBattlePoints(character.getAge(), character.getReputation()));
        character.setCivilPoints(calculateCivilPoints(character.getReputation()));
    }


    public int calculateImplantPoints(int reputation) {
        if (reputation < 20) {
            return 7;
        } else if (reputation < 30) {
            return 8;
        } else if (reputation < 40) {
            return 9;
        } else if (reputation < 60) {
            return 10;
        } else if (reputation < 100) {
            return 11;
        } else if (reputation < 150) {
            return 13;
        } else if (reputation < 170) {
            return 15;
        } else {
            return 16;
        }
    }

    public int calculateSpecialImplantPoints(int reputation) {
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

    public int calculateBattlePoints(int age, int reputation) {

        if (age <= 25) {
            return 13 + reputation / 10 - BP_COMPENSATION;
        } else if (age <= 40) {
            return 15 + reputation / 10 - BP_COMPENSATION;
        } else {
            return 17 + reputation / 10 - BP_COMPENSATION;
        }
    }


    public int calculateCivilPoints(int reputation) {
        return 13 + reputation / 10 - BP_COMPENSATION;
    }

}
