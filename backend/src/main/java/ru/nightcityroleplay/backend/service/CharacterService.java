package ru.nightcityroleplay.backend.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.nightcityroleplay.backend.dto.CharacterDTO;
import ru.nightcityroleplay.backend.dto.CreateCharacterRequest;
import ru.nightcityroleplay.backend.dto.UpdateCharacterRequest;
import ru.nightcityroleplay.backend.entity.CharacterEntity;
import ru.nightcityroleplay.backend.repo.CharacterRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

// Решить как сравнить ОвнерАйди и дать доступ для удаление только Владельцу

@Service
public class CharacterService {

    private final CharacterRepository characterRepository;

    public CharacterService(CharacterRepository characterRepository) {
        this.characterRepository = characterRepository;
    }


    private CharacterDTO toDto(CharacterEntity character) {
        CharacterDTO characterDTO = new CharacterDTO();
        characterDTO.setId(character.getId());
        characterDTO.setOwner_id(UUID.randomUUID());
        characterDTO.setName(character.getName());
        characterDTO.setAge(character.getAge());
        return characterDTO;
    }


    @Transactional
    public UUID createCharacter(CreateCharacterRequest request) {

        CharacterEntity character = new CharacterEntity();
        character.setOwner_id(UUID.randomUUID());
        character.setName(request.getName());
        character.setAge(request.getAge());
        character = characterRepository.save(character);
        return character.getId();

    }

    @Transactional
    public Page<CharacterDTO> getCharacterPage(Pageable pageable) {
        Page<CharacterEntity> characterPage = characterRepository.findAll(pageable);
        List<CharacterEntity> characters = characterPage.toList();
        List<CharacterDTO> characterDTOS = new ArrayList<>();
        for (var character : characters) {
            characterDTOS.add(toDto(character));

        }
        return new PageImpl<>(characterDTOS, pageable, characterPage.getTotalPages());


    }

    @Transactional
    public CharacterDTO getCharacter(UUID characterId) {
        Optional<CharacterEntity> byId = characterRepository.findById(characterId);
        if (byId.isEmpty()) {
            return null;
        } else {
            CharacterDTO usDto = toDto(byId.get());
            return usDto;
        }
    }

    @Transactional
    public void updateCharacter(UpdateCharacterRequest request, UUID characterId) {
        CharacterEntity character = new CharacterEntity();
        character.setOwner_id(UUID.randomUUID());
        character.setName(request.getName());
        character.setAge(request.getAge());
        characterRepository.save(character);
    }


    @Transactional
    public void deleteCharacter(UUID characterId) {
        characterRepository.deleteById(characterId);
    }
}




