package ru.nightcityroleplay.backend.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.nightcityroleplay.backend.dto.CharacterDto;
import ru.nightcityroleplay.backend.dto.CreateCharacterRequest;
import ru.nightcityroleplay.backend.dto.UpdateCharacterRequest;
import ru.nightcityroleplay.backend.entity.CharacterEntity;
import ru.nightcityroleplay.backend.entity.User;
import ru.nightcityroleplay.backend.repo.CharacterRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

// Решить как сравнить ОвнерАйди и дать доступ для удаление только Владельцу

@Service
public class CharacterService {

    private final CharacterRepository characterRepository;

    public CharacterService(CharacterRepository characterRepository) {
        this.characterRepository = characterRepository;
    }


    private CharacterDto toDto(CharacterEntity character) {
        CharacterDto characterDTO = new CharacterDto();
        characterDTO.setId(character.getId());
        characterDTO.setOwner_id(UUID.randomUUID());
        characterDTO.setName(character.getName());
        characterDTO.setAge(character.getAge());
        return characterDTO;
    }


    @Transactional
    public UUID createCharacter(CreateCharacterRequest request) {

        CharacterEntity character = new CharacterEntity();
        character.setOwnerId(UUID.randomUUID());
        character.setName(request.getName());
        character.setAge(request.getAge());
        character = characterRepository.save(character);
        return character.getId();

    }

    @Transactional
    public Page<CharacterDto> getCharacterPage(Pageable pageable) {
        Page<CharacterEntity> characterPage = characterRepository.findAll(pageable);
        List<CharacterEntity> characters = characterPage.toList();
        List<CharacterDto> characterDTOS = new ArrayList<>();
        for (var character : characters) {
            characterDTOS.add(toDto(character));

        }
        return new PageImpl<>(characterDTOS, pageable, characterPage.getTotalPages());


    }

    @Transactional
    public CharacterDto getCharacter(UUID characterId) {
        Optional<CharacterEntity> byId = characterRepository.findById(characterId);
        if (byId.isEmpty()) {
            return null;
        } else {
            CharacterDto usDto = toDto(byId.get());
            return usDto;
        }
    }

    @Transactional
    public void updateCharacter(UpdateCharacterRequest request, UUID characterId, Authentication auth) {
        CharacterEntity character = new CharacterEntity();
        character.setId(characterId);
        Object principal = auth.getPrincipal();
        User user = (User) principal;
        character.setOwnerId(user.getId());
        character.setName(request.getName());
        character.setAge(request.getAge());
        characterRepository.save(character);
    }



    @Transactional
    public void deleteCharacter(UUID characterId) {
        characterRepository.deleteById(characterId);
    }
}




