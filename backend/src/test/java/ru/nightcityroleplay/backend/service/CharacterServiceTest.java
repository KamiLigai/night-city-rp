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
import ru.nightcityroleplay.backend.entity.CharacterEntity;
import ru.nightcityroleplay.backend.entity.Implant;
import ru.nightcityroleplay.backend.entity.User;
import ru.nightcityroleplay.backend.entity.Weapon;
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
    WeaponRepository weaponRepo;
    CharacterRepository charRepo;
    SkillRepository skillRepo;
    private Pageable pageable;
    ImplantRepository implantRepo;

    @BeforeEach
    void setUp() {
        characterStatsService = mock();
        weaponRepo = mock();
        pageable = mock();
        charRepo = mock();
        skillRepo = mock();
        implantRepo = mock();

        service = new CharacterService(charRepo, characterStatsService, weaponRepo, skillRepo, implantRepo, characterStatsService);
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
        character1.setWeapons(new ArrayList<>()); // инициализация пустого списка
        character1.setReputation(5);

        CharacterEntity character2 = new CharacterEntity();
        character2.setId(UUID.randomUUID());
        character2.setName("Character 2");
        character2.setWeapons(new ArrayList<>());
        character2.setReputation(5);// инициализация пустого списка

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
        request.setReputation(0);
        request.setName(randomUUID().toString());

        Authentication auth = mock(Authentication.class);

        when(charRepo.findById(characterId)).thenReturn(Optional.empty());

        // then
        assertThatThrownBy(() -> service.updateCharacter(request, characterId, auth))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("Персонаж " + characterId + " не найден")
            .extracting(ResponseStatusException.class::cast)
            .extracting(ErrorResponseException::getStatusCode)
            .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void updateCharacterForbidden() {
        // given
        var request = new UpdateCharacterRequest();
        UUID characterId = UUID.randomUUID();

        request.setAge(1);
        request.setReputation(0);
        request.setName(randomUUID().toString());

        var oldCharacter = new CharacterEntity();
        oldCharacter.setId(characterId);
        oldCharacter.setOwnerId(UUID.randomUUID()); // Должен отличаться от ID пользователя

        var user = new User();
        user.setId(UUID.randomUUID()); // Должен отличаться от ID владельца персонажа

        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(user);

        when(charRepo.findById(characterId)).thenReturn(Optional.of(oldCharacter));

        // then
        assertThatThrownBy(() -> service.updateCharacter(request, characterId, auth))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining(("Изменить чужого персонажа вздумал? а ты хорош."))
            .extracting(ResponseStatusException.class::cast)
            .extracting(ErrorResponseException::getStatusCode)
            .isEqualTo(HttpStatus.FORBIDDEN);

    }

    @Test
    void updateCharacterSkill_characterNotExists_throw404() {
        // given
        var request = new UpdateCharacterSkillRequest();
        UUID characterId = UUID.randomUUID();

        Authentication auth = mock(Authentication.class);

        when(charRepo.findById(characterId)).thenReturn(Optional.empty());

        // then
        assertThatThrownBy(() -> service.updateCharacterSkill(request, characterId, auth))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("Персонаж " + characterId + " не найден")
            .extracting(ResponseStatusException.class::cast)
            .extracting(ErrorResponseException::getStatusCode)
            .isEqualTo(HttpStatus.NOT_FOUND);

    }

    @Test
    void updateCharacterSkill_unauthorized_throw403() {
        // given
        var request = new UpdateCharacterSkillRequest();
        UUID characterId = UUID.randomUUID();

        var oldCharacter = new CharacterEntity();
        oldCharacter.setId(characterId);
        oldCharacter.setOwnerId(UUID.randomUUID());

        var user = new User();
        user.setId(UUID.randomUUID());

        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(user);

        when(charRepo.findById(characterId)).thenReturn(Optional.of(oldCharacter));

        // then
        assertThatThrownBy(() -> service.updateCharacterSkill(request, characterId, auth))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("Нельзя добавлять навык не своему персонажу!")
            .extracting(ResponseStatusException.class::cast)
            .extracting(ErrorResponseException::getStatusCode)
            .isEqualTo(HttpStatus.FORBIDDEN);
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
    void updatedCharacter_ownedByUser_success() {
        // given
        UpdateCharacterRequest request = new UpdateCharacterRequest();
        request.setAge(42);
        request.setReputation(0);
        request.setName("test-name");

        UUID charId = randomUUID();

        Authentication auth = mock();
        User user = new User();
        user.setId(randomUUID());
        when(auth.getPrincipal())
            .thenReturn(user);

        CharacterEntity character = new CharacterEntity();
        character.setOwnerId(user.getId());
        when(charRepo.findById(charId))
            .thenReturn(Optional.of(character));

        // when
        service.updateCharacter(request, charId, auth);

        // then
        ArgumentCaptor<CharacterEntity> charCaptor = ArgumentCaptor.captor();
        verify(charRepo).save(charCaptor.capture());
        CharacterEntity savedChar = charCaptor.getValue();
        assertThat(savedChar.getId()).isEqualTo(charId);
        assertThat(savedChar.getOwnerId()).isEqualTo(user.getId());
        assertThat(savedChar.getName()).isEqualTo("test-name");
        assertThat(savedChar.getAge()).isEqualTo(42);
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
