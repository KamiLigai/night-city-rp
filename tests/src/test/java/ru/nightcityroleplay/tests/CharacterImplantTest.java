package ru.nightcityroleplay.tests;

import io.qameta.allure.Description;
import org.jooq.DSLContext;
import org.jooq.Result;
import org.junit.jupiter.api.*;
import ru.nightcityroleplay.tests.component.AppContext;
import ru.nightcityroleplay.tests.component.BackendRemoteComponent;
import ru.nightcityroleplay.tests.dto.*;
import ru.nightcityroleplay.tests.entity.tables.records.CharactersImplantsRecord;
import ru.nightcityroleplay.tests.entity.tables.records.CharactersRecord;


import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.nightcityroleplay.tests.entity.Tables.CHARACTERS;
import static ru.nightcityroleplay.tests.entity.Tables.CHARACTERS_IMPLANTS;

public class CharacterImplantTest {

    DSLContext dbContext = AppContext.get(DSLContext.class);
    BackendRemoteComponent backendRemote = AppContext.get(BackendRemoteComponent.class);

    @BeforeEach
    void setUserAdmin() {
        UserDto defaultAdmin = AppContext.get("defaultAdmin");
        backendRemote.setCurrentUser(defaultAdmin.id(), defaultAdmin.username(), defaultAdmin.username());
    }

    @Test
    @Description("""
    Дано: В базе есть персонаж с достаточной репутацией и два импланта.
    Действие: Добавить импланты персонажу методом PUT /characters/{characterId}/implants.
    Ожидается: У персонажа в бд появились оба импланта.
    """)
    void updateCharacterImplants_whenValidData_success() {
        // Создать персонажа
        String charName = UUID.randomUUID().toString();
        backendRemote.createCharacter(
            CreateCharacterRequest.builder()
                .name(charName)
                .height(180)
                .weight(75)
                .age(30)
                .organization("org")
                .characterClass("class")
                .reputation(40)
                .build()
        );
        Result<CharactersRecord> charResult = dbContext.select().from(CHARACTERS)
            .where(CHARACTERS.NAME.eq(charName))
            .fetchInto(CHARACTERS);
        UUID characterId = charResult.get(0).getId();

        // Создать импланты и взять их id
        CreateImplantResponse implant1 = backendRemote.createImplant(
            CreateImplantRequest.builder()
                .name("Имплант 1")
                .implantType("OPTICAL_SYSTEM")
                .description("desc1")
                .reputationRequirement(0)
                .implantPointsCost(1)
                .specialImplantPointsCost(0)
                .build()
        );
        CreateImplantResponse implant2 = backendRemote.createImplant(
            CreateImplantRequest.builder()
                .name("Имплант 2")
                .implantType("FRONTAL_LOBE")
                .description("desc2")
                .reputationRequirement(40)
                .implantPointsCost(2)
                .specialImplantPointsCost(0)
                .build()
        );

        // Добавить импланты персонажу
        backendRemote.putCharacterImplants(
            UpdateCharacterImplantsRequest.builder()
                .implantIds(Set.of(implant1.id(), implant2.id()))
                .build(),
            characterId
        );

        // Проверить что оба импланта есть у персонажа
        Result<CharactersImplantsRecord> implants = dbContext.selectFrom(CHARACTERS_IMPLANTS)
            .where(CHARACTERS_IMPLANTS.CHAR_ID.eq(characterId))
            .fetch();
        Set<UUID> actualImplantIds = new HashSet<>();
        for (CharactersImplantsRecord rec : implants) {
            actualImplantIds.add(rec.getImplantId());
        }
        assertThat(actualImplantIds).containsExactlyInAnyOrder(implant1.id(), implant2.id());
    }

    @Test
    @Description("""
    Дано: Персонаж с недостаточной репутацией и имплант с высоким требованием.
    Действие: Добавить имплант персонажу методом PUT /characters/{characterId}/implants.
    Ожидается: Прилетает 400, "Недостаточная репутация".
    """)
    void updateCharacterImplants_whenNotEnoughReputation_throw400() {
        // Создать персонажа с низкой репутацией
        String charName = UUID.randomUUID().toString();
        backendRemote.createCharacter(
            CreateCharacterRequest.builder()
                .name(charName)
                .height(170)
                .weight(60)
                .age(22)
                .organization("org")
                .characterClass("class")
                .reputation(5)
                .build()
        );
        Result<CharactersRecord> charResult = dbContext.select().from(CHARACTERS)
            .where(CHARACTERS.NAME.eq(charName))
            .fetchInto(CHARACTERS);
        UUID characterId = charResult.get(0).getId();

        // Создать имплант с высоким требованием по репутации и взять его id
        CreateImplantResponse implant = backendRemote.createImplant(
            CreateImplantRequest.builder()
                .name("S-Hard-" + UUID.randomUUID())
                .implantType("FRONTAL_LOBE")
                .description("desc")
                .reputationRequirement(10)
                .implantPointsCost(1)
                .specialImplantPointsCost(1)
                .build()
        );

        // Пытаемся выдать имплант персонажу
        HttpResponse response = backendRemote.putCharacterImplants(
            UpdateCharacterImplantsRequest.builder().implantIds(Set.of(implant.id())).build(),
            characterId
        );

        // Проверяем ожидаемую ошибку
        assertThat(response.code()).isEqualTo(400);
        assertThat(response.body()).contains("Недостаточная репутация");
    }

    @Test
    @Description("""
    Дано: Персонаж с одной ОИ, имплант дороже.
    Действие: Добавить имплант.
    Ожидается: 400, недостаточно ОИ.
    """)
    void updateCharacterImplants_whenNotEnoughPoints_throw400() {
        // Создаем персонажа с одной ОИ
        String charName = UUID.randomUUID().toString();
        backendRemote.createCharacter(
            CreateCharacterRequest.builder()
                .name(charName)
                .height(171)
                .weight(60)
                .age(20)
                .organization("org")
                .characterClass("class")
                .reputation(10) // пусть это даёт только 1 обычную ОИ
                .build()
        );
        Result<CharactersRecord> charResult = dbContext.select().from(CHARACTERS)
            .where(CHARACTERS.NAME.eq(charName))
            .fetchInto(CHARACTERS);
        UUID characterId = charResult.get(0).getId();

        // Создаём дорогой имплант
        CreateImplantResponse implant1 = backendRemote.createImplant(
            CreateImplantRequest.builder()
                .name("Big-" + UUID.randomUUID())
                .implantType("OPERATING_SYSTEM")
                .description("expensive")
                .reputationRequirement(0)
                .implantPointsCost(100)
                .specialImplantPointsCost(0)
                .build()
        );

        // Пробуем добавить имплант
        HttpResponse response = backendRemote.putCharacterImplants(
            UpdateCharacterImplantsRequest.builder().implantIds(Set.of(implant1.id())).build(),
            characterId
        );
        assertThat(response.code()).isEqualTo(400);
        assertThat(response.body()).contains("Недостаточно ОИ");
    }

}
