package ru.nightcityroleplay.backend.service;

import org.springframework.stereotype.Service;
import ru.nightcityroleplay.backend.entity.CharacterEntity;


@Service
public class CharacterStatsService {


    public void updateCharacterStats(CharacterEntity character) {

        character.setImplant_points(calculateImplantPoints(character.getReputation()));
        character.setSpecial_implant_points(calculateSpecialImplantPoint(character.getReputation()));
        character.setBattle_points(calculateBattlePoints(character.getAge()));
        character.setCivil_points(calculateCivilPoints());
    }


    private int calculateImplantPoints(int reputation) {
        if (reputation <= 19) {
            return 7;
        } else if (reputation < 30) {
            return 8;
        } else if (reputation < 40) {
            return 9;
        } else {
            return 10;
        }

    }

    int calculateSpecialImplantPoint(int reputation) {
        return 0;
    }

    int calculateBattlePoints(int age) {
        if (age <= 25) {
            return 13;
        } else if (age <= 40) {
            return 15;
        } else {
            return 17;
        }
    }

    int calculateCivilPoints() {
        return 13;
    }

}
