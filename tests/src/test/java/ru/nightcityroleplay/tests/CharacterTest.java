package ru.nightcityroleplay.tests;

import jdk.jfr.Description;
import lombok.SneakyThrows;
import org.jooq.DSLContext;
import org.jooq.Result;
import org.junit.jupiter.api.Test;
import ru.nightcityroleplay.tests.component.AppContext;
import ru.nightcityroleplay.tests.component.BackendRemoteComponent;
import ru.nightcityroleplay.tests.dto.CreateCharacterRequest;
import ru.nightcityroleplay.tests.entity.tables.records.CharactersRecord;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static ru.nightcityroleplay.tests.entity.Tables.CHARACTERS;

public class CharacterTest {

    DSLContext dbContext = AppContext.get(DSLContext.class);
    BackendRemoteComponent backendRemote = AppContext.get(BackendRemoteComponent.class);

    @Test
    void name() {
        fail();
    }

    @Test
    @SneakyThrows
    @Description("""
        Создать персонажа.
        Убедиться, что новый персонаж создан корректно.
        """)
    void createCharacter() {
        // Создать персонажа
        String charName = randomUUID().toString();
        backendRemote.createCharacter(
            CreateCharacterRequest.builder()
                .name(charName)
                .age(20)
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
}
