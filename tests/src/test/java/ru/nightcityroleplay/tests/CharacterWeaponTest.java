package ru.nightcityroleplay.tests;

import io.qameta.allure.Description;
import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.jooq.DSLContext;
import org.jooq.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.nightcityroleplay.tests.component.AppContext;
import ru.nightcityroleplay.tests.component.BackendRemoteComponent;
import ru.nightcityroleplay.tests.dto.*;
import ru.nightcityroleplay.tests.entity.tables.records.CharactersRecord;
import ru.nightcityroleplay.tests.entity.tables.records.CharactersWeaponsRecord;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static ru.nightcityroleplay.tests.entity.Tables.CHARACTERS;
import static ru.nightcityroleplay.tests.entity.Tables.CHARACTERS_WEAPONS;

public class CharacterWeaponTest {

    DSLContext dbContext = AppContext.get(DSLContext.class);
    BackendRemoteComponent backendRemote = AppContext.get(BackendRemoteComponent.class);

    @BeforeEach
    @Test
    void setUserAdmin() {
        UserDto defaultAdmin = AppContext.get("defaultAdmin");
        backendRemote.setCurrentUser(defaultAdmin.id(), defaultAdmin.username(), defaultAdmin.username());
    }

    @Test
    @SneakyThrows
    @Description("""
        Дано: В базе есть персонаж и два оружия, пользователь — владелец персонажа.
        Действие: Добавить оружия персонажу методом PUT /characters/{characterId}/weapons.
        Ожидается: У персонажа в бд появились оба оружия.
        """)
    void putCharacterWeapon_success() {
        String charName = randomUUID().toString();
        backendRemote.makeCreateCharacterRequest(
            CreateCharacterRequest.builder()
                .name(charName)
                .height(170)
                .weight(70)
                .age(25)
                .organization("org")
                .characterClass("class")
                .reputation(40)
                .build()
        );
        // Проверить нового персонажа
        Result<CharactersRecord> charResult = dbContext.select().from(CHARACTERS)
            .where(CHARACTERS.NAME.eq(charName))
            .fetchInto(CHARACTERS);

        // Получить ID персонажа
        UUID characterId = charResult.get(0).getId();

        WeaponDto weapon1 = backendRemote.createWeapon(
            CreateWeaponRequest.builder()
                .isMelee(false)
                .name("Револьвер тест 1")
                .weaponType("пистолет")
                .penetration(1)
                .reputationRequirement(30)
                .build()
        );
        WeaponDto weapon2 = backendRemote.createWeapon(
            CreateWeaponRequest.builder()
                .isMelee(false)
                .name("Револьвер тест 2")
                .weaponType("пистолет")
                .penetration(2)
                .reputationRequirement(40)
                .build()
        );

        // Получить ID оружия
        UUID weaponId1 = weapon1.getId();
        UUID weaponId2 = weapon2.getId();

        // Добавить оружия персонажу
        backendRemote.putCharacterWeapon(
            UpdateCharacterWeaponRequest.builder()
                .weaponIds(List.of(weaponId1, weaponId2))
                .build(), characterId
        );

        // Проверить, что у персонажа появились оба оружия
        Result<CharactersWeaponsRecord> records = dbContext.selectFrom(CHARACTERS_WEAPONS)
            .where(CHARACTERS_WEAPONS.CHAR_ID.eq(characterId))
            .fetch();

        Assertions.assertThat(records).hasSize(2);
    }

