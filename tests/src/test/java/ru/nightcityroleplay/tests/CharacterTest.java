package ru.nightcityroleplay.tests;

import com.fasterxml.jackson.databind.ObjectMapper;
import jdk.jfr.Description;
import lombok.SneakyThrows;
import okhttp3.Response;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ru.nightcityroleplay.tests.component.AppContext;
import ru.nightcityroleplay.tests.component.BackendRemoteComponent;
import ru.nightcityroleplay.tests.dto.*;
import ru.nightcityroleplay.tests.entity.tables.records.CharactersRecord;

import java.util.UUID;
import java.util.stream.Stream;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static ru.nightcityroleplay.tests.entity.Tables.CHARACTERS;

public class CharacterTest {

    DSLContext dbContext = AppContext.get(DSLContext.class);
    BackendRemoteComponent backendRemote = AppContext.get(BackendRemoteComponent.class);
    ObjectMapper objectMapper = AppContext.get(ObjectMapper.class);

    @BeforeEach
    void setUp() {
        UserDto user = AppContext.get("defaultUser");
        backendRemote.setCurrentUser(user.id(), user.username(), user.username());
    }

    @AfterAll
    static void afterAll() {
        UserDto user = AppContext.get("defaultUser");
        AppContext.get(BackendRemoteComponent.class)
            .setCurrentUser(user.id(), user.username(), user.username());
    }

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

