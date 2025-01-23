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

import static ru.nightcityroleplay.backend.util.BooleanUtils.not;

@Service
@Slf4j
public class CharacterService {

    private final CharacterRepository characterRepo;
    private final CharacterStatsService characterStatsService;
    private final WeaponRepository weaponRepo;
    private final SkillRepository skillRepo;
    private final ImplantRepository implantRepo;

    public CharacterService(
        CharacterRepository characterRepo,
        CharacterStatsService characterStatsService,
        WeaponRepository weaponRepo,
        SkillRepository skillRepo,
        ImplantRepository implantRepo
    ) {
        this.characterStatsService = characterStatsService;
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
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Персонаж " + characterId + " не найден");
        }
        return toDto(byId.get());
    }

    @Transactional
    public void updateCharacter(UpdateCharacterRequest request, UUID characterId, Authentication auth) {
        validate(request);
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
        newCharacter.setReputation(request.getReputation());
        newCharacter.setImplantPoints(character.getImplantPoints());
        newCharacter.setSpecialImplantPoints(character.getSpecialImplantPoints());
        newCharacter.setBattlePoints(character.getBattlePoints());
        newCharacter.setCivilPoints(character.getCivilPoints());
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
        List<Skill> skills = new ArrayList<>();
        int totalBattlePoints = 0;
        int totalCivilPoints = 0;
        // Проверка наличия навыка и суммирование стоимости

        for (UUID skillId : request.getSkillId()) {
            Skill skill = skillRepo.findById(skillId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Навык с ID" + skillId + "не найден"));
            if (character.getReputation() < skill.getReputationRequirement()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Данный уровень навыка не доступен на вашей репутации");
            }
            skills.add(skill);
            totalBattlePoints += skill.getBattleCost();
            totalCivilPoints += skill.getCivilCost();
        }
        if (character.getBattlePoints() < totalBattlePoints) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Недостаточно БО для выбранного уровня навыка");
        }
        if (character.getCivilPoints() < totalCivilPoints){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Недостаточно МО для выбранного уровня навыка");
        }
        // Создаем или обновляем список имплантов персонажа
        if (character.getSkills() == null) {
            character.setSkills(new ArrayList<>());
        }
        character.getSkills().addAll(skills);
        character.setBattlePoints(character.getBattlePoints() - totalBattlePoints);
        character.setCivilPoints(character.getCivilPoints() - totalCivilPoints);
        characterRepo.save(character);
    }

    @Transactional
    public List<ImplantDto> getCharacterImplants(UUID characterId) {
        Optional<CharacterEntity> character = characterRepo.findById(characterId);
        if (character.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Персонаж " + characterId + " не найден");
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
        // Получение персонажа по ID
        CharacterEntity character = characterRepo.findById(characterId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Персонаж не найден"));

        // Получить текущего пользователя
        User user = (User) auth.getPrincipal();
        UUID userId = user.getId();

        // Проверка прав доступа
        if (!character.getOwnerId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Нельзя добавлять оружие не своему персонажу!");
        }

        // Получение списка айди оружия из запроса
        Collection<UUID> weaponIds = request.getWeaponIds(); // Предполагается, что в запросе есть метод getWeaponIds()

        // Найти все оружия по списку ID
        List<Weapon> weapons = weaponRepo.findAllByIdIn(weaponIds);
        if (weapons.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Оружие не найдено");
        }

        // Добавьте все найденные оружия к персонажу
        for (Weapon weapon : weapons) {
            if (!character.getWeapons().contains(weapon)) {
                character.getWeapons().add(weapon);
            }
        }

        characterRepo.save(character);
    }

    @Transactional
    public void putCharacterImplant(UpdateCharacterImplantRequest request, UUID characterId, Authentication auth) {
        CharacterEntity character = characterRepo.findById(characterId).orElseThrow(() ->
            new ResponseStatusException(HttpStatus.NOT_FOUND, "Персонаж не найден"));

        // Получить текущего пользователя
        Object principal = auth.getPrincipal();
        User user = (User) principal;
        UUID userid = user.getId();

        // Проверка прав доступа
        if (!character.getOwnerId().equals(userid)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Нельзя добавлять имплант не своему персонажу!");
        }

        // Списки для проверок
        List<Implant> implants = new ArrayList<>();
        int totalImplantPointsCost = 0;
        int totalSpecialImplantPointsCost = 0;

        // Проверка наличия имплантов и суммируем стоимости
        for (UUID implantId : request.getImplantId()) {
            Implant implant = implantRepo.findById(implantId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Имплант с ID " + implantId + " не найден"));

            if (character.getReputation() < implant.getReputationRequirement()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Данный имплант не доступен на вашей репутации");
            }
            // попытка добавить имплант в список
            implants.add(implant);
            totalImplantPointsCost += implant.getImplantPointsCost();
            totalSpecialImplantPointsCost += implant.getSpecialImplantPointsCost();
        }

        // Проверка наличия у персонажа нужных очков
        if (character.getImplantPoints() < totalImplantPointsCost) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Недостаточно ОИ для обычных имплантов");
        }
        if (character.getSpecialImplantPoints() < totalSpecialImplantPointsCost) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Недостаточно ОИ* для специальных имплантов");
        }

        // Создаем или обновляем список имплантов персонажа
        if (character.getImplants() == null) {
            character.setImplants(new ArrayList<>());
        }
        character.getImplants().addAll(implants);
        character.setImplantPoints(character.getImplantPoints() - totalImplantPointsCost);
        character.setSpecialImplantPoints(character.getSpecialImplantPoints() - totalSpecialImplantPointsCost);
        characterRepo.save(character);
    }


    @Transactional
    public void deleteCharacterImplant(UUID implantId, UUID characterId, Authentication auth) {
        CharacterEntity character = characterRepo.findById(characterId).orElseThrow(() ->
            new ResponseStatusException(HttpStatus.NOT_FOUND, "Персонаж не найден"));

        Object principal = auth.getPrincipal();
        User user = (User) principal;
        UUID userid = user.getId();

        if (!character.getOwnerId().equals(userid)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Нельзя добавлять имплант не своему персонажу!");
        }
        // Найти Имплант по ID
        Implant implant = implantRepo.findById(implantId).orElseThrow(() ->
            new ResponseStatusException(HttpStatus.NOT_FOUND, "Имплант не найден"));

        List<Implant> implants = character.getImplants();
        boolean hasImplant = false;
        for (Implant value : implants) {
            if (value.getId().equals(implant.getId())) {
                hasImplant = true;
                break;
            }
        }
        if (!hasImplant) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Этого импланта нет в вашем списке.");
        }
        implants.remove(implant);
        character.setImplantPoints(character.getImplantPoints() + implant.getImplantPointsCost());
        character.setSpecialImplantPoints(character.getSpecialImplantPoints() + implant.getSpecialImplantPointsCost());
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

    private void validate(SaveCharacterRequest request) {
        if (request.getAge() == null || request.getAge() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Возраст не может быть 0 или меньше или null");
        }
        if (request.getReputation() == null || request.getReputation() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Репутация не может быть меньше 0 или null");
        }
        if (characterRepo.existsByName(request.getName())) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Персонаж с таким именем уже есть");
        }
    }
}
