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
import ru.nightcityroleplay.backend.dto.character.*;
import ru.nightcityroleplay.backend.dto.implants.ImplantType;
import ru.nightcityroleplay.backend.dto.implants.ImplantDto;
import ru.nightcityroleplay.backend.entity.*;
import ru.nightcityroleplay.backend.repo.CharacterRepository;
import ru.nightcityroleplay.backend.repo.ImplantRepository;
import ru.nightcityroleplay.backend.repo.SkillRepository;
import ru.nightcityroleplay.backend.repo.WeaponRepository;

import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@Slf4j
public class CharacterService {

    private final CharacterRepository characterRepo;
    private final CharacterStatsService characterStatsService;
    private final CharacterClassService characterClassService;
    private final WeaponRepository weaponRepo;
    private final SkillRepository skillRepo;
    private final ImplantRepository implantRepo;

    public CharacterService(
        CharacterRepository characterRepo,
        CharacterStatsService characterStatsService,
        CharacterClassService characterClassService,
        WeaponRepository weaponRepo,
        SkillRepository skillRepo,
        ImplantRepository implantRepo
    ) {
        this.characterStatsService = characterStatsService;
        this.characterClassService = characterClassService;
        this.characterRepo = characterRepo;
        this.weaponRepo = weaponRepo;
        this.skillRepo = skillRepo;
        this.implantRepo = implantRepo;
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
        characterDto.setImplantPoints(characterStatsService.calculateImplantPoints(character.getReputation())
            + characterClassService.bonusFromSolo(character));
        characterDto.setSpecialImplantPoints(characterStatsService.calculateSpecialImplantPoints
            (character.getReputation()));
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
        if (request.getReputation() == null || request.getReputation() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Репутация не может быть меньше 0 или null");
        }
        if (request.getReputation() > 40) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Репутация не может быть больше 40");
        }
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
    public void updateCharacter(UpdateCharacterRequest request, UUID characterId) {
        validate(request);
        CharacterEntity newCharacter = new CharacterEntity();
        CharacterEntity character = characterRepo.findById(characterId).orElseThrow(() ->
            new ResponseStatusException(HttpStatus.NOT_FOUND, "Персонаж " + characterId + " не найден"));
        newCharacter.setId(characterId);
        newCharacter.setOwnerId(character.getOwnerId());
        newCharacter.setName(request.getName());
        newCharacter.setHeight(request.getHeight());
        newCharacter.setWeight(request.getWeight());
        newCharacter.setAge(request.getAge());
        newCharacter.setOrganization(request.getOrganization());
        newCharacter.setCharacterClass(request.getCharacterClass());
        newCharacter.setReputation(character.getReputation());
        characterStatsService.updateCharacterStats(newCharacter);
        characterRepo.save(newCharacter);
        log.info("Персонаж {} изменён", newCharacter.getId());
    }


    @PreAuthorize("hasRole('Role_ADMIN')")
    public void giveReputation(GiveReputationRequest request, UUID characterId, Authentication auth) {
        CharacterEntity character = characterRepo.findById(characterId).orElseThrow(() ->
            new ResponseStatusException(HttpStatus.NOT_FOUND, "Персонаж не найден"));

        Object principal = auth.getPrincipal();
        User user = (User) principal;
        int rewardReputation = request.getReputation();
        character.setReputation(character.getReputation() + rewardReputation);
        log.info("{} выдал персонажу {} репутацию {}", user.getUsername(), characterId, rewardReputation);
        characterRepo.save(character);
    }

