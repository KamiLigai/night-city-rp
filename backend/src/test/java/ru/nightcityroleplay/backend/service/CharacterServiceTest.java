package ru.nightcityroleplay.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
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

import java.nio.file.AccessDeniedException;
import java.util.*;
import java.util.*;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CharacterServiceTest {

    CharacterService service;
    CharacterRepository repo;

    CharacterStatsService characterStatsService;

    WeaponRepository weaponRepo;
    CharacterRepository charRepo;
    SkillRepository skillRepo;
    private Pageable pageable;
    ImplantRepository implantRepo;

    Authentication auth;

    @BeforeEach
    void setUp() {
        weaponRepo = mock();
        pageable = mock();
        charRepo = mock();
        skillRepo = mock();
        characterStatsService = mock();
        implantRepo = mock();
        service = new CharacterService(weaponRepo, charRepo, characterStatsService, skillRepo, implantRepo);
    }

    @Test
    void getCharacterWhenCharacterIsAbsent() {
        // given
        UUID id = randomUUID();
        when(charRepo.findById(id))
            .thenReturn(Optional.empty());

        // when
        var result = service.getCharacter(id);

        // then
        assertThat(result).isNull();
        verify(charRepo).findById(id);
    }

    @Test
    void createCharacterIsSave() {
        // given
        var request = new CreateCharacterRequest();
        request.setName("Илон");
        request.setAge(8);

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
        verifyNoMoreInteractions(charRepo);
    }


    @Test
    void toDtoIsNotNull() {
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
    void getCharacterPage_Success() {
        // given
        CharacterEntity character1 = new CharacterEntity();
        character1.setId(UUID.randomUUID());
        character1.setName("Character 1");
        character1.setWeapons(new ArrayList<>()); // инициализация пустого списка

        CharacterEntity character2 = new CharacterEntity();
        character2.setId(UUID.randomUUID());
        character2.setName("Character 2");
        character2.setWeapons(new ArrayList<>()); // инициализация пустого списка

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
    void getCharacterPage_Empty() {
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
    void updateCharacterNotFound() {
        // given
        var request = new UpdateCharacterRequest();
        UUID characterId = UUID.randomUUID();

        Authentication auth = mock(Authentication.class);

        when(charRepo.findById(characterId)).thenReturn(Optional.empty());

        // then
        assertThatThrownBy(() -> service.updateCharacter(request, characterId, auth))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("Персонаж " + characterId + " не найден");
    }

    @Test
    void updateCharacterUnauthorized() {
        // given
        var request = new UpdateCharacterRequest();
        UUID characterId = UUID.randomUUID();

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
            .hasMessageContaining("Изменить чужого персонажа вздумал? а ты хорош."); // Проверяем сообщение
    }

    @Test
    void updateCharacterSkillNotFound() {
        // given
        var request = new UpdateCharacterSkillRequest();
        UUID characterId = UUID.randomUUID();

        Authentication auth = mock(Authentication.class);

        when(charRepo.findById(characterId)).thenReturn(Optional.empty());

        // then
        assertThatThrownBy(() -> service.updateCharacterSkill(request, characterId, auth))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("Персонаж " + characterId + " не найден");
    }

    @Test
    void updateCharacterSkillUnauthorized() {
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
            .hasMessageContaining("Нельзя добавлять навык не своему персонажу!");
    }

    @Test
    public void deleteCharacterNotFound() {
        // given
        UUID characterId = UUID.randomUUID();
        Authentication authentication = mock(Authentication.class);

        // when
        when(charRepo.findById(characterId)).thenReturn(java.util.Optional.empty());

        // then
        assertThatThrownBy(() -> service.deleteCharacter(characterId, authentication))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("Персонаж " + characterId + " не найден");
    }

    @Test
    public void deleteCharacterUnauthorized() {
        // given
        UUID characterId = UUID.randomUUID();
        Authentication authentication = mock(Authentication.class);
        User user = new User();
        UUID userId = UUID.randomUUID();
        user.setId(userId);
        when(authentication.getPrincipal()).thenReturn(user);

        CharacterEntity character = new CharacterEntity();
        character.setId(characterId);
        character.setOwnerId(UUID.randomUUID());

        when(charRepo.findById(characterId)).thenReturn(java.util.Optional.of(character));

        // when
        Call call = () -> service.deleteCharacter(characterId, authentication);

        // then
        assertThatThrownBy(call)
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("Удалить чужого персонажа вздумал? а ты хорош.")
            .extracting(ResponseStatusException.class::cast)
            .extracting(ErrorResponseException::getStatusCode)
            .isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void updatedCharacterOwnedByUser() {
        // given
        UpdateCharacterRequest request = new UpdateCharacterRequest();
        request.setAge(42);
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
    void putCharacterWeapon_CharacterNotFound() {
        //given
        UUID characterId = randomUUID();
        UUID wheaponId = randomUUID();
        Authentication auth = mock(Authentication.class);
        when(charRepo.findById(characterId)).thenReturn(Optional.empty());
        UpdateCharacterWeaponRequest request = new UpdateCharacterWeaponRequest();
        request.setWeaponId(wheaponId);

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
    void putCharacterWeapon_UserNotOwner() {
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
    void putCharacterWeapon_WeaponNotExist() {
        // given
        UUID characterId = randomUUID();
        UUID weaponId = randomUUID();
        UUID userId = randomUUID();
        Authentication auth = mock(Authentication.class);

        User user = new User();
        user.setId(userId);
        when(auth.getPrincipal()).thenReturn(user);

        CharacterEntity character = new CharacterEntity();
        character.setId(characterId);
        character.setOwnerId(userId);

        UpdateCharacterWeaponRequest request = new UpdateCharacterWeaponRequest();
        request.setWeaponId(weaponId);

        when(charRepo.findById(characterId)).thenReturn(Optional.of(character));
        when(weaponRepo.findById(weaponId)).thenReturn(Optional.empty());

        // When
        Call call = () -> service.putCharacterWeapon(request, characterId, auth);

        // then
        assertThatThrownBy(call)
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("Оружие не найдено")
            .extracting(ResponseStatusException.class::cast)
            .extracting(ErrorResponseException::getStatusCode)
            .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void deleteWeaponSuccessful() {
        // Given
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

        // When
        service.deleteCharacterWeapon(weaponId, characterId, auth);

        // Then
        assertThat(character.getWeapons()).doesNotContain(weapon);
        verify(charRepo).save(character);
    }

    @Test
    void deleteWeaponCharacterNotFound() {
        // Given
        UUID weaponId = UUID.randomUUID();
        UUID characterId = UUID.randomUUID();
        Authentication auth = mock(Authentication.class);
        when(charRepo.findById(characterId)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.deleteCharacterWeapon(weaponId, characterId, auth))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("Персонаж не найден")
            .extracting(ResponseStatusException.class::cast)
            .extracting(ErrorResponseException::getStatusCode)
            .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void deleteWeaponUnauthorizedAccess() {
        // Given
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

        // When / Then
        assertThatThrownBy(() -> service.deleteCharacterWeapon(weaponId, characterId, auth))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("Нельзя удалять оружие не своему персонажу!")
            .extracting(ResponseStatusException.class::cast)
            .extracting(ErrorResponseException::getStatusCode)
            .isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void deleteWeaponNotFound() {
        // Given
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

        // When & Then
        assertThatThrownBy(() -> service.deleteCharacterWeapon(weaponId, characterId, auth))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("Оружие не найдено")
            .extracting(ResponseStatusException.class::cast)
            .extracting(ErrorResponseException::getStatusCode)
            .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void deleteWeaponNotFoundInCharacter() {
        // Given
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

        // When / Then
        assertThatThrownBy(() -> service.deleteCharacterWeapon(weaponId, characterId, auth))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("Этого оружия нет в списке вашего персонажа")
            .extracting(ResponseStatusException.class::cast)
            .extracting(ErrorResponseException::getStatusCode)
            .isEqualTo(HttpStatus.BAD_REQUEST);
    }
}


    @Test
    public void giveReputationSuccess() {
        // Given
        UUID characterId = UUID.randomUUID();
        CharacterEntity character = new CharacterEntity();
        character.setId(characterId);
        character.setReputation(50);

        GiveRewardRequest request = new GiveRewardRequest();
        request.setReputation(10);

        Authentication auth = mock();
        User user = new User();
        user.setId(randomUUID());
        when(auth.getPrincipal())
            .thenReturn(user);

        when(charRepo.findById(characterId)).thenReturn(Optional.of(character));
        when(auth.getPrincipal()).thenReturn(user);

        // When
        service.giveReputation(request, characterId, auth);

        // Then
        assertEquals(60, character.getReputation());
        verify(charRepo).save(character);
    }

    @Test
    public void getCharactersImplantsSuccess() {
        // Given
        UUID characterId = UUID.randomUUID();
        Authentication auth = mock(Authentication.class);

        CharacterEntity character = new CharacterEntity();
        character.setId(characterId);
        Implant implant = new Implant(); // Создайте экземпляр импланта
        List<Implant> implants = List.of(implant); // Добавьте имплант в список
        character.setImplants(implants);

        when(charRepo.findById(characterId)).thenReturn(Optional.of(character));

        // When
        List<ImplantDto> implantDtos = service.getCharactersImplants(characterId, auth);

        // Then
        assertEquals(1, implantDtos.size());
        verify(charRepo, times(1)).findById(characterId);
    }

    @Test
    public void getCharactersImplantsCharacterNotFound() {
        // Given
        UUID characterId = UUID.randomUUID();
        Authentication auth = mock(Authentication.class);

        when(charRepo.findById(characterId)).thenReturn(Optional.empty());

        // When & Then
        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> service.getCharactersImplants(characterId, auth)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Персонаж " + characterId + " не найден", exception.getReason());
        verify(charRepo, times(1)).findById(characterId);
    }

    @Test
    public void getCharactersImplantsNoImplants() {
        // Given
        UUID characterId = UUID.randomUUID();
        Authentication auth = mock(Authentication.class);

        CharacterEntity character = new CharacterEntity();
        character.setId(characterId);
        character.setImplants(Collections.emptyList()); // Нет имплантов

        when(charRepo.findById(characterId)).thenReturn(Optional.of(character));

        // When
        List<ImplantDto> implantDtos = service.getCharactersImplants(characterId, auth);

        // Then
        assertTrue(implantDtos.isEmpty());
        verify(charRepo, times(1)).findById(characterId);
    }

    @Test
    public void putCharacterImplantReputationInsufficient() {
        // Given
        UUID characterId = UUID.randomUUID();
        User user = new User();
        user.setId(UUID.randomUUID());
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(user);

        CharacterEntity character = new CharacterEntity();
        character.setId(characterId);
        character.setOwnerId(user.getId());
        character.setImplantPoints(10);
        character.setSpecialImplantPoints(10);
        character.setReputation(0); // Низкая репутация <----

        Implant implant = new Implant();
        implant.setId(UUID.randomUUID());
        implant.setReputationRequirement(1);
        implant.setImplantPointsCost(3);
        implant.setSpecialImplantPointsCost(0);

        UpdateCharacterImplantRequest request = new UpdateCharacterImplantRequest();
        request.setImplantId(List.of(implant.getId()));

        when(charRepo.findById(characterId)).thenReturn(Optional.of(character));
        when(implantRepo.findById(implant.getId())).thenReturn(Optional.of(implant));

        // When & Then
        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> service.putCharacterImplant(request, characterId, auth)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Данный имплант не доступен на вашей репутации", exception.getReason());
    }

    @Test
    public void putCharacterImplantInsufficientPoints() {
        // Given
        UUID characterId = UUID.randomUUID();
        User user = new User();
        user.setId(UUID.randomUUID());
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(user);

        CharacterEntity character = new CharacterEntity();
        character.setId(characterId);
        character.setOwnerId(user.getId());
        character.setImplantPoints(4); // Недостаточно очков
        character.setSpecialImplantPoints(10);
        character.setReputation(5);

        Implant implant = new Implant();
        implant.setId(UUID.randomUUID());
        implant.setReputationRequirement(1);
        implant.setImplantPointsCost(5);
        implant.setSpecialImplantPointsCost(0); // 0 для специальных очков

        UpdateCharacterImplantRequest request = new UpdateCharacterImplantRequest();
        request.setImplantId(List.of(implant.getId()));

        when(charRepo.findById(characterId)).thenReturn(Optional.of(character));
        when(implantRepo.findById(implant.getId())).thenReturn(Optional.of(implant));

        // When & Then
        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> service.putCharacterImplant(request, characterId, auth)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Недостаточно ОИ для обычных имплантов", exception.getReason());
    }

    @Test
    public void deleteCharacterImplantSuccess() {
        // Given
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

        // Adding the implant to the character
        character.getImplants().add(implant);
        character.setImplantPoints(10);
        character.setSpecialImplantPoints(10);

        when(charRepo.findById(characterId)).thenReturn(Optional.of(character));
        when(implantRepo.findById(implantId)).thenReturn(Optional.of(implant));

        // When
        service.deleteCharacterImplant(implantId, characterId, auth);

        // Then
        assertFalse(character.getImplants().contains(implant));
        assertEquals(15, character.getImplantPoints());
        assertEquals(15, character.getSpecialImplantPoints());
        verify(charRepo, times(1)).save(character);
    }

    @Test
    public void deleteCharacterImplantCharacterNotFound() {
        // Given
        UUID characterId = UUID.randomUUID();
        UUID implantId = UUID.randomUUID();
        User user = new User();
        user.setId(UUID.randomUUID());
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(user);

        when(charRepo.findById(characterId)).thenReturn(Optional.empty());

        // When & Then
        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> service.deleteCharacterImplant(implantId, characterId, auth)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Персонаж не найден", exception.getReason());
    }

    @Test
    public void deleteCharacterImplantForbidden() {
        // Given
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

        // When & Then
        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> service.deleteCharacterImplant(implantId, characterId, auth)
        );

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        assertEquals("Нельзя добавлять имплант не своему персонажу!", exception.getReason());
    }

    @Test
    public void deleteCharacterImplantImplantNotFound() {
        // Given
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

        // When & Then
        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> service.deleteCharacterImplant(implantId, characterId, auth)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Имлант не найден", exception.getReason());
    }

    @Test
    public void deleteCharacterImplantNotInList() {
        // Given
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
        implant.setId(UUID.randomUUID()); // Другой имплант

        // Adding a different implant
        character.getImplants().add(implant);
        character.setImplantPoints(10);
        character.setSpecialImplantPoints(10);

        when(charRepo.findById(characterId)).thenReturn(Optional.of(character));
        when(implantRepo.findById(implantId)).thenReturn(Optional.of(new Implant())); // Это возвращает несуществующий имплант

        // When & Then
        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> service.deleteCharacterImplant(implantId, characterId, auth)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Этого импланта нет в вашем списке.", exception.getReason());
    }
}
