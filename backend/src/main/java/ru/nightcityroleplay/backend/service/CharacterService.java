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
import ru.nightcityroleplay.backend.repo.CharacterRepository;
import ru.nightcityroleplay.backend.repo.SkillRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static ru.nightcityroleplay.backend.util.BooleanUtils.not;

/*
Дано: Пустая бд.
Действие: Добавить персонажа методом POST /characters.
Ожидается: Персонаж добавлен в бд.


Дано: Пустая бд.
Действие: Добавить персонажа методом POST /characters с некорректными данными.
Ожидается: 400 Bad_Request.
           Новый персонаж не создан в бд.


Дано: Персонаж с определённым именем.
Действие: Добавить нового персонажа с таким же именем методом POST /characters.
Ожидается: 422 UNPROCESSABLE_ENTITY.
           Новый персонаж не создан в бд.


Дано: Персонаж с id.
Действие: Удалить персонажа методом DELETE /characters/{id}.
Ожидается: Персонаж удалён из бд.


Дано: Персонаж 1.
Действие: Удалить персонажа 2 методом DELETE /characters/{id}.
Ожидается: 404 Not Found.
           Никакой персонаж не удалён.


Дано: Персонаж владельца 1.
Действие: Удалить персонажа владельца 1 методом DELETE /characters/{id} от имени владельца 2.
Ожидается: 403 Forbidden.
           Никакой персонаж не удалён.


Дано: Персонаж.
Действие: Получить персонажа методом GET /characters/{id}.
Ожидается: Получены данные персонажа


Дано: Персонаж 1.
Действие: Получить персонажа 2 методом GET /characters/{id}.
Ожидается: 404 Not found.
           Данные персонажа не получены.


Дано: Несколько персонажей.
Действие: Получить всех персонажей методом GET /characters.
Ожидается: Получены данные всех персонажей


Дано: Несколько персонажей
Действие: Получить страницу персонажей методом GET /characters.
          Размер страницы меньше чем персонажей в бд.
Ожидается: Получена страница данных персонажей.


Дано: Пустая бд.
Действие: Получить всех персонажей методом GET /characters.
Ожидается: Пустой ответ.


Дано: Персонаж с id.
Действие: Изменить персонажа по id методом PUT /characters/{id}.
Ожидается: Персонаж в бд обновлен.


Дано: Персонаж персонаж отсутствует
Действие: Изменить персонажа по id методом PUT /characters/{id}
Ожидается: Ошибка 404, персонаж не найден


Дано: Персонаж с id.
Действие: Изменить персонажа по id методом PUT /characters/{id} с некорректными данными.
Ожидается: 400 Bad_Request.
           Никакой персонаж не был изменён.


Дано: Персонаж юзера 1.
Действие: Юзер 2 изменяет персонажа по id методом PUT /characters/{id}.
Ожидается: Ошибка 403, нельзя менять чужого персонажа.
           Никакой персонаж не был изменён.


Дано: Персонаж с id.
Действие: Изменить персонажа по id методом PUT /characters/{id} без аутентификации.
Ожидается: Ошибка 401, юзер не аутентифицирован.
           Никакой персонаж не был изменён.
 */

@Service
@Slf4j
public class CharacterService {

    private final CharacterRepository characterRepo;
    private final SkillRepository skillRepo;

    public CharacterService(CharacterRepository characterRepo, SkillRepository skillRepo) {
        this.characterRepo = characterRepo;
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
        if (request.getAge() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Возраст не может быть null");
        }
        if (characterRepo.existsByName(request.getName())) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Персонаж с таким именем уже есть");
        }
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
        Object principal = auth.getPrincipal();
        User user = (User) principal;
        UUID userid = user.getId();
        if (not(character.getOwnerId().equals(userid))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Изменить чужого персонажа вздумал? а ты хорош.");
        }
        newCharacter.setId(characterId);
        newCharacter.setOwnerId(user.getId());
        newCharacter.setName(request.getName());
        newCharacter.setAge(request.getAge());
        characterRepo.save(newCharacter);
        log.info("Персонаж {} изменён", newCharacter.getId());
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
        Object principal = auth.getPrincipal();
        User user = (User) principal;
        UUID userid = user.getId();
        if (not(character.get().getOwnerId().equals(userid))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Удалить чужого персонажа вздумал? а ты хорош.");
        }
        characterRepo.deleteById(characterId);
        log.info("Персонаж {} был удалён", characterId);
    }
}