    @Transactional
    public void updateCharacterSkill(UpdateCharacterSkillsRequest request, UUID characterId) {
        log.info("Навыки персонажа {} обновляют ся", characterId);
        CharacterEntity character = characterRepo.findById(characterId).orElseThrow(() ->
            new ResponseStatusException(HttpStatus.NOT_FOUND, "Персонаж " + characterId + " не найден"));
        List<Skill> skills = new ArrayList<>();
        int totalBattlePoints = 0;
        int totalCivilPoints = 0;
        // Проверка наличия навыка и суммирование стоимости
        for (UUID skillId : request.getSkillIds()) {
            Skill skill = skillRepo.findById(skillId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Навык с ID" + skillId + "не найден"));
            if (character.getReputation() < skill.getReputationRequirement()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Данный уровень навыка не доступен на репутации изменяемого персонажа");
            }
            skills.add(skill);
            totalBattlePoints += skill.getBattleCost();
            totalCivilPoints += skill.getCivilCost();
        }
        if (character.getBattlePoints() < totalBattlePoints) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Недостаточно БО для выбранного уровня навыка");
        }
        if (character.getCivilPoints() < totalCivilPoints) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Недостаточно МО для выбранного уровня навыка");
        }
        // Создаем или обновляем список имплантов персонажа
        if (character.getSkills() == null) {
            character.setSkills(new ArrayList<>());
        }
        character.getSkills().addAll(skills);
        characterRepo.save(character);
    }

    @Transactional
    public void selectInitialCharacterSkills(UpdateCharacterSkillsRequest request,
                                             UUID characterId, Authentication auth) {
        log.info("Персонаж {} выбирает стартовые навыки", characterId);

        // Получение персонажа
        CharacterEntity character = characterRepo.findById(characterId).orElseThrow(() ->
            new ResponseStatusException(HttpStatus.NOT_FOUND, "Персонаж " + characterId + " не найден"));

        // Проверка владельца персонажа
        Object principal = auth.getPrincipal();
        User user = (User) principal;
        UUID userId = user.getId();
        if (!character.getOwnerId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Нельзя добавлять навык не своему персонажу!");
        }

        List<Skill> skills = new ArrayList<>();
        int totalBattlePoints = 0;
        int totalCivilPoints = 0;

        if (character.getSkills().isEmpty()) {
            // Проверяем, что все навыки, которые выбираются, находятся на первом уровне
            for (UUID skillId : request.getSkillIds()) {
                Skill skill = skillRepo.findById(skillId).orElseThrow(() ->
                    new ResponseStatusException(HttpStatus.NOT_FOUND, "Навык с ID " + skillId + " не найден"));

                if (skill.getLevel() != 1) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Можно выбирать только навыки первого уровня!");
                }
                skills.add(skill);
                totalBattlePoints += skill.getBattleCost();
                totalCivilPoints += skill.getCivilCost();
            }
            if (character.getBattlePoints() < totalBattlePoints) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Недостаточно БО для выбранных навыков. Сбавь колличество навыков и повтори попытку");
            }
            if (character.getCivilPoints() < totalCivilPoints) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Недостаточно МО для выбранных навыков. Сбавь колличество навыков и повтори попытку");
            }
            if (character.getSkills() == null) {
                character.setSkills(new ArrayList<>());
            }
            character.getSkills().addAll(skills);
            characterRepo.save(character);
        } else throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Вы уже выбрали первые навыки");
    }

    @Transactional
    public void upgradeCharacterSkill(UpgradeCharacterSkillRequest request, UUID characterId, Authentication auth) {
        log.info("Навыки персонажа {} обновляются", characterId);
        CharacterEntity character = characterRepo.findById(characterId).orElseThrow(() ->
            new ResponseStatusException(HttpStatus.NOT_FOUND, "Персонаж " + characterId + " не найден"));

        User user = (User) auth.getPrincipal();
        UUID userId = user.getId();

        if (!character.getOwnerId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Нельзя добавлять навык не своему персонажу!");
        }

        List<Skill> skillsToAdd = new ArrayList<>();
        int totalBattlePoints = 0;
        int totalCivilPoints = 0;

        for (UUID skillId : request.getSkillIds()) {
            Skill currentSkill = skillRepo.findById(skillId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Навык с ID " + skillId + " не найден"));
            if (character.getReputation() < currentSkill.getReputationRequirement()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Нынешний уровень навыка не доступен на вашей репутации");
            }
            // Определяем следующий уровень навыка на основе его текущего уровня
            int nextLevel = currentSkill.getLevel() + 1;
            if (nextLevel > 10) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Достигнут максимальный уровень навыка");
            }
            Skill nextSkill = skillRepo.findBySkillFamilyAndLevel(currentSkill.getSkillFamily(), nextLevel)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Навык уровня " + nextLevel + " не найден"));
            // Проверка репутации для следующего уровня навыка
            if (character.getReputation() < nextSkill.getReputationRequirement()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Следующий уровень навыка не доступен на вашей репутации");
            }
            Skill existingSkill = character.getSkills().stream()
                .filter(s -> s.getId().equals(currentSkill.getId()))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Текущий навык не найден у персонажа"));
            character.getSkills().remove(existingSkill);
            skillsToAdd.add(nextSkill);
            totalBattlePoints += nextSkill.getBattleCost() - existingSkill.getBattleCost();
            totalCivilPoints += nextSkill.getCivilCost() - existingSkill.getCivilCost();
        }
        if (character.getBattlePoints() < totalBattlePoints) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Недостаточно БО для выбранного уровня навыка");
        }
        if (character.getCivilPoints() < totalCivilPoints) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Недостаточно МО для выбранного уровня навыка");
        }
        character.getSkills().addAll(skillsToAdd);
        characterRepo.save(character);
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

        Map<ImplantType, List<Implant>> implantsToAdd = validateAndCollectImplants(request, character);

        for (List<Implant> implantList : implantsToAdd.values()) {
            character.getImplants().addAll(implantList);
        }
        characterRepo.save(character);
    }

    private Map<ImplantType, List<Implant>> validateAndCollectImplants(
        UpdateCharacterImplantsRequest request,
        CharacterEntity character
    ) throws ResponseStatusException {
        Map<ImplantType, List<Implant>> implantMap = new HashMap<>();
        int totalImplantPointsCost = 0;
        int totalSpecialImplantPointsCost = 0;
        Map<ImplantType, Integer> currentImplantTypeCounts = new HashMap<>();
        for (Implant existing : character.getImplants()) {
            ImplantType type = existing.getImplantType();
            currentImplantTypeCounts.put(type, currentImplantTypeCounts.getOrDefault(type, 0) + 1);
        }

        for (UUID implantId : request.getImplantIds()) {
            Implant implant = implantRepo.findById(implantId).orElseThrow(() ->
                new ResponseStatusException(NOT_FOUND, "Имплант с ID " + implantId + " не найден"));

            if (character.getReputation() < implant.getReputationRequirement()) {
                throw new ResponseStatusException(
                    BAD_REQUEST, "Недостаточная репутация для импланта с ID " + implantId
                );
            }

            ImplantType type = implant.getImplantType();
            if (type == null) {
                throw new ResponseStatusException(
                    BAD_REQUEST, "Тип импланта не определён для импланта с ID " + implantId
                );
            }
            int newCount = currentImplantTypeCounts.getOrDefault(type, 0)
                + implantMap.getOrDefault(type, new ArrayList<>()).size() + 1;
            if (newCount > type.getLimit()) {
                throw new ResponseStatusException(
                    BAD_REQUEST, "Лимит имплантов типа " + type.name() + " превышен"
                );
            }
            implantMap.computeIfAbsent(type, k -> new ArrayList<>()).add(implant);

            totalImplantPointsCost += implant.getImplantPointsCost();
            totalSpecialImplantPointsCost += implant.getSpecialImplantPointsCost();
        }

        // Проверяем, достаточно ли ресурсов у персонажа
        if (characterStatsService.calculateImplantPoints(character.getReputation()) < totalImplantPointsCost) {
            throw new ResponseStatusException(BAD_REQUEST, "Недостаточно ОИ для обычных имплантов");
        }
        if (characterStatsService.calculateSpecialImplantPoints(character.getReputation())
            < totalSpecialImplantPointsCost) {
            throw new ResponseStatusException(BAD_REQUEST, "Недостаточно ОИ* для специальных имплантов");
        }

        return implantMap;
    }


    private void validate(SaveCharacterRequest request) {
        if (request.getAge() == null || request.getAge() <= 0) {
            throw new ResponseStatusException(BAD_REQUEST, "Возраст не может быть 0 или меньше или null");
        }
        if (request.getAge() > 100) {
            throw new ResponseStatusException(BAD_REQUEST, "Возраст не может быть больше 100");
        }
        if (characterRepo.existsByName(request.getName())) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Персонаж с таким именем уже есть");
        }
    }
}

