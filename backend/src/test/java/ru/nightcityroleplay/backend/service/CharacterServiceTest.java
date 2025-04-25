package ru.nightcityroleplay.backend.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.server.ResponseStatusException;
import ru.nightcityroleplay.backend.dto.*;
import ru.nightcityroleplay.backend.dto.character.*;
import ru.nightcityroleplay.backend.dto.implants.ImplantDto;
import ru.nightcityroleplay.backend.entity.*;
import ru.nightcityroleplay.backend.repo.CharacterRepository;
import ru.nightcityroleplay.backend.repo.ImplantRepository;
import ru.nightcityroleplay.backend.repo.SkillRepository;
import ru.nightcityroleplay.backend.repo.WeaponRepository;
import ru.nightcityroleplay.backend.util.Call;

import java.util.*;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Slf4j
class CharacterServiceTest {

    CharacterService service;
    CharacterStatsService characterStatsService;
    CharacterClassService characterClassService;
    WeaponRepository weaponRepo;
    CharacterRepository charRepo;
    SkillRepository skillRepo;
    private Pageable pageable;
    ImplantRepository implantRepo;

    @BeforeEach
    void setUp() {
        characterClassService = mock();
        weaponRepo = mock();
        pageable = mock();
        charRepo = mock();
        skillRepo = mock();
        implantRepo = mock();

        service = new CharacterService(charRepo, characterStatsService, characterClassService, weaponRepo, skillRepo, implantRepo);
    }

    @Test
    void getCharacter_characterIsAbsent_throw404() {
        // given
        UUID id = randomUUID();
        when(charRepo.findById(id))
            .thenReturn(Optional.empty());

        // then
        assertThatThrownBy(() -> service.getCharacter(id))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("Персонаж " + id + " не найден")
            .extracting(ResponseStatusException.class::cast)
            .extracting(ErrorResponseException::getStatusCode)
            .isEqualTo(HttpStatus.NOT_FOUND);

    }