    @ParameterizedTest(name = "{index} - Проверка с данными: {0}, ожидаемое сообщение: {1}")
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
            Arguments.of(CreateCharacterRequest.builder().age(null).reputation(0).build(), "Возраст не может быть 0 или меньше или null"),
            Arguments.of(CreateCharacterRequest.builder().age(0).reputation(0).build(), "Возраст не может быть 0 или меньше или null"),
            Arguments.of(CreateCharacterRequest.builder().age(-1).reputation(0).build(), "Возраст не может быть 0 или меньше или null"),
            Arguments.of(CreateCharacterRequest.builder().age(10).reputation(null).build(), "Репутация не может быть меньше 0 или null"),
            Arguments.of(CreateCharacterRequest.builder().age(10).reputation(-1).build(), "Репутация не может быть меньше 0 или null")
        );
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
        Result<Record> result = dbContext.select().from(CHARACTERS)
            .where(CHARACTERS.NAME.eq(charName))
            .fetch();

        Response response2 = backendRemote.makeCreateCharacterRequest(
            CreateCharacterRequest.builder()
                .name(charName)
                .age(22)
                .reputation(0)
                .build()
        );
        assertThat(response2.code()).isEqualTo(422);
        assertThat(response2.body().string()).contains("Персонаж с таким именем уже есть");
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

        assertThat(charResult).hasSize(1);

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
        assertThat(response.body().string()).contains("не найден");
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
        CreateCharacterResponse responseChar = backendRemote.createCharacter(
            CreateCharacterRequest.builder()
                .name(charName)
                .age(205)
                .reputation(0)
                .build()
        );

        // Сменить юзера
        String username = randomUUID().toString();
        String password = randomUUID().toString();
        UserDto userDto = backendRemote.createUser(username, password);
        backendRemote.setCurrentUser(userDto.id(), username, password);

        // Удалить персонажа
        Response response = backendRemote.makeDeleteCharacterRequest(responseChar.id());

        assertThat(response.code()).isEqualTo(403);
        assertThat(response.body().string()).contains("Удалить чужого персонажа вздумал? а ты хорош.");

        Result<CharactersRecord> charRecord = dbContext.select().from(CHARACTERS)
            .where(CHARACTERS.NAME.eq(charName))
            .fetchInto(CHARACTERS);

        assertThat(charRecord).hasSize(1);
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
    @SneakyThrows
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
        assertThat(response.body().string()).contains("не найден");
        assertThat(response.code()).isEqualTo(404);
    }


    @Test
    @Description("""
        Дано: Несколько персонажей
        Действие: Получить страницу персонажей методом GET /characters.
                  Размер страницы меньше чем персонажей в бд.
        Ожидается: Получена страница данных персонажей.
        """)
    void getCharacterPage() {
        // Создать несколько персонажей
        backendRemote.createCharacter(
            CreateCharacterRequest.builder()
                .name(randomUUID().toString())
                .age(10000)
                .reputation(0)
                .build()
        );
        backendRemote.createCharacter(
            CreateCharacterRequest.builder()
                .name(randomUUID().toString())
                .age(10001)
                .reputation(0)
                .build()
        );
        backendRemote.createCharacter(
            CreateCharacterRequest.builder()
                .name(randomUUID().toString())
                .age(10002)
                .reputation(0)
                .build()
        );

        // Получить страницу персонажей
        PageDto<Object> charPageRecord = backendRemote.getCharacterPage(2);

        assertThat(charPageRecord.getContent().size()).isEqualTo(2);
        assertThat(charPageRecord.getNumberOfElements()).isEqualTo(2);
        assertThat(charPageRecord.getSize()).isEqualTo(2);
    }

    @Test
    @Description("""
        Дано: Персонаж с id.
        Действие: Изменить персонажа по id методом PUT /characters/{id}.
        Ожидается: Персонаж в бд обновлен.
        """)
    void updateCharacter() {
        String charName = randomUUID().toString();
        backendRemote.createCharacter(
            CreateCharacterRequest.builder()
                .name(charName)
                .age(1000)
                .reputation(0)
                .build()
        );

        // Получить персонажа
        Result<CharactersRecord> charRecord = dbContext.select().from(CHARACTERS)
            .where(CHARACTERS.NAME.eq(charName))
            .fetchInto(CHARACTERS);
        UUID charId = charRecord.get(0).getId();

        //Изменить персонажа
        UpdateCharacterRequest request = createUpdateCharacterRequest();
        backendRemote.updateCharacter(
            charId,
            UpdateCharacterRequest.builder()
                .name(request.name())
                .age(request.age())
                .reputation(request.reputation())
                .build()
        );

        CharacterDto charDto = backendRemote.getCharacter(charId);

        assertThat(charDto.getName()).isEqualTo(request.name());
        assertThat(charDto.getAge()).isEqualTo(request.age());
        assertThat(charDto.getReputation()).isEqualTo(request.reputation());
    }

    @Test
    @SneakyThrows
    @Description("""
         Дано: Персонаж отсутствует
          Действие: Изменить персонажа по id методом PUT /characters/{id}
          Ожидается: Ошибка 404, персонаж не найден
        """)
    void updateNonExistingCharacter() {
        //Изменить персонажа
        Response response = backendRemote.makeUpdateCharacterRequest(
            randomUUID(),
            UpdateCharacterRequest.builder()
                .name(randomUUID().toString())
                .age(1000)
                .reputation(0)
                .build()
        );

        assertThat(response.code()).isEqualTo(404);
        assertThat(response.body().string()).contains("не найден");
    }

    @ParameterizedTest(name = "guardTest")
    @MethodSource("updateCharacterWithBadRequestData")
    @Description("""
        Дано: Персонаж с id.
        Действие: Изменить персонажа по id методом PUT /characters/{id} с некорректными данными.
        Ожидается: 400 Bad_Request.
                   Никакой персонаж не был изменён.
        """)
    void updateCharacterWithBadRequest(UpdateCharacterRequest request) {
        String charName = randomUUID().toString();
        backendRemote.createCharacter(
            CreateCharacterRequest.builder()
                .name(charName)
                .age(1000)
                .reputation(0)
                .build()
        );

        // Получить персонажа
        Result<CharactersRecord> charRecord = dbContext.select().from(CHARACTERS)
            .where(CHARACTERS.NAME.eq(charName))
            .fetchInto(CHARACTERS);
        UUID charId = charRecord.get(0).getId();

        //Изменить персонажа
        Response response = backendRemote.makeUpdateCharacterRequest(
            charId,
            UpdateCharacterRequest.builder()
                .name(request.name())
                .age(request.age())
                .reputation(request.reputation())
                .build()
        );

        assertThat(response.code()).isEqualTo(400);
    }

    public static Stream<Arguments> updateCharacterWithBadRequestData() {
        return Stream.of(
            Arguments.of(UpdateCharacterRequest.builder().name("UPDATED" + randomUUID()).age(null).reputation(null).build()));
    }


    @Test
    @SneakyThrows
    @Description("""
        Дано: Персонаж юзера 1.
        Действие: Юзер 2 изменяет персонажа по id методом PUT /characters/{id}.
        Ожидается: Ошибка 403, нельзя менять чужого персонажа.
                   Никакой персонаж не был изменён.
        """)
    void updateNotOwnedCharacter() {
        // Создать персонажа
        String charName = randomUUID().toString();
        backendRemote.createCharacter(
            CreateCharacterRequest.builder()
                .name(charName)
                .age(1001)
                .reputation(0)
                .build()
        );

        // Сменить юзера
        UUID userId = randomUUID();
        String username = randomUUID().toString();
        String password = randomUUID().toString();
        UserDto userDto = backendRemote.createUser(username, password);
        backendRemote.setCurrentUser(userDto.id(), username, password);

        // Изменить персонажа
        Result<CharactersRecord> charRecord = dbContext.select().from(CHARACTERS)
            .where(CHARACTERS.NAME.eq(charName))
            .fetchInto(CHARACTERS);

        UpdateCharacterRequest request = createUpdateCharacterRequest();
        Response response = backendRemote.makeUpdateCharacterRequest(charRecord.get(0).getId(),
            UpdateCharacterRequest.builder()
                .name(request.name())
                .age(request.age())
                .reputation(request.reputation())
                .build()
        );

        assertThat(charRecord).hasSize(1);
        assertThat(charRecord.get(0).getOwnerId().equals(userId)).isFalse();
        assertThat(response.code()).isEqualTo(403);
        assertThat(response.body().string()).contains("Изменить чужого персонажа вздумал? а ты хорош.");
    }

    @Test
    @Description("""
        Дано: Персонаж с id.
        Действие: Изменить персонажа по id методом PUT /characters/{id} без аутентификации.
        Ожидается: Ошибка 401, юзер не аутентифицирован.
                   Никакой персонаж не был изменён.
        """)
    void updateCharacterWithoutAuthentication() {

        String charName = randomUUID().toString();
        backendRemote.createCharacter(
            CreateCharacterRequest.builder()
                .name(charName)
                .age(1000)
                .reputation(0)
                .build()
        );

        // Получить персонажа
        Result<CharactersRecord> charRecord = dbContext.select().from(CHARACTERS)
            .where(CHARACTERS.NAME.eq(charName))
            .fetchInto(CHARACTERS);
        UUID charId = charRecord.get(0).getId();

        //Изменить персонажа
        UpdateCharacterRequest request = createUpdateCharacterRequest();
        Response response = backendRemote.makeUpdateCharacterWithoutAutentication(
            charId,
            UpdateCharacterRequest.builder()
                .name(request.name())
                .age(request.age())
                .reputation(request.reputation())
                .build()
        );

        assertThat(response.code()).isEqualTo(401);
    }

    private UpdateCharacterRequest createUpdateCharacterRequest() {
        return (UpdateCharacterRequest.builder().name("UPDATED" + randomUUID()).age(100).reputation(0).build());
    }
}
