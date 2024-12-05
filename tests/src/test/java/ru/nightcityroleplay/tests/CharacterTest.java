package ru.nightcityroleplay.tests;

import com.fasterxml.jackson.databind.ObjectMapper;
import jdk.jfr.Description;
import lombok.SneakyThrows;
import okhttp3.Response;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ru.nightcityroleplay.tests.component.AppContext;
import ru.nightcityroleplay.tests.component.BackendRemoteComponent;
import ru.nightcityroleplay.tests.dto.CharacterDto;
import ru.nightcityroleplay.tests.dto.CreateCharacterRequest;
import ru.nightcityroleplay.tests.dto.ErrorResponse;
import ru.nightcityroleplay.tests.dto.UserDto;
import ru.nightcityroleplay.tests.entity.tables.records.CharactersRecord;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static ru.nightcityroleplay.tests.entity.Tables.CHARACTERS;

public class CharacterTest {

    DSLContext dbContext = AppContext.get(DSLContext.class);
    BackendRemoteComponent backendRemote = AppContext.get(BackendRemoteComponent.class);
    ObjectMapper objectMapper = AppContext.get(ObjectMapper.class);

    //todo ParametrizedTest

    @Test
    @SneakyThrows
    @Description("""
        Дано: Пустая бд.
        Действие: Добавить персонажа методом POST /characters.
        Ожидается: Персонаж добавлен в бд.
        """)
    void createCharacter() {
        // Создать персонажа
        String charName = randomUUID().toString();
        backendRemote.createCharacter(
            CreateCharacterRequest.builder()
                .name(charName)
                .age(20)
                .reputation(0)
                .build()
        );

        // Проверить нового персонажа
        Result<CharactersRecord> charResult = dbContext.select().from(CHARACTERS)
            .where(CHARACTERS.NAME.eq(charName))
            .fetchInto(CHARACTERS);

        assertThat(charResult).hasSize(1);
        assertThat(charResult.get(0))
            .satisfies(
                character -> assertThat(character.getId()).isNotNull(),
                character -> assertThat(character.getOwnerId())
                    .isEqualTo(backendRemote.remote().getUserId()),
                character -> assertThat(character.getName()).isEqualTo(charName),
                character -> assertThat(character.getAge()).isEqualTo(20)
            );
    }

    @ParameterizedTest
    @MethodSource("createCharacterWithBadRequestData")
    @SneakyThrows
    @Description("""
        Дано: Пустая бд.
        Действие: Добавить персонажа методом POST /characters с некорректными данными.
        Ожидается: 400 Bad_Request.
                   Новый персонаж не создан в бд.
        """)
    void createCharacterWithBadRequest(CreateCharacterRequest request, String expectedMessage) {
        // Создать персонажа
        String charName = randomUUID().toString();
        Response response = backendRemote.makeCreateCharacterRequest(
            CreateCharacterRequest.builder()
                .name(charName)
                .age(request.age())
                .reputation(request.reputation())
                .build()
        );
        assertThat(response.code()).isEqualTo(400);
        var body = objectMapper.readValue(response.body().string(), ErrorResponse.class);
        assertThat(body.message()).isEqualTo(expectedMessage);

        // Проверить что новый персонаж не был создан
        Result<Record> result = dbContext.select().from(CHARACTERS)
            .where(CHARACTERS.NAME.eq(charName))
            .fetch();

        assertThat(result).isEmpty();
    }

    public static Stream<Arguments> createCharacterWithBadRequestData() {
        // roles, expectedAuthorities
        return Stream.of(
            Arguments.of(CreateCharacterRequest.builder().age(null).build(), "Возраст не может быть null"));
    }

    @Test
    @SneakyThrows
    @Description("""
        Дано: Персонаж с определённым именем.
        Действие: Добавить нового персонажа с таким же именем методом POST /characters.
        Ожидается: 422 UNPROCESSABLE_ENTITY.
                   Новый персонаж не создан в бд.
        """)
    void createCharacterWithSameName() {
        String charName = randomUUID().toString();
        backendRemote.makeCreateCharacterRequest(
            CreateCharacterRequest.builder()
                .name(charName)
                .age(22)
                .reputation(0)
                .build()
        );
        Response response2 = backendRemote.makeCreateCharacterRequest(
            CreateCharacterRequest.builder()
                .name(charName)
                .age(22)
                .reputation(0)
                .build()
        );
        assertThat(response2.code()).isEqualTo(422);

        // Проверить что новый персонаж не был создан
        Result<Record> result = dbContext.select().from(CHARACTERS)
            .where(CHARACTERS.NAME.eq(charName))
            .fetch();

        assertThat(result).size().isEqualTo(1);
    }