    @Test
    @SneakyThrows
    @Description("""
        Дано У персонажа уже есть оружие.
        Действие Обновить список оружия (передать пустой список) методом PUT /characters/{characterId}/weapons.
        Ожидается Все связи персонажа с оружием удалены.
        """)
    void putCharacterWeapon_removeAllWeapons() {
        // Создаём персонажа
        String charName = randomUUID().toString();
        backendRemote.createCharacter(
            CreateCharacterRequest.builder()
                .name(charName)
                .height(180)
                .weight(60)
                .age(20)
                .organization("test")
                .characterClass("test")
                .reputation(40)
                .build()
        );
        Result<CharactersRecord> charResult = dbContext.select().from(CHARACTERS)
            .where(CHARACTERS.NAME.eq(charName))
            .fetchInto(CHARACTERS);

        assertThat(charResult).hasSize(1);
        UUID characterId = charResult.get(0).getId();

        // Добавляем оружие
        WeaponDto weapon = backendRemote.createWeapon(
            CreateWeaponRequest.builder()
                .isMelee(true)
                .name("Меч тест")
                .weaponType(null)
                .penetration(3)
                .reputationRequirement(40)
                .build()
        );

        backendRemote.putCharacterWeapon(
            UpdateCharacterWeaponRequest.builder().weaponIds(List.of(weapon.getId())).build(),
            characterId
        );

        // Проверяем, что связь с оружием появилась
        Result<CharactersWeaponsRecord> beforeClear = dbContext
            .selectFrom(CHARACTERS_WEAPONS)
            .where(CHARACTERS_WEAPONS.CHAR_ID.eq(characterId))
            .fetch();

        Assertions.assertThat(beforeClear)
            .overridingErrorMessage("Оружие не добавилось персонажу — проверить createWeapon/putCharacterWeapon")
            .hasSize(1);

        // Очищаем (ставим пустой список)
        backendRemote.putCharacterWeapon(
            UpdateCharacterWeaponRequest.builder().weaponIds(Collections.emptyList()).build(),
            characterId
        );

        // Ещё раз проверяем
        Result<CharactersWeaponsRecord> afterClear = dbContext
            .selectFrom(CHARACTERS_WEAPONS)
            .where(CHARACTERS_WEAPONS.CHAR_ID.eq(characterId))
            .fetch();

        if (!afterClear.isEmpty()) {
            afterClear.forEach(r -> System.err.println("НЕ УДАЛЕНО: char_id=" + r.getCharId() + " weapon_id=" + r.getWeaponId()));
        }

        Assertions.assertThat(afterClear)
            .overridingErrorMessage("Ожидалось, что после PUT с пустым списком у персонажа не останется связей с оружием, но они есть!")
            .isEmpty();
    }


    @Test
    @SneakyThrows
    @Description("""
    Дано У персонажа уже есть несколько оружий.
    Действие Обновить список, оставить только одно оружие методом PUT /characters/{characterId}/weapons.
    Ожидается У персонажа в бд только это оружие.
    """)
    void putCharacterWeaponsetOnlyOne() {
        String charName = randomUUID().toString();
        backendRemote.createCharacter(
            CreateCharacterRequest.builder()
                .name(charName)
                .height(180)
                .weight(90)
                .age(40)
                .organization("org5")
                .characterClass("class5")
                .reputation(40)
                .build()
        );
        Result<CharactersRecord> charResult = dbContext.select().from(CHARACTERS)
            .where(CHARACTERS.NAME.eq(charName))
            .fetchInto(CHARACTERS);
        UUID characterId = charResult.get(0).getId();

        WeaponDto weapon1 = backendRemote.createWeapon(
            CreateWeaponRequest.builder()
                .isMelee(false)
                .name("Пушка тест 5а")
                .weaponType("пистолет")
                .penetration(2)
                .reputationRequirement(42)
                .build()
        );
        WeaponDto weapon2 = backendRemote.createWeapon(
            CreateWeaponRequest.builder()
                .isMelee(false)
                .name("Пушка тест 5б")
                .weaponType("пистолет")
                .penetration(3)
                .reputationRequirement(55)
                .build()
        );

        backendRemote.putCharacterWeapon(
            UpdateCharacterWeaponRequest.builder().weaponIds(List.of(weapon1.getId(), weapon2.getId())).build(),
            characterId
        );

        // Теперь обновляем: оставляем только второе оружие
        backendRemote.putCharacterWeapon(
            UpdateCharacterWeaponRequest.builder().weaponIds(List.of(weapon2.getId())).build(),
            characterId
        );

        // Проверяем - только одно оружие
        Result<CharactersWeaponsRecord> records = dbContext.selectFrom(CHARACTERS_WEAPONS)
            .where(CHARACTERS_WEAPONS.CHAR_ID.eq(characterId))
            .fetch();

        // Ожидаем только одну запись, и именно с этим id
        Assertions.assertThat(records).hasSize(1);
        Assertions.assertThat(records.get(0).getWeaponId()).isEqualTo(weapon2.getId());
    }


}