    @Test
    void createCharacter_ShouldThrowUnprocessableEntity_WhenCharacterNameExists() {
        // given
        var request = new CreateCharacterRequest();
        request.setName("Илон");
        request.setAge(8);
        request.setReputation(0);

        Authentication auth = mock(Authentication.class);
        User user = new User();
        user.setId(UUID.randomUUID());
        when(auth.getPrincipal()).thenReturn(user);
        when(charRepo.existsByName(request.getName())).thenReturn(true);

        // when & then
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            service.createCharacter(request, auth);
        });
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, exception.getStatusCode());
        assertEquals("Персонаж с таким именем уже есть", exception.getReason());
    }

    @Test
    void createCharacter_ageIsNull_throw400() {
        // given
        var request = new CreateCharacterRequest();
        request.setName("Илон");
        request.setAge(null);

        Authentication auth = mock(Authentication.class);
        User user = new User();
        user.setId(UUID.randomUUID());
        when(auth.getPrincipal()).thenReturn(user);

        // when & then
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            service.createCharacter(request, auth);
        });
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Возраст не может быть 0 или меньше или null", exception.getReason());
    }

    @Test
    void createCharacter_characterExists_success() {
        // given
        var request = new CreateCharacterRequest();
        request.setName("Илон");
        request.setAge(8);
        request.setReputation(0);

        Authentication auth = mock();
        User user = new User();

        when(auth.getPrincipal())
            .thenReturn(user);

        UUID id = randomUUID();
        var character = new CharacterEntity();
        character.setId(id);
        when(charRepo.save(any()))
            .thenReturn(character);

        // when
        service.createCharacter(request, auth);

        // then
        verify(charRepo).save(any());
    }

    @Test
    void getCharacter_characterData_isNotNull() {
        // given
        UUID owId = randomUUID();
        UUID charId = randomUUID();
        CharacterEntity character = new CharacterEntity();
        //Weapon someWeapon = mock();
        List<Weapon> weapons = List.of(Mockito.<Weapon>mock());
        character.setOwnerId(owId);
        character.setId(charId);
        character.setName("Vasyatka");
        character.setAge(42);
        character.setReputation(0);
        character.setWeapons(weapons);

        when(charRepo.findById(charId))
            .thenReturn(Optional.of(character));

        // when
        var result = service.getCharacter(charId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getOwnerId()).isEqualTo(owId);
        assertThat(result.getId()).isEqualTo(charId);
        assertThat(result.getName()).isEqualTo("Vasyatka");
        assertThat(result.getAge()).isEqualTo(42);
        assertThat(result.getWeaponIds()).isNotEmpty();
    }

    @Test
    void getCharacterPage_characterPageExists_success() {
        // given
        CharacterEntity character1 = new CharacterEntity();
        character1.setId(UUID.randomUUID());
        character1.setName("Character 1");
        character1.setWeapons(new ArrayList<>());
        character1.setReputation(5);

        CharacterEntity character2 = new CharacterEntity();
        character2.setId(UUID.randomUUID());
        character2.setName("Character 2");
        character2.setWeapons(new ArrayList<>());
        character2.setReputation(5);

        List<CharacterEntity> characterList = List.of(character1, character2);
        Page<CharacterEntity> characterPage = new PageImpl<>(characterList, pageable, characterList.size());

        when(charRepo.findAll(pageable)).thenReturn(characterPage);

        // when
        Page<CharacterDto> result = service.getCharacterPage(pageable);

        // then
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Character 1");
        assertThat(result.getContent().get(1).getName()).isEqualTo("Character 2");
    }

    @Test
    void getCharacterPage_characterPageNotExists_isEmpty() {
        // given
        Page<CharacterEntity> characterPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
        when(charRepo.findAll(pageable)).thenReturn(characterPage);

        // when
        Page<CharacterDto> result = service.getCharacterPage(pageable);

        // then
        assertThat(result.getTotalElements()).isEqualTo(0);
        assertThat(result.getContent()).isEmpty();
    }


    @Test
    void updateCharacter_characterIsAbsent_throw404() {
        // given
        var request = new UpdateCharacterRequest();
        UUID characterId = UUID.randomUUID();
        request.setAge(1);
        request.setName(randomUUID().toString());

        when(charRepo.findById(characterId)).thenReturn(Optional.empty());

        // then
        assertThatThrownBy(() -> service.updateCharacter(request, characterId))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("Персонаж " + characterId + " не найден")
            .extracting(ResponseStatusException.class::cast)
            .extracting(ErrorResponseException::getStatusCode)
            .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void updateCharacterSkill_characterNotExists_throw404() {
        // given
        var request = new UpdateCharacterSkillRequest();
        UUID characterId = UUID.randomUUID();

        when(charRepo.findById(characterId)).thenReturn(Optional.empty());

        // then
        assertThatThrownBy(() -> service.updateCharacterSkill(request, characterId))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("Персонаж " + characterId + " не найден")
            .extracting(ResponseStatusException.class::cast)
            .extracting(ErrorResponseException::getStatusCode)
            .isEqualTo(HttpStatus.NOT_FOUND);

    }

    @Test
    void firstSelectCharacterSkill_unauthorized_throw403() {
        // given
        var request = new UpgradeCharacterSkillRequest();
        UUID characterId = UUID.randomUUID();

        var oldCharacter = new CharacterEntity();
        oldCharacter.setId(characterId);
        oldCharacter.setOwnerId(UUID.randomUUID()); // Персонаж принадлежит другому пользователю

        var user = new User();
        user.setId(UUID.randomUUID()); // Не совпадает с ownerId персонажа

        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(user);

        when(charRepo.findById(characterId)).thenReturn(Optional.of(oldCharacter));

        // then
        assertThatThrownBy(() -> service.upgradeCharacterSkill(request, characterId, auth))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("Нельзя добавлять навык не своему персонажу!")
            .extracting(ResponseStatusException.class::cast)
            .extracting(ResponseStatusException::getStatusCode)
            .isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void upgradeCharacterSkill_skillNotFound_throw404() {
        // given
        var request = new UpgradeCharacterSkillRequest();
        request.setSkillIds(List.of(UUID.randomUUID()));
        UUID characterId = UUID.randomUUID();

        var oldCharacter = new CharacterEntity();
        oldCharacter.setId(characterId);
        oldCharacter.setOwnerId(UUID.randomUUID());

        var user = new User();
        user.setId(oldCharacter.getOwnerId());

        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(user);

        when(charRepo.findById(characterId)).thenReturn(Optional.of(oldCharacter));
        when(skillRepo.findById(any(UUID.class))).thenReturn(Optional.empty()); // Навык не найден

        // then
        assertThatThrownBy(() -> service.upgradeCharacterSkill(request, characterId, auth))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("Навык с ID")
            .hasMessageContaining("не найден")
            .extracting(ResponseStatusException.class::cast)
            .extracting(ResponseStatusException::getStatusCode)
            .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void upgradeCharacterSkill_insufficientBattlePoints_throw400() {
        // given
        var request = new UpgradeCharacterSkillRequest();
        UUID skillId = UUID.randomUUID();
        request.setSkillIds(List.of(skillId));

        UUID characterId = UUID.randomUUID();

        var character = new CharacterEntity();
        character.setId(characterId);
        character.setOwnerId(UUID.randomUUID());
        character.setReputation(0);
        character.setBattlePoints(10); // Недостаточно боевых очков

        var user = new User();
        user.setId(character.getOwnerId());

        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(user);

        var currentSkill = new Skill();
        currentSkill.setId(skillId);
        currentSkill.setSkillFamily("long_blade");
        currentSkill.setLevel(1); // Текущий уровень навыка
        currentSkill.setReputationRequirement(0); // Требуемая репутация меньше текущей
        currentSkill.setBattleCost(1);
        currentSkill.setCivilCost(0);

        var nextSkill = new Skill();
        nextSkill.setId(UUID.randomUUID());
        nextSkill.setSkillFamily("long_blade");
        nextSkill.setLevel(currentSkill.getLevel()+1); // Следующий уровень навыка
        nextSkill.setReputationRequirement(0); // Требуемая репутация меньше текущей
        nextSkill.setBattleCost(20);
        nextSkill.setCivilCost(0);


        character.setSkills(new ArrayList<>()); // Персонаж уже обладает текущим навыком
        character.getSkills().add(currentSkill);


        when(charRepo.findById(characterId)).thenReturn(Optional.of(character));
        when(skillRepo.findById(skillId)).thenReturn(Optional.of(currentSkill));
        when(skillRepo.findBySkillFamilyAndLevel(any(), eq(2))).thenReturn(Optional.of(nextSkill));

        // then
        assertThatThrownBy(() -> service.upgradeCharacterSkill(request, characterId, auth))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("Недостаточно БО для выбранного уровня навыка")
            .extracting(ResponseStatusException.class::cast)
            .extracting(ResponseStatusException::getStatusCode)
            .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void deleteCharacter_characterNotExists_throw404() {
        // given
        UUID characterId = UUID.randomUUID();
        Authentication authentication = mock(Authentication.class);

        // when
        when(charRepo.findById(characterId)).thenReturn(java.util.Optional.empty());

        // then
        assertThatThrownBy(() -> service.deleteCharacter(characterId, authentication))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("Персонаж " + characterId + " не найден")
            .extracting(ResponseStatusException.class::cast)
            .extracting(ErrorResponseException::getStatusCode)
            .isEqualTo(HttpStatus.NOT_FOUND);
    }



    @Test
    void upgradeCharacterSkill_success() {
        // given
        UpgradeCharacterSkillRequest request = new UpgradeCharacterSkillRequest();
        UUID skillId = UUID.randomUUID();

        UUID characterId = UUID.randomUUID();
        request.setSkillIds(new ArrayList<>());
        request.getSkillIds().add(skillId);

        var character = new CharacterEntity();
        character.setId(characterId);
        character.setOwnerId(UUID.randomUUID());
        character.setReputation(40); // Репутация больше минимально необходимой
        character.setBattlePoints(10); // Достаточно боевых очков
        character.setCivilPoints(10); // Достаточно гражданских очков

        int totalBattlePoints = 0;
        int totalCivilPoints = 0;

        var user = new User();
        user.setId(character.getOwnerId());

        var currentSkill = new Skill();
        currentSkill.setId(skillId);
        currentSkill.setSkillFamily("long_blade");
        currentSkill.setLevel(1); // Текущий уровень навыка
        currentSkill.setReputationRequirement(0); // Требуемая репутация меньше текущей
        currentSkill.setBattleCost(1);
        currentSkill.setCivilCost(0);

        var nextSkill = new Skill();
        nextSkill.setId(UUID.randomUUID());
        nextSkill.setSkillFamily("long_blade");
        nextSkill.setLevel(currentSkill.getLevel()+1); // Следующий уровень навыка
        nextSkill.setReputationRequirement(0); // Требуемая репутация меньше текущей
        nextSkill.setBattleCost(2);
        nextSkill.setCivilCost(0);


        character.setSkills(new ArrayList<>()); // Персонаж уже обладает текущим навыком
        character.getSkills().add(currentSkill);


        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(user);

        totalBattlePoints += character.getBattlePoints() - nextSkill.getBattleCost();
        totalCivilPoints +=  character.getCivilPoints() - nextSkill.getCivilCost();

        when(charRepo.findById(characterId)).thenReturn(Optional.of(character));
        when(skillRepo.findById(skillId)).thenReturn(Optional.of(currentSkill));
        when(skillRepo.findBySkillFamilyAndLevel("long_blade", 2)).thenReturn(Optional.of(nextSkill));

        // when
        service.upgradeCharacterSkill(request, characterId, auth);

        // then
        assertThat(character.getSkills()).contains(nextSkill);

        // Проверяем, что боевые и гражданские очки скорректированы
        assertThat(totalBattlePoints).isEqualTo(8);
        assertThat(totalCivilPoints).isEqualTo(10 );

        // Проверяем, что сущность персонажа сохранена
        verify(charRepo).save(character);
    }

    @Test
    void updatedCharacter_ownedByUser_success() {
        // given
        UUID charId = randomUUID();
        UpdateCharacterRequest request = new UpdateCharacterRequest();
        request.setName("test-name");
        request.setAge(42);
        request.setHeight(180);
        request.setWeight(60);
        request.setOrganization("raven");
        request.setCharacterClass("Соло");

        Authentication auth = mock(Authentication.class);
        User user = new User();
        user.setId(randomUUID());
        when(auth.getPrincipal()).thenReturn(user);

        CharacterEntity character = new CharacterEntity();
        character.setId(charId);
        character.setOwnerId(user.getId());
        character.setName("old-name");
        character.setAge(30);
        when(charRepo.findById(charId)).thenReturn(Optional.of(character));

        // when
        service.updateCharacter(request, charId);

        // then
        ArgumentCaptor<CharacterEntity> charCaptor = ArgumentCaptor.forClass(CharacterEntity.class);
        verify(charRepo).save(charCaptor.capture());
        CharacterEntity savedCharacter = charCaptor.getValue();

        assertThat(savedCharacter.getId()).isEqualTo(charId);
        assertThat(savedCharacter.getOwnerId()).isEqualTo(user.getId());
        assertThat(savedCharacter.getName()).isEqualTo(request.getName());
        assertThat(savedCharacter.getHeight()).isEqualTo(request.getHeight());
        assertThat(savedCharacter.getWeight()).isEqualTo(request.getWeight());
        assertThat(savedCharacter.getAge()).isEqualTo(request.getAge());
        assertThat(savedCharacter.getOrganization()).isEqualTo(request.getOrganization());
        assertThat(savedCharacter.getCharacterClass()).isEqualTo(request.getCharacterClass());
        assertThat(savedCharacter.getReputation()).isEqualTo(character.getReputation());

        verify(charRepo).findById(charId);
    }



    @Test
    void putCharacterWeapon_characterNotExists_throw404() {
        //given
        UUID characterId = randomUUID();
        UUID weaponId = randomUUID();
        Authentication auth = mock(Authentication.class);
        when(charRepo.findById(characterId)).thenReturn(Optional.empty());
        UpdateCharacterWeaponRequest request = new UpdateCharacterWeaponRequest();
        request.setWeaponIds(List.of(weaponId));

        //when
        Call call = () -> service.putCharacterWeapon(request, characterId, auth);

        //then
        assertThatThrownBy(call)
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("Персонаж не найден")
            .extracting(ResponseStatusException.class::cast)
            .extracting(ErrorResponseException::getStatusCode)
            .isEqualTo(HttpStatus.NOT_FOUND);
    }


    @Test
    void putCharacterWeapon_userNotOwner_throw403() {
        //given
        var user = new User();
        user.setId(UUID.randomUUID()); // Должен отличаться от ID владельца персонажа
        UUID characterId = randomUUID();
        UUID notUsersCharacter = randomUUID();
        Authentication auth = mock(Authentication.class);
        UpdateCharacterWeaponRequest request = new UpdateCharacterWeaponRequest();
        when(auth.getPrincipal()).thenReturn(user);

        when(charRepo.findById(characterId))
            .thenReturn(Optional.of(new CharacterEntity().setOwnerId(notUsersCharacter)));

        //when
        Call call = () -> service.putCharacterWeapon(request, characterId, auth);

        //then
        assertThatThrownBy(call)
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("Нельзя добавлять оружие не своему персонажу!")
            .extracting(ResponseStatusException.class::cast)
            .extracting(ErrorResponseException::getStatusCode)
            .isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void deleteWeapon_weaponExists_success() {
        // given
        UUID weaponId = UUID.randomUUID();
        UUID characterId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Authentication auth = mock(Authentication.class);
        User user = new User();
        user.setId(userId);
        when(auth.getPrincipal()).thenReturn(user);

        // Инициализируем список оружий
        CharacterEntity character = new CharacterEntity();
        character.setOwnerId(userId);
        character.setWeapons(new ArrayList<>()); // Инициализация списка, чтобы избежать NullPointerException

        Weapon weapon = new Weapon();
        weapon.setId(weaponId);
        character.getWeapons().add(weapon);

        when(charRepo.findById(characterId)).thenReturn(Optional.of(character));
        when(weaponRepo.findById(weaponId)).thenReturn(Optional.of(weapon));

        // when
        service.deleteCharacterWeapon(weaponId, characterId, auth);

        // then
        assertThat(character.getWeapons()).doesNotContain(weapon);
        verify(charRepo).save(character);
    }

    @Test
    void deleteWeaponCharacter_characterNotExists_throw404() {
        // given
        UUID weaponId = UUID.randomUUID();
        UUID characterId = UUID.randomUUID();
        Authentication auth = mock(Authentication.class);
        when(charRepo.findById(characterId)).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> service.deleteCharacterWeapon(weaponId, characterId, auth))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("Персонаж не найден")
            .extracting(ResponseStatusException.class::cast)
            .extracting(ErrorResponseException::getStatusCode)
            .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void deleteWeapon_unauthorized_throw403() {
        // given
        UUID weaponId = UUID.randomUUID();
        UUID characterId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Authentication auth = mock(Authentication.class);

        User user = new User();
        user.setId(userId);
        when(auth.getPrincipal()).thenReturn(user);

        CharacterEntity character = new CharacterEntity();
        character.setOwnerId(UUID.randomUUID()); // другой владелец
        when(charRepo.findById(characterId)).thenReturn(Optional.of(character));

        // when / then
        assertThatThrownBy(() -> service.deleteCharacterWeapon(weaponId, characterId, auth))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("Нельзя удалять оружие не своему персонажу!")
            .extracting(ResponseStatusException.class::cast)
            .extracting(ErrorResponseException::getStatusCode)
            .isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void deleteWeapon_weaponNotExists_throw404() {
        // given
        UUID weaponId = UUID.randomUUID();
        UUID characterId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Authentication auth = mock(Authentication.class);
        User user = new User();
        user.setId(userId);
        when(auth.getPrincipal()).thenReturn(user);

        CharacterEntity character = new CharacterEntity();
        character.setId(characterId); // Добавлено для соответствия
        character.setOwnerId(userId);
        character.setWeapons(new ArrayList<>());

        when(charRepo.findById(characterId)).thenReturn(Optional.of(character));
        when(weaponRepo.findById(weaponId)).thenReturn(Optional.empty()); // Ожидаем, что оружие не найдено

        // when & then
        assertThatThrownBy(() -> service.deleteCharacterWeapon(weaponId, characterId, auth))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("Оружие не найдено")
            .extracting(ResponseStatusException.class::cast)
            .extracting(ErrorResponseException::getStatusCode)
            .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void deleteWeapon_characterDontHaveWeapon_throw400() {
        // given
        UUID weaponId = UUID.randomUUID();
        UUID characterId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Authentication auth = mock(Authentication.class);

        User user = new User();
        user.setId(userId);
        when(auth.getPrincipal()).thenReturn(user);

        CharacterEntity character = new CharacterEntity();
        character.setOwnerId(userId);
        when(charRepo.findById(characterId)).thenReturn(Optional.of(character));
        when(weaponRepo.findById(weaponId)).thenReturn(Optional.of(new Weapon())); // оружие не в инвентаре

        // when / then
        assertThatThrownBy(() -> service.deleteCharacterWeapon(weaponId, characterId, auth))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("Этого оружия нет в списке вашего персонажа")
            .extracting(ResponseStatusException.class::cast)
            .extracting(ErrorResponseException::getStatusCode)
            .isEqualTo(HttpStatus.BAD_REQUEST);
    }


    @Test
    public void giveReputation_characterExists_success() {
        // given
        UUID characterId = UUID.randomUUID();
        CharacterEntity character = new CharacterEntity();
        character.setId(characterId);
        character.setReputation(50);

        GiveReputationRequest request = new GiveReputationRequest();
        request.setReputation(10);

        Authentication auth = mock();
        User user = new User();
        user.setId(randomUUID());
        when(auth.getPrincipal())
            .thenReturn(user);

        when(charRepo.findById(characterId)).thenReturn(Optional.of(character));
        when(auth.getPrincipal()).thenReturn(user);

        // when
        service.giveReputation(request, characterId, auth);

        // then
        assertEquals(60, character.getReputation());
        verify(charRepo).save(character);
    }

    @Test
    public void getCharacterImplants_characterExists_success() {
        // given
        UUID characterId = UUID.randomUUID();

        CharacterEntity character = new CharacterEntity();
        character.setId(characterId);
        Implant implant = new Implant();
        List<Implant> implants = List.of(implant);
        character.setImplants(implants);

        when(charRepo.findById(characterId)).thenReturn(Optional.of(character));

        // when
        List<ImplantDto> implantDtos = service.getCharacterImplants(characterId);

        // then
        assertEquals(1, implantDtos.size());
        verify(charRepo, times(1)).findById(characterId);
    }

    @Test
    public void getCharactersImplants_characterNotExists_throw404() {
        // given
        UUID characterId = UUID.randomUUID();

        when(charRepo.findById(characterId)).thenReturn(Optional.empty());

        // when & then
        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> service.getCharacterImplants(characterId)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Персонаж " + characterId + " не найден", exception.getReason());
        verify(charRepo, times(1)).findById(characterId);
    }

    @Test
    public void getCharactersImplants_implantNotExists_success() {
        // given
        UUID characterId = UUID.randomUUID();

        CharacterEntity character = new CharacterEntity();
        character.setId(characterId);
        character.setImplants(Collections.emptyList()); // Нет имплантов

        when(charRepo.findById(characterId)).thenReturn(Optional.of(character));

        // when
        List<ImplantDto> implantDtos = service.getCharacterImplants(characterId);

        // then
        assertTrue(implantDtos.isEmpty());
        verify(charRepo, times(1)).findById(characterId);
    }

    @Test
    public void putCharacterImplantReputation_reputationInsufficient_throw400() {
        // given
        UUID characterId = UUID.randomUUID();
        User user = new User();
        user.setId(UUID.randomUUID());
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(user);

        CharacterEntity character = new CharacterEntity();
        character.setId(characterId);
        character.setOwnerId(user.getId());
        character.setReputation(0); // Низкая репутация <----

        Implant implant = new Implant();
        implant.setId(UUID.randomUUID());
        implant.setReputationRequirement(1);
        implant.setImplantPointsCost(3);
        implant.setSpecialImplantPointsCost(0);

        UpdateCharacterImplantsRequest request = new UpdateCharacterImplantsRequest();
        request.setImplantIds(List.of(implant.getId()));


        when(charRepo.findById(characterId)).thenReturn(Optional.of(character));
        when(implantRepo.findById(implant.getId())).thenReturn(Optional.of(implant));

        // when & then
        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> service.updateCharacterImplants(request, characterId, auth)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Недостаточная репутация для импланта с ID " + implant.getId(), exception.getReason());
    }

    @Test
    public void putCharacterImplant_pointsInsufficient_throw400() {
        // given
        UUID characterId = UUID.randomUUID();
        User user = new User();
        user.setId(UUID.randomUUID());
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(user);

        CharacterEntity character = new CharacterEntity();
        character.setId(characterId);
        character.setOwnerId(user.getId());
        character.setReputation(1);

        Implant implant = new Implant();
        implant.setId(UUID.randomUUID());
        implant.setReputationRequirement(1);
        implant.setImplantPointsCost(1000);

        UpdateCharacterImplantsRequest request = new UpdateCharacterImplantsRequest();
        request.setImplantIds(List.of(implant.getId()));

        when(charRepo.findById(characterId)).thenReturn(Optional.of(character));
        when(implantRepo.findById(implant.getId())).thenReturn(Optional.of(implant));

        // when & then
        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> service.updateCharacterImplants(request, characterId, auth)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Недостаточно ОИ для обычных имплантов", exception.getReason());
    }

    @Test
    public void deleteCharacterImplant_implantExists_success() {
        // given
        UUID characterId = UUID.randomUUID();
        UUID implantId = UUID.randomUUID();
        User user = new User();
        user.setId(UUID.randomUUID());
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(user);

        CharacterEntity character = new CharacterEntity();
        character.setId(characterId);
        character.setOwnerId(user.getId());
        character.setImplants(new ArrayList<>());

        Implant implant = new Implant();
        implant.setId(implantId);
        implant.setImplantPointsCost(5);
        implant.setSpecialImplantPointsCost(5);

        character.getImplants().add(implant);

        when(charRepo.findById(characterId)).thenReturn(Optional.of(character));
        when(implantRepo.findById(implantId)).thenReturn(Optional.of(implant));

        // when
        service.deleteCharacterImplant(characterId, implantId, auth);

        // then
        assertFalse(character.getImplants().contains(implant));
        verify(charRepo, times(1)).save(character);
    }

    @Test
    public void deleteCharacterImplant_characterNotExists_throw404() {
        // given
        UUID characterId = UUID.randomUUID();
        UUID implantId = UUID.randomUUID();
        User user = new User();
        user.setId(UUID.randomUUID());
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(user);

        when(charRepo.findById(characterId)).thenReturn(Optional.empty());

        // when & then
        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> service.deleteCharacterImplant(implantId, characterId, auth)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Персонаж не найден", exception.getReason());
    }

    @Test
    public void deleteCharacterImplant_characterNotOwned_throw403() {
        // given
        UUID characterId = UUID.randomUUID();
        UUID implantId = UUID.randomUUID();
        User user = new User();
        user.setId(UUID.randomUUID()); // Другой пользователь
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(user);
        CharacterEntity character = new CharacterEntity();
        character.setId(characterId);
        character.setOwnerId(UUID.randomUUID()); // Не тот пользователь

        when(charRepo.findById(characterId)).thenReturn(Optional.of(character));

        // when & then
        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> service.deleteCharacterImplant(characterId, implantId, auth)
        );

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        assertEquals("Нельзя добавлять имплант не своему персонажу!", exception.getReason());
    }

    @Test
    public void deleteCharacterImplant_implantNotExists_throw404() {
        // given
        UUID characterId = UUID.randomUUID();
        UUID implantId = UUID.randomUUID();
        User user = new User();
        user.setId(UUID.randomUUID());
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(user);

        CharacterEntity character = new CharacterEntity();
        character.setId(characterId);
        character.setOwnerId(user.getId());
        character.setImplants(new ArrayList<>());

        when(charRepo.findById(characterId)).thenReturn(Optional.of(character));
        when(implantRepo.findById(implantId)).thenReturn(Optional.empty());

        // when & then
        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> service.deleteCharacterImplant(characterId, implantId, auth)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Имплант не найден", exception.getReason());
    }

    @Test
    public void deleteCharacterImplant_implantNotInList_throw400() {
        // given
        UUID characterId = UUID.randomUUID();
        UUID implantId = UUID.randomUUID();
        User user = new User();
        user.setId(UUID.randomUUID());
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(user);

        CharacterEntity character = new CharacterEntity();
        character.setId(characterId);
        character.setOwnerId(user.getId());
        character.setImplants(new ArrayList<>());

        Implant implant = new Implant();
        implant.setId(UUID.randomUUID());

        character.getImplants().add(implant);

        when(charRepo.findById(characterId)).thenReturn(Optional.of(character));
        when(implantRepo.findById(implantId)).thenReturn(Optional.of(new Implant())); // Это возвращает несуществующий имплант

        // when & then
        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> service.deleteCharacterImplant(characterId, implantId, auth)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Этого импланта нет в вашем списке.", exception.getReason());
    }
}