    @Test
    @Description("""
        Дано: Персонаж с id.
        Действие: Удалить персонажа методом DELETE /characters/{id}.
        Ожидается: Персонаж удалён из бд.
        """)
    void deleteCharacter() {
        // Создать персонажа
        String charName = randomUUID().toString();
        backendRemote.createCharacter(
            CreateCharacterRequest.builder()
                .name(charName)
                .age(20)
                .reputation(0)
                .build()
        );

        // Проверить нового персонажа
        Result<CharactersRecord> charResult = dbContext.select().from(CHARACTERS)
            .where(CHARACTERS.NAME.eq(charName))
            .fetchInto(CHARACTERS);

        charResult.get(0).getId();

        assertThat(charResult).hasSize(1);
        assertThat(charResult.get(0))
            .satisfies(
                character -> assertThat(character.getId()).isNotNull(),
                character -> assertThat(character.getOwnerId())
                    .isEqualTo(backendRemote.remote().getUserId()),
                character -> assertThat(character.getName()).isEqualTo(charName),
                character -> assertThat(character.getAge()).isEqualTo(20)
            );
        // Удалить персонажа
        backendRemote.deleteCharacter(charResult.get(0).getId());

        // Проверить удаление персонажа
        Result<Record> result = dbContext.select().from(CHARACTERS)
            .where(CHARACTERS.NAME.eq(charName))
            .fetch();

        assertThat(result).size().isEqualTo(0);
    }

    @Test
    @SneakyThrows
    @Description("""
        Дано: Персонаж 1.
        Действие: Удалить персонажа 2 методом DELETE /characters/{id}.
        Ожидается: 404 Not Found.
                   Никакой персонаж не удалён.
        """)
    void deleteNonExistingCharacter() {
        // Удалить персонажа
        UUID charId = randomUUID();
        Response response = backendRemote.makeDeleteCharacterRequest(charId);

        // Проверить удаление персонажа
        Result<Record> result = dbContext.select().from(CHARACTERS)
            .where(CHARACTERS.ID.eq(charId))
            .fetch();

        assertThat(response.code()).isEqualTo(404);
        assertThat(result).size().isEqualTo(0);
    }

    @Test
    @SneakyThrows
    @Description("""
        Дано: Персонаж владельца 1.
        Действие: Удалить персонажа владельца 1 методом DELETE /characters/{id} от имени владельца 2.
        Ожидается: 403 Forbidden.
                   Никакой персонаж не удалён.
        """)
    void deleteNotOwnedCharacter() {
        // Создать персонажа
        String charName = randomUUID().toString();
        backendRemote.createCharacter(
            CreateCharacterRequest.builder()
                .name(charName)
                .age(205)
                .reputation(0)
                .build()
        );

        // Сменить юзера
        UUID userId = randomUUID();
        String username = randomUUID().toString();
        String password = randomUUID().toString();
        UserDto userDto = backendRemote.createUser(username, password);
        backendRemote.setCurrentUser(userDto.id(), username, password);

        // Удалить персонажа
        Result<CharactersRecord> charRecord = dbContext.select().from(CHARACTERS)
            .where(CHARACTERS.NAME.eq(charName))
            .fetchInto(CHARACTERS);

        Response response = backendRemote.makeDeleteCharacterRequest(charRecord.get(0).getId());

        assertThat(charRecord).hasSize(1);
        assertThat(charRecord.get(0).getOwnerId().equals(userId)).isFalse();
        assertThat(response.code()).isEqualTo(403);
    }

    @Test
    @Description("""
        Дано: Персонаж.
        Действие: Получить персонажа методом GET /characters/{id}.
        Ожидается: Получены данные персонажа
        """)
    void getCharacter() {
        // Создать персонажа
        String charName = randomUUID().toString();
        backendRemote.createCharacter(
            CreateCharacterRequest.builder()
                .name(charName)
                .age(240)
                .reputation(0)
                .build()
        );

        // Получить персонажа
        Result<CharactersRecord> charRecord = dbContext.select().from(CHARACTERS)
            .where(CHARACTERS.NAME.eq(charName))
            .fetchInto(CHARACTERS);
        CharacterDto charDto = backendRemote.getCharacter(charRecord.get(0).getId());

        assertThat(charRecord).hasSize(1);
        assertThat(charDto.getName()).isEqualTo(charRecord.get(0).getName());
        assertThat(charDto.getId()).isEqualTo(charRecord.get(0).getId());
        assertThat(charDto.getAge()).isEqualTo(charRecord.get(0).getAge());
    }

    @Test
    @Description("""
        Дано: Пустая бд.
        Действие: Получить персонажа методом GET /characters/{id}.
        Ожидается: 404 Not found.
                   Данные персонажа не получены.
        """)
    void getNonExistingCharacter() {

        String newCharName = randomUUID().toString();

        // Получить персонажа

        Result<CharactersRecord> charRecord = dbContext.select().from(CHARACTERS)
            .where(CHARACTERS.NAME.eq(newCharName))
            .fetchInto(CHARACTERS);

        Response response = backendRemote.makeGetCharacterRequest(randomUUID());

        assertThat(charRecord).hasSize(0);
        assertThat(response.code()).isEqualTo(404);
    }


    void createUser() {
        backendRemote.createUser("test", "test");
    }
}
