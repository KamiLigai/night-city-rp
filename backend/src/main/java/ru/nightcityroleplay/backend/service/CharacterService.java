package ru.nightcityroleplay.backend.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.nightcityroleplay.backend.dto.*;
import ru.nightcityroleplay.backend.entity.CharacterEntity;
import ru.nightcityroleplay.backend.entity.Skill;
import ru.nightcityroleplay.backend.entity.User;
import ru.nightcityroleplay.backend.entity.Weapon;
import ru.nightcityroleplay.backend.repo.CharacterRepository;
import ru.nightcityroleplay.backend.repo.SkillRepository;
import ru.nightcityroleplay.backend.repo.WeaponRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static ru.nightcityroleplay.backend.util.BooleanUtils.not;


@Service
@Slf4j
public class CharacterService {

    private final CharacterStatsService characterStatsService;
    private final CharacterRepository characterRepo;
    private final WeaponRepository weaponRepo;
    private final SkillRepository skillRepo;

    public CharacterService(
        CharacterRepository characterRepo,
        CharacterStatsService characterStatsService,
        WeaponRepository weaponRepo,
        SkillRepository skillRepo
    ) {
        this.characterStatsService = characterStatsService;
        this.characterRepo = characterRepo;
        this.weaponRepo = weaponRepo;
        this.skillRepo = skillRepo;
    }

    private CharacterDto toDto(CharacterEntity character) {
        CharacterDto characterDto = new CharacterDto();
        characterDto.setId(character.getId());
        characterDto.setOwnerId(character.getOwnerId());
        characterDto.setName(character.getName());
        characterDto.setAge(character.getAge());
        List<UUID> weaponIds = character.getWeapons().stream()
            .map(Weapon::getId)
            .collect(Collectors.toList());
        characterDto.setWeaponIds(weaponIds);
        characterDto.setReputation(character.getReputation());
        characterDto.setImplantPoints(character.getImplantPoints());
        characterDto.setSpecialImplantPoints(character.getSpecialImplantPoints());
        characterDto.setBattlePoints(character.getBattlePoints());
        characterDto.setCivilPoints(character.getCivilPoints());
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
        log.info("Персонаж {} создан", character.getId());
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
        }
        return toDto(byId.get());
    }

    @Transactional
    public void updateCharacter(UpdateCharacterRequest request, UUID characterId, Authentication auth) {
        CharacterEntity newCharacter = new CharacterEntity();
        CharacterEntity oldCharacter = characterRepo.findById(characterId).orElseThrow(() ->
            new ResponseStatusException(HttpStatus.NOT_FOUND, "Персонаж " + characterId + " не найден"));
        Object principal = auth.getPrincipal();
        User user = (User) principal;
        UUID userid = user.getId();
        if (!oldCharacter.getOwnerId().equals(userid)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Изменить чужого персонажа вздумал? а ты хорош.");
        }
        newCharacter.setId(characterId);
        newCharacter.setOwnerId(user.getId());
        newCharacter.setName(request.getName());
        newCharacter.setAge(request.getAge());
        newCharacter.setReputation(oldCharacter.getReputation());
        newCharacter.setImplantPoints(oldCharacter.getImplantPoints());
        newCharacter.setSpecialImplantPoints(oldCharacter.getSpecialImplantPoints());
        newCharacter.setBattlePoints(oldCharacter.getBattlePoints());
        newCharacter.setCivilPoints(oldCharacter.getCivilPoints());
        characterRepo.save(newCharacter);
    }

    @Transactional
    public void updateCharacterSkill(UpdateCharacterSkillRequest request, UUID characterId, Authentication auth) {
        log.info("Навыки персонажа {} обновляются", characterId);
        CharacterEntity character = characterRepo.findById(characterId).orElseThrow(() ->
            new ResponseStatusException(HttpStatus.NOT_FOUND, "Персонаж " + characterId + " не найден"));
        Object principal = auth.getPrincipal();
        User user = (User) principal;
        UUID userid = user.getId();
        if (not(character.getOwnerId().equals(userid))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Нельзя добавлять навык не своему персонажу!");
        }
        List<Skill> skills = skillRepo.findAllByIdIn(request.getSkillId());
        character.setSkills(skills);
        characterRepo.save(character);
        log.info("Персонажу {} обновлены навыки", character.getId());
    }

    @Transactional
    public void deleteCharacter(UUID characterId, Authentication auth) {
        Optional<CharacterEntity> character = characterRepo.findById(characterId);
        if (character.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Персонаж " + characterId + " не найден");
        }
        // Получить текущего пользователя
        Object principal = auth.getPrincipal();
        User user = (User) principal;
        UUID userid = user.getId();

        // Проверка прав доступа
        if (not(character.get().getOwnerId().equals(userid))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Удалить чужого персонажа вздумал? а ты хорош.");
        }
        characterRepo.deleteById(characterId);
        log.info("Персонаж {} был удалён", characterId);
    }

    @Transactional
    public void putCharacterWeapon(UpdateCharacterWeaponRequest request, UUID characterId, Authentication auth) {
        CharacterEntity character = characterRepo.findById(characterId).orElseThrow(() ->
            new ResponseStatusException(HttpStatus.NOT_FOUND, "Персонаж не найден"));

        // Получить текущего пользователя
        Object principal = auth.getPrincipal();
        User user = (User) principal;
        UUID userid = user.getId();

        // Проверка прав доступа
        if (!character.getOwnerId().equals(userid)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Нельзя добавлять оружие не своему персонажу!");
        }

        // Найти оружие по ID
        Weapon weapon = weaponRepo.findById(request.getWeaponId()).orElse(null);
        if (weapon == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Оружие не найдено");
        }
        character.getWeapons().add(weapon);
        characterRepo.save(character);
    }

    @Transactional
    public void deleteCharacterWeapon(UUID weaponId, UUID characterId, Authentication auth) {
        // Найти персонажа по ID
        CharacterEntity character = characterRepo.findById(characterId).orElseThrow(() ->
            new ResponseStatusException(HttpStatus.NOT_FOUND, "Персонаж не найден"));

        // Получить текущего пользователя
        Object principal = auth.getPrincipal();
        User user = (User) principal;
        UUID userId = user.getId();

        // Проверка прав доступа
        if (!character.getOwnerId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Нельзя удалять оружие не своему персонажу!");
        }

        // Найти оружие по ID
        Weapon weapon = weaponRepo.findById(weaponId).orElseThrow(() ->
            new ResponseStatusException(HttpStatus.NOT_FOUND, "Оружие не найдено"));

        // Удалить оружие из списка персонажа
        if (character.getWeapons().contains(weapon)) {
            character.getWeapons().remove(weapon);
            characterRepo.save(character);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Этого оружия нет в списке вашего персонажа");
        }
    }

}

