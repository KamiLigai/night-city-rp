package ru.nightcityroleplay.backend.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.core.Authentication;
import ru.nightcityroleplay.backend.dto.CharacterDto;
import ru.nightcityroleplay.backend.dto.CreateCharacterRequest;
import ru.nightcityroleplay.backend.dto.UpdateCharacterRequest;
import ru.nightcityroleplay.backend.entity.CharacterEntity;
import ru.nightcityroleplay.backend.entity.User;
import ru.nightcityroleplay.backend.exception.NightCityRpException;
import ru.nightcityroleplay.backend.repo.CharacterRepository;
import ru.nightcityroleplay.backend.repo.WeaponRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CharacterServiceTest {

    CharacterService service;
    CharacterRepository repo;
    WeaponRepository weaponRepo;



    @BeforeEach
    void setUp() {

        repo = mock();
        weaponRepo = mock();
        service = new CharacterService(repo, weaponRepo);
    }


    @Test
    void test1() {
        System.out.println("123");
    }

    @Test
    @Disabled
    void test2() {
        throw new RuntimeException();
    }

    @Test
    void test3() {
        // given
        int x = 2;
        int y = 3;

        // when
        int result = y - x;

        // then
        assertEquals(result, 1);
    }

    @Test
    void test4() {
        // given
        int x = 10;
        int z = 3;

        // when
        int result = 10 % 3;

        // then
        assertEquals(result, 1);

    }

    @Test
    void test5() {
        //given
        List<Integer> x = new ArrayList<Integer>();
        x.add(2);

        //when
        x.add(42);

        // then
        assertEquals(x.size(), 2);
    }

    @Test
    void test6() {
        //given
        List<Integer> x = new ArrayList<Integer>();
        x.add(2);

        //when
        x.add(42);

        // then
        assertThat(x).hasSize(2);
        assertThat(x).contains(42);
    }

    @Test
    void getCharacterWhenCharacterIsAbsent() {
        //given
        UUID id = randomUUID();
        when(repo.findById(id))
            .thenReturn(Optional.empty());

        //when
        var result = service.getCharacter(id);


        // then
        assertThat(result).isNull();
        verify(repo).findById(id);
    }

//    @Test
//    void getCharacterWhenCharacterIsPresent() {
//        //given
//        UUID id = randomUUID();
//        var character = new CharacterEntity();
//        character.setId(id);
//        character.setName("test-name");
//        when(repo.findById(any()))
//            .thenReturn(Optional.of(character));
//
//
//        //when
//        var result = service.getCharacter(id);
//
//        // then
//        assertThat(result).isNotNull();
//        verify(repo, times(1)).findById(any());
//    }

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
        when(repo.save(any()))
            .thenReturn(character);

        // when
        var result = service.createCharacter(request, auth);
        // verify(mock).someMethod(); - правильно
        // verify(mock.someMethod()); - не правильно

        // then
        verify(repo).save(any());
        verify(repo, never()).deleteById(any());
        Object someObject = mock();
        verifyNoInteractions(someObject);

    }


    @Test
    void toDtoIsNotNull() {
        //given
        UUID owId = randomUUID();
        UUID charId = randomUUID();
        CharacterEntity character = new CharacterEntity();
        character.setOwnerId(owId);
        character.setId(charId);
        character.setName("Vasyatka");
        character.setAge(42);

        when(repo.findById(charId))
            .thenReturn(Optional.of(character));

        //when
        var result = service.getCharacter(charId);

        //then

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

        when(repo.findById(characterId)).thenReturn(Optional.empty());

        // when/then
        Assertions.assertThrows(
            NightCityRpException.class,
            () -> service.updateCharacter(request, characterId, auth)
        );
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

        when(repo.findById(characterId)).thenReturn(Optional.of(oldCharacter));

        // when/then
        Assertions.assertThrows(
            NightCityRpException.class,
            () -> service.updateCharacter(request, characterId, auth)
        );
    }





    @Test
    public void testDeleteCharacterSuccess() {
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

        when(repo.findById(characterId)).thenReturn(java.util.Optional.of(character));

        // when
        service.deleteCharacter(characterId, auth);

        // then
        verify(repo, times(1)).deleteById(characterId);
    }

    @Test
    public void testDeleteCharacterNotFound() {
        // given
        UUID characterId = UUID.randomUUID();
        Authentication authentication = mock(Authentication.class);

        // when
        when(repo.findById(characterId)).thenReturn(java.util.Optional.empty());

        // then
        assertThrows(NightCityRpException.class, () -> {
            service.deleteCharacter(characterId, authentication);
        });
    }

    @Test
    public void testDeleteCharacterUnauthorized() {
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

        when(repo.findById(characterId)).thenReturn(java.util.Optional.of(character));

        // when, then
        assertThrows(NightCityRpException.class, () -> {
            service.deleteCharacter(characterId, authentication);
        });
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
        when(repo.findById(charId))
            .thenReturn(Optional.of(character));

        // when
        service.updateCharacter(request, charId, auth);

        // then
        ArgumentCaptor<CharacterEntity> charCaptor = ArgumentCaptor.captor();
        verify(repo).save(charCaptor.capture());
        CharacterEntity savedChar = charCaptor.getValue();
        assertThat(savedChar.getId()).isEqualTo(charId);
        assertThat(savedChar.getOwnerId()).isEqualTo(user.getId());
        assertThat(savedChar.getName()).isEqualTo("test-name");
        assertThat(savedChar.getAge()).isEqualTo(42);
    }
}