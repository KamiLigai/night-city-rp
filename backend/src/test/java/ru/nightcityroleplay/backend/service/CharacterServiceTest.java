package ru.nightcityroleplay.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.server.ResponseStatusException;
import ru.nightcityroleplay.backend.dto.CreateCharacterRequest;
import ru.nightcityroleplay.backend.dto.UpdateCharacterRequest;
import ru.nightcityroleplay.backend.dto.UpdateCharacterSkillRequest;
import ru.nightcityroleplay.backend.dto.UpdateCharacterWeaponRequest;
import ru.nightcityroleplay.backend.entity.CharacterEntity;
import ru.nightcityroleplay.backend.entity.User;
import ru.nightcityroleplay.backend.entity.Weapon;
import ru.nightcityroleplay.backend.repo.CharacterRepository;
import ru.nightcityroleplay.backend.repo.SkillRepository;
import ru.nightcityroleplay.backend.repo.WeaponRepository;
import ru.nightcityroleplay.backend.util.Call;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

class CharacterServiceTest {

    CharacterService service;
    WeaponRepository weaponRepo;
    CharacterRepository characterRepo;
    SkillRepository skillRepo;

    @BeforeEach
    void setUp() {

        characterRepo = mock();
        weaponRepo = mock();
        service = new CharacterService(characterRepo, weaponRepo, skillRepo);
    }


    @Test
    void getCharacterWhenCharacterIsAbsent() {
        // given
        UUID id = randomUUID();
        when(characterRepo.findById(id))
            .thenReturn(Optional.empty());

        // when
        var result = service.getCharacter(id);

        // then
        assertThat(result).isNull();
        verify(characterRepo).findById(id);
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
        when(characterRepo.save(any()))
            .thenReturn(character);

        // when
        service.createCharacter(request, auth);

        // then
        verify(characterRepo).save(any());
        verifyNoMoreInteractions(characterRepo);
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

        when(characterRepo.findById(charId))
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
    void updateCharacterNotFound() {
        // given
        var request = new UpdateCharacterRequest();
        UUID characterId = UUID.randomUUID();

        Authentication auth = mock(Authentication.class);

        when(characterRepo.findById(characterId)).thenReturn(Optional.empty());

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

        when(characterRepo.findById(characterId)).thenReturn(Optional.of(oldCharacter));

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

        when(characterRepo.findById(characterId)).thenReturn(Optional.empty());

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

        when(characterRepo.findById(characterId)).thenReturn(Optional.of(oldCharacter));

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

        when(characterRepo.findById(characterId)).thenReturn(java.util.Optional.of(character));

        // when
        service.deleteCharacter(characterId, auth);

        // then
        verify(characterRepo, times(1)).deleteById(characterId);
    }

    @Test
    public void deleteCharacterNotFound() {
        // given
        UUID characterId = UUID.randomUUID();
        Authentication authentication = mock(Authentication.class);

        // when
        when(characterRepo.findById(characterId)).thenReturn(java.util.Optional.empty());

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

        when(characterRepo.findById(characterId)).thenReturn(java.util.Optional.of(character));

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
        when(characterRepo.findById(charId))
            .thenReturn(Optional.of(character));

        // when
        service.updateCharacter(request, charId, auth);

        // then
        ArgumentCaptor<CharacterEntity> charCaptor = ArgumentCaptor.captor();
        verify(characterRepo).save(charCaptor.capture());
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
        when(characterRepo.findById(characterId)).thenReturn(Optional.empty());
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

        when(characterRepo.findById(characterId))
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
        //given
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
        when(characterRepo.findById(characterId)).thenReturn(Optional.of(character));
        when(weaponRepo.findById(weaponId)).thenReturn(Optional.empty());
        //when
        Call call = () -> service.putCharacterWeapon(request, characterId, auth);
        //then
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

        when(characterRepo.findById(characterId)).thenReturn(Optional.of(character));
        when(weaponRepo.findById(weaponId)).thenReturn(Optional.of(weapon));

        // When
        service.deleteCharacterWeapon(weaponId, characterId, auth);

        // Then
        assertThat(character.getWeapons()).doesNotContain(weapon);
        verify(characterRepo).save(character);
    }

    @Test
    void deleteWeaponCharacterNotFound() {
        // Given
        UUID weaponId = UUID.randomUUID();
        UUID characterId = UUID.randomUUID();
        Authentication auth = mock(Authentication.class);
        when(characterRepo.findById(characterId)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.deleteCharacterWeapon(weaponId, characterId, auth))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("Персонаж не найден");
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
        when(characterRepo.findById(characterId)).thenReturn(Optional.of(character));

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
        when(characterRepo.findById(characterId)).thenReturn(Optional.of(character));
        when(weaponRepo.findById(weaponId)).thenReturn(Optional.of(new Weapon())); // оружие не в инвентаре

        // When / Then
        assertThatThrownBy(() -> service.deleteCharacterWeapon(weaponId, characterId, auth))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("Этого оружия нет в списке вашего персонада");

    }
}

