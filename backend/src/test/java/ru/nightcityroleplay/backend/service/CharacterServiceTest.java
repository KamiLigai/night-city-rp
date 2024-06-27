package ru.nightcityroleplay.backend.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import ru.nightcityroleplay.backend.dto.CharacterDto;
import ru.nightcityroleplay.backend.dto.CreateCharacterRequest;
import ru.nightcityroleplay.backend.entity.CharacterEntity;
import ru.nightcityroleplay.backend.entity.User;
import ru.nightcityroleplay.backend.repo.CharacterRepository;

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


    @BeforeEach
    void setUp() {
        repo = mock();
        service = new CharacterService(repo);
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
}