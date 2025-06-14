package ru.nightcityroleplay.backend.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.nightcityroleplay.backend.dto.*;
import ru.nightcityroleplay.backend.entity.*;
import ru.nightcityroleplay.backend.repo.CharacterRepository;
import ru.nightcityroleplay.backend.repo.ImplantRepository;
import ru.nightcityroleplay.backend.repo.SkillRepository;
import ru.nightcityroleplay.backend.repo.WeaponRepository;

import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static ru.nightcityroleplay.backend.util.BooleanUtils.not;

@Service
@Slf4j
public class CharacterService {

    private final CharacterRepository characterRepo;
    private final CharacterStatsService characterStatsService;
    private final CharacterClassService characterClassService;
    private final WeaponRepository weaponRepo;
    private final SkillRepository skillRepo;
    private final ImplantRepository implantRepo;
    private final CharacterStatsService statsService;

    public CharacterService(
        CharacterRepository characterRepo,
        CharacterStatsService characterStatsService,
        CharacterClassService characterClassService,
        WeaponRepository weaponRepo,
        SkillRepository skillRepo,
        ImplantRepository implantRepo,
        CharacterStatsService statsService
    ) {
        this.characterStatsService = characterStatsService;
        this.characterClassService = characterClassService;
        this.characterRepo = characterRepo;
        this.weaponRepo = weaponRepo;
        this.skillRepo = skillRepo;
        this.implantRepo = implantRepo;
        this.statsService = statsService;
    }


    private CharacterDto toDto(CharacterEntity character) {
        CharacterDto characterDto = new CharacterDto();
        characterDto.setId(character.getId());
        characterDto.setOwnerId(character.getOwnerId());
        characterDto.setName(character.getName());
        characterDto.setHeight(character.getHeight());
        characterDto.setWeight(character.getWeight());
        characterDto.setAge(character.getAge());
        List<UUID> weaponIds = character.getWeapons().stream()
            .map(Weapon::getId)
            .collect(Collectors.toList());
        characterDto.setOrganization(character.getOrganization());
        characterDto.setCharacterClass(character.getCharacterClass());
        characterDto.setWeaponIds(weaponIds);
        characterDto.setReputation(character.getReputation());
        characterDto.setImplantPoints(statsService.calculateImplantPoints(character.getReputation())
            + characterClassService.bonusFromSolo(character));
        characterDto.setSpecialImplantPoints(statsService.calculateSpecialImplantPoints(character.getReputation()));
        characterDto.setBattlePoints(character.getBattlePoints());
        characterDto.setCivilPoints(character.getCivilPoints());
        return characterDto;
    }

    private ImplantDto implantDto(Implant implant) {
        ImplantDto implantDto = new ImplantDto();
        implantDto.setId(implant.getId());
        implantDto.setName(implant.getName());
        implantDto.setImplantType(implant.getImplantType());
        implantDto.setDescription(implant.getDescription());
        implantDto.setReputationRequirement(implant.getReputationRequirement());
        implantDto.setImplantPointsCost(implant.getImplantPointsCost());
        implantDto.setSpecialImplantPointsCost(implant.getSpecialImplantPointsCost());
        return implantDto;
    }

