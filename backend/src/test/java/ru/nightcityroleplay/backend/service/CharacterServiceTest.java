package ru.nightcityroleplay.backend.service;

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
import ru.nightcityroleplay.backend.entity.User;
import ru.nightcityroleplay.backend.entity.Weapon;
import ru.nightcityroleplay.backend.repo.CharacterRepository;
import ru.nightcityroleplay.backend.repo.SkillRepository;
import ru.nightcityroleplay.backend.repo.WeaponRepository;
import ru.nightcityroleplay.backend.util.Call;

import java.util.*;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

class CharacterServiceTest {

    CharacterService service;
    CharacterRepository repo;

    CharacterStatsService characterStatsService;


    CharacterRepository charRepo;
    WeaponRepository weaponRepo;
    SkillRepository skillRepo;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        charRepo = mock();
        skillRepo = mock();
        characterStatsService = mock();
        service = new CharacterService(charRepo, characterStatsService, skillRepo);
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
    public void deleteCharacterSuccess() {
        // given
        UUID characterId = UUID.randomUUID();
        Authentication auth = mock(Authentication.class);
        User user = new User();
        UUID userId = UUID.randomUUID();
        user.setId(userId);
        when(auth.getPrincipal()).thenReturn(user);

        CharacterEntity character = new CharacterEntity();
        character.setId(characterId);
        character.setOwnerId(userId);

        when(charRepo.findById(characterId)).thenReturn(java.util.Optional.of(character));

        // when
        service.deleteCharacter(characterId, auth);

        // then
        verify(charRepo, times(1)).deleteById(characterId);
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
            .hasMessageContaining("Персонаж не найден");
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
            .hasMessageContaining("Нельзя добавлять оружие не своему персонажу!");


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

        // Then
        assertThatThrownBy(call)
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("Оружие не найдено");
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
            .hasMessageContaining("Персонаж не найден");
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
            .hasMessageContaining("Оружие не найдено"); // Исправлено на правильное сообщение
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
            .hasMessageContaining("Нельзя удалять оружие не своему персонажу!");
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
            .hasMessageContaining("Этого оружия нет в списке вашего персонада");

    }
}
