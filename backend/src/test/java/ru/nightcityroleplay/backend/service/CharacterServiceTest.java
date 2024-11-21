package ru.nightcityroleplay.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.server.ResponseStatusException;
import ru.nightcityroleplay.backend.dto.*;
import ru.nightcityroleplay.backend.entity.CharacterEntity;
import ru.nightcityroleplay.backend.entity.Implant;
import ru.nightcityroleplay.backend.entity.User;
import ru.nightcityroleplay.backend.repo.CharacterRepository;
import ru.nightcityroleplay.backend.repo.ImplantRepository;
import ru.nightcityroleplay.backend.repo.SkillRepository;

import java.nio.file.AccessDeniedException;
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


    CharacterRepository charRepo;
    SkillRepository skillRepo;
    ImplantRepository implantRepo;

    Authentication auth;

    @BeforeEach
    void setUp() {
        charRepo = mock();
        skillRepo = mock();
        characterStatsService = mock();
        implantRepo = mock();
        service = new CharacterService(charRepo, characterStatsService, skillRepo, implantRepo);
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
        character.setOwnerId(owId);
        character.setId(charId);
        character.setName("Vasyatka");
        character.setAge(42);

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
        oldCharacter.setOwnerId(UUID.randomUUID());

        var user = new User();
        user.setId(UUID.randomUUID());

        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(user);

        when(charRepo.findById(characterId)).thenReturn(Optional.of(oldCharacter));

        // then
        assertThatThrownBy(() -> service.updateCharacter(request, characterId, auth))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("Изменить чужого персонажа вздумал? а ты хорош.");
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

        // then
        assertThatThrownBy(() -> service.deleteCharacter(characterId, authentication))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("Удалить чужого персонажа вздумал? а ты хорош.");
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
