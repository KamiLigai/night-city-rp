package ru.nightcityroleplay.backend.service;

import org.springframework.stereotype.Service;
import ru.nightcityroleplay.backend.dto.CharacterDTO;
import ru.nightcityroleplay.backend.dto.CreateCharacterRequest;
import ru.nightcityroleplay.backend.dto.UpdateCharacterRequest;
import ru.nightcityroleplay.backend.repo.CharacterRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

// Решить как сравнить ОвнерАйди и дать доступ для удаление только Владельцу

@Service
public class CharacterService {

    private List<CharacterDTO> characters = new ArrayList<>();


    public UUID createCharacter(CreateCharacterRequest request) {
        CharacterDTO newCharacter = new CharacterDTO();
        newCharacter.setId(UUID.randomUUID());
        newCharacter.setOwner_id(UUID.randomUUID());
        newCharacter.setName(request.getName());
        newCharacter.setAge(request.getAge());
        CharacterRepository s;

        characters.add(newCharacter);
        return newCharacter.getId();
    }

    public List<CharacterDTO> getCharacter() {
        return characters;
    }


    public CharacterDTO getCharacter(UUID characterId) {
        for (int i = 0; i < characters.size(); i++) {
            CharacterDTO character = characters.get(i);
            if (character.getId().equals(characterId)) {
                return character;
            }
        }
        throw new RuntimeException(" и кончились");
    }

    public void updateCharacter(UpdateCharacterRequest request, UUID characterId) {
        for (int i = 0; i < characters.size(); i++) {
            CharacterDTO character = characters.get(i);
            if (character.getId().equals(characterId)) {
                character.setName(request.getName());
                character.setAge(request.getAge());
                return;
            }
        }
        CharacterDTO newCharacter = new CharacterDTO();
        newCharacter.setId(characterId);
        newCharacter.setName(request.getName());
        newCharacter.setAge(request.getAge());
        characters.add(newCharacter);
    }


    public void deleteCharacter(UUID characterId) {
        for (int i = 0; i < characters.size(); i++) {
            CharacterDTO character = characters.get(i);
            if (character.getId().equals(characterId)) {
                characters.remove(i);
                return;
            }
        }
    }
}


