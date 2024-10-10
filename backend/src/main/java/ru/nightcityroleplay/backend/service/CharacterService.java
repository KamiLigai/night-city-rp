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
import ru.nightcityroleplay.backend.exception.NightCityRpException;
import ru.nightcityroleplay.backend.repo.CharacterRepository;
import ru.nightcityroleplay.backend.repo.SkillRepository;
import ru.nightcityroleplay.backend.repo.WeaponRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static ru.nightcityroleplay.backend.util.BooleanUtils.not;


@Service
@Slf4j
public class CharacterService {
    private final CharacterRepository characterRepo;
    private final WeaponRepository weaponRepo;
    private final SkillRepository skillRepo;

    public CharacterService(CharacterRepository characterRepo, WeaponRepository weaponRepo, SkillRepository skillRepo) {
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
        CharacterEntity character = characterRepo.findById(characterId).orElseThrow(() ->
            new ResponseStatusException(HttpStatus.NOT_FOUND, "Персонаж " + characterId + " не найден"));
        CharacterEntity oldCharacter = characterRepo.findById(characterId).get();
        Object principal = auth.getPrincipal();
        User user = (User) principal;
        UUID userid = user.getId();
        if (!oldCharacter.getOwnerId().equals(userid)) {
            throw new NightCityRpException("Изменить чужого персонажа вздумал? а ты хорош.");
        } else {
            newCharacter.setId(characterId);
            newCharacter.setOwnerId(user.getId());
            newCharacter.setName(request.getName());
            newCharacter.setAge(request.getAge());
            characterRepo.save(newCharacter);
            if (not(character.getOwnerId().equals(userid))) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Изменить чужого персонажа вздумал? а ты хорош.");
            }
            log.info("Персонаж {} изменён", newCharacter.getId());
        }
    }

    @Transactional
    public void updateCharacterSkill(UpdateCharacterSkillRequest request, UUID characterId, Authentication auth) {
        log.info("Навыки персонажа {} обновляются", characterId);
        CharacterEntity character = characterRepo.findById(characterId).orElseThrow(() ->
            new ResponseStatusException(HttpStatus.NOT_FOUND, "Персонаж " + characterId + " не найден"));

        // Получить текущего пользователя
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
    public void getCharacterWeapon(UUID characterId) {
        // Проверяем, существует ли персонаж с данным ID
        CharacterEntity character = characterRepo.findById(characterId)
            .orElseThrow(() -> new RuntimeException("Character not found"));

        // Возвращаем список оружий, связанных с этим персонажем
        weaponRepo.findByCharsId(character);
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
        // Проверка на наличие оружия и Создание нового списка оружия для персонажа
        if (character.getWeapons() == null) {
            List<Weapon> weapons = new ArrayList<>();
            weapons.add(weapon);
            character.setWeapons(weapons);
        } else {
            character.getWeapons().add(weapon);
        }
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
        if (character.getWeapons() != null && character.getWeapons().contains(weapon)) {
            character.getWeapons().remove(weapon);
            characterRepo.save(character);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Этого оружия нет в списке вашего персонада");
        }
    }

}

