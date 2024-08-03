package ru.nightcityroleplay.backend.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.nightcityroleplay.backend.dto.CharacterDto;
import ru.nightcityroleplay.backend.dto.CreateCharacterRequest;
import ru.nightcityroleplay.backend.dto.CreateCharacterResponse;
import ru.nightcityroleplay.backend.dto.UpdateCharacterRequest;
import ru.nightcityroleplay.backend.entity.CharacterEntity;
import ru.nightcityroleplay.backend.entity.User;
import ru.nightcityroleplay.backend.exception.NightCityRpException;
import ru.nightcityroleplay.backend.repo.CharacterRepository;


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Service
public class CharacterService {

    private final CharacterStatsService characterStatsService;


    private final CharacterRepository characterRepo;

    public CharacterService(CharacterRepository characterRepo, CharacterStatsService characterStatsService ) {
        this.characterStatsService = characterStatsService;
        this.characterRepo = characterRepo;
    }


    private CharacterDto toDto(CharacterEntity character) {
        CharacterDto characterDto = new CharacterDto();
        characterDto.setId(character.getId());
        characterDto.setOwnerId(character.getOwnerId());
        characterDto.setName(character.getName());
        characterDto.setAge(character.getAge());
        characterDto.setReputation(character.getReputation());
        characterDto.setImplantPoints(character.getImplant_points());
        characterDto.setSpecialImplantPoints(character.getSpecial_implant_points());
        characterDto.setBattlePoints(character.getBattle_points());
        characterDto.setCivilPoints(character.getCivil_points());
        return characterDto;
    }

    @Transactional
    public CreateCharacterResponse createCharacter(CreateCharacterRequest request, Authentication auth) {
        CharacterEntity character = new CharacterEntity();
        Object principal = auth.getPrincipal();
        User user = (User) principal;
        character.setOwnerId(user.getId());
        character.setName(request.getName());
        character.setAge(request.getAge());
        character.setReputation(request.getReputation());
        characterStatsService.updateCharacterStats(character);

        character = characterRepo.save(character);
        return new CreateCharacterResponse(character.getId());
    }

    @Transactional
    public Page<CharacterDto> getCharacterPage(Pageable pageable) {
        Page<CharacterEntity> characterPage = characterRepo.findAll(pageable);
        List<CharacterEntity> characters = characterPage.toList();
        List<CharacterDto> characterDtos = new ArrayList<>();
        for (var character : characters) {
            characterDtos.add(toDto(character));

        }
        return new PageImpl<>(characterDtos, pageable, characterPage.getTotalElements());


    }

    @Transactional
    public CharacterDto getCharacter(UUID characterId) {
        Optional<CharacterEntity> byId = characterRepo.findById(characterId);
        if (byId.isEmpty()) {
            return null;
        } else {
            return toDto(byId.get());
        }
    }

    @Transactional
    public void updateCharacter(UpdateCharacterRequest request, UUID characterId, Authentication auth) {
        CharacterEntity newCharacter = new CharacterEntity();
        if (characterRepo.findById(characterId).isEmpty()) {
            throw new NightCityRpException("Персонаж не найден");
        }
        CharacterEntity oldCharacter = characterRepo.findById(characterId).get();
        Object principal = auth.getPrincipal();
        User user = (User) principal;
        UUID userid = user.getId();

        if (!oldCharacter.getOwnerId().equals(userid)) {
            throw new NightCityRpException("Изменить чужого персонажа вздумал? а ты хорош.");
        }

        newCharacter.setId(characterId);
        newCharacter.setOwnerId(user.getId());
        newCharacter.setName(request.getName());
        newCharacter.setAge(request.getAge());
        newCharacter.setReputation(oldCharacter.getReputation());
        newCharacter.setImplant_points(oldCharacter.getImplant_points());
        newCharacter.setSpecial_implant_points(oldCharacter.getSpecial_implant_points());
        newCharacter.setBattle_points(oldCharacter.getBattle_points());
        newCharacter.setCivil_points(oldCharacter.getCivil_points());
        characterRepo.save(newCharacter);
    }

    @Transactional
    public void deleteCharacter(UUID characterId, Authentication auth) {
        Optional<CharacterEntity> character = characterRepo.findById(characterId);
        if (character.isEmpty()) {
            throw new NightCityRpException("Персонаж не найден");
        }
        Object principal = auth.getPrincipal();
        User user = (User) principal;
        UUID userid = user.getId();
        if (!character.get().getOwnerId().equals(userid)) {
            throw new NightCityRpException("Удалить чужого персонажа вздумал? а ты хорош.");
        } else {
            characterRepo.deleteById(characterId);
        }
    }
}