    @Transactional
    public CreateCharacterResponse createCharacter(CreateCharacterRequest request, Authentication auth) {
        validate(request);
        CharacterEntity character = new CharacterEntity();
        Object principal = auth.getPrincipal();
        User user = (User) principal;
        character.setOwnerId(user.getId());
        character.setName(request.getName());
        character.setHeight(request.getHeight());
        character.setWeight(request.getWeight());
        character.setOrganization(request.getOrganization());
        character.setCharacterClass(request.getCharacterClass());
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
            throw new ResponseStatusException(NOT_FOUND, "Персонаж " + characterId + " не найден");
        }
        return toDto(byId.get());
    }

    @Transactional
    public void updateCharacter(UpdateCharacterRequest request, UUID characterId, Authentication auth) {
        validate(request);
        CharacterEntity newCharacter = new CharacterEntity();
        CharacterEntity character = characterRepo.findById(characterId).orElseThrow(() ->
            new ResponseStatusException(NOT_FOUND, "Персонаж " + characterId + " не найден"));
        Object principal = auth.getPrincipal();
        User user = (User) principal;
        UUID userid = user.getId();
        if (not(character.getOwnerId().equals(userid))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Изменить чужого персонажа вздумал? а ты хорош.");
        }
        newCharacter.setId(characterId);
        newCharacter.setOwnerId(user.getId());
        newCharacter.setName(request.getName());
        newCharacter.setHeight(request.getHeight());
        newCharacter.setWeight(request.getWeight());
        newCharacter.setAge(character.getAge());
        newCharacter.setOrganization(character.getOrganization());
        newCharacter.setCharacterClass(character.getCharacterClass());
        newCharacter.setReputation(character.getReputation());
        characterStatsService.updateCharacterStats(newCharacter);
        characterRepo.save(newCharacter);
        log.info("Персонаж {} изменён", newCharacter.getId());
    }


    @PreAuthorize("hasRole('Role_ADMIN')")
    public void giveReputation(GiveReputationRequest request, UUID characterId, Authentication auth) {
        CharacterEntity character = characterRepo.findById(characterId).orElseThrow(() ->
            new ResponseStatusException(NOT_FOUND, "Персонаж не найден"));

        Object principal = auth.getPrincipal();
        User user = (User) principal;
        int rewardReputation = request.getReputation();
        character.setReputation(character.getReputation() + rewardReputation);
        log.info("{} выдал персонажу {} репутацию {}", user.getUsername(), characterId, rewardReputation);
        characterRepo.save(character);
    }

    @Transactional
    public void updateCharacterSkill(UpdateCharacterSkillRequest request, UUID characterId, Authentication auth) {
        log.info("Навыки персонажа {} обновляются", characterId);
        CharacterEntity character = characterRepo.findById(characterId).orElseThrow(() ->
            new ResponseStatusException(NOT_FOUND, "Персонаж " + characterId + " не найден"));
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
    public List<ImplantDto> getCharacterImplants(UUID characterId) {
        Optional<CharacterEntity> character = characterRepo.findById(characterId);
        if (character.isEmpty()) {
            throw new ResponseStatusException(NOT_FOUND, "Персонаж " + characterId + " не найден");
        }

        List<Implant> implants = character.get().getImplants();
        if (implants == null || implants.isEmpty()) {
            log.info("У персонажа нет имплантов.");
            return Collections.emptyList();
        }

        return implants.stream()
            .map(this::implantDto)
            .collect(Collectors.toList());
    }

    @Transactional
    public void deleteCharacter(UUID characterId, Authentication auth) {
        Optional<CharacterEntity> character = characterRepo.findById(characterId);
        if (character.isEmpty()) {
            throw new ResponseStatusException(NOT_FOUND, "Персонаж " + characterId + " не найден");
        }
        // Получить текущего пользователя
        Object principal = auth.getPrincipal();
        User user = (User) principal;
        UUID userid = user.getId();
        characterRepo.deleteById(characterId);
        log.info("Персонаж {} был удалён Администратором {}", characterId, userid);
    }

    @Transactional
    public void putCharacterWeapon(UpdateCharacterWeaponRequest request, UUID characterId, Authentication auth) {
        // Получение персонажа по ID
        CharacterEntity character = characterRepo.findById(characterId)
            .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Персонаж не найден"));

        // Получить текущего пользователя
        User user = (User) auth.getPrincipal();
        UUID userId = user.getId();

        // Проверка прав доступа
        if (!character.getOwnerId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Нельзя добавлять оружие не своему персонажу!");
        }

        // Получение списка айди оружия из запроса
        Collection<UUID> weaponIds = request.getWeaponIds();

        // Очистить всё оружие у персонажа
        character.getWeapons().clear();

        // Только если weaponIds не пустой — добавить новое оружие
        if (weaponIds != null && !weaponIds.isEmpty()) {
            // Найти все оружия по списку ID
            List<Weapon> weapons = weaponRepo.findAllByIdIn(weaponIds);
            if (weapons.size() != weaponIds.size()) {
                throw new ResponseStatusException(NOT_FOUND, "Оружие не найдено");
            }

            character.getWeapons().addAll(weapons);
        }

        characterRepo.save(character);
    }

    @Transactional
    public void updateCharacterImplants(UpdateCharacterImplantsRequest request, UUID characterId, Authentication auth) {
        CharacterEntity character = characterRepo.findById(characterId).orElseThrow(() ->
            new ResponseStatusException(NOT_FOUND, "Персонаж не найден"));
        User user = (User) auth.getPrincipal();
        UUID userId = user.getId();
        if (!character.getOwnerId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Вы не можете добавлять импланты чужому персонажу");
        }

        // Собираем импланты и проверяем доступные ресурсы
        List<Implant> implantsToAdd = validateAndCollectImplants(request, character);

        // Обновляем характеристики персонажа и сохраняем
        character.getImplants().addAll(implantsToAdd);
        characterRepo.save(character);
    }

    // Проверяет импланты и возвращает список имплантов, которые можно добавить
    private List<Implant> validateAndCollectImplants(
        UpdateCharacterImplantsRequest request,
        CharacterEntity character
    ) throws ResponseStatusException {
        List<Implant> implants = new ArrayList<>();
        int totalImplantPointsCost = 0;
        int totalSpecialImplantPointsCost = 0;

        for (UUID implantId : request.getImplantIds()) {
            Implant implant = implantRepo.findById(implantId).orElseThrow(() ->
                new ResponseStatusException(NOT_FOUND, "Имплант с ID " + implantId + " не найден"));

            if (character.getReputation() < implant.getReputationRequirement()) {
                throw new ResponseStatusException(
                    BAD_REQUEST, "Недостаточная репутация для импланта с ID " + implantId
                );
            }

            // Добавляем имплант в список проверенных
            implants.add(implant);
            totalImplantPointsCost += implant.getImplantPointsCost();
            totalSpecialImplantPointsCost += implant.getSpecialImplantPointsCost();
        }

        // Проверяем, достаточно ли ресурсов у персонажа
        if (statsService.calculateImplantPoints(character.getReputation()) < totalImplantPointsCost) {
            throw new ResponseStatusException(BAD_REQUEST, "Недостаточно ОИ для обычных имплантов");
        }
        if (statsService.calculateSpecialImplantPoints(character.getReputation()) < totalSpecialImplantPointsCost) {
            throw new ResponseStatusException(BAD_REQUEST, "Недостаточно ОИ* для специальных имплантов");
        }

        return implants;
    }

    private void validate(SaveCharacterRequest request) {
        if (request.getAge() == null || request.getAge() <= 0) {
            throw new ResponseStatusException(BAD_REQUEST, "Возраст не может быть 0 или меньше или null");
        }
        if (request.getAge() > 100) {
            throw new ResponseStatusException(BAD_REQUEST, "Возраст не может быть больше 100");
        }
        if (request.getReputation() == null || request.getReputation() < 0) {
            throw new ResponseStatusException(BAD_REQUEST, "Репутация не может быть меньше 0 или null");
        }
        if (request.getReputation() > 40) {
            throw new ResponseStatusException(BAD_REQUEST, "Репутация не может быть больше 40");
        }
        if (characterRepo.existsByName(request.getName())) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Персонаж с таким именем уже есть");
        }
    }
}
