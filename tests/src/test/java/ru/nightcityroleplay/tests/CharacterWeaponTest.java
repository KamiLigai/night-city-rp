package ru.nightcityroleplay.tests;

import io.qameta.allure.Description;
import lombok.SneakyThrows;
import org.jooq.DSLContext;
import org.jooq.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.nightcityroleplay.tests.component.AppContext;
import ru.nightcityroleplay.tests.component.BackendRemoteComponent;
import ru.nightcityroleplay.tests.dto.CreateCharacterRequest;
import ru.nightcityroleplay.tests.dto.CreateWeaponRequest;
import ru.nightcityroleplay.tests.dto.UserDto;
import ru.nightcityroleplay.tests.dto.WeaponDto;
import ru.nightcityroleplay.tests.entity.tables.records.CharactersRecord;
import ru.nightcityroleplay.tests.entity.tables.records.WeaponsRecord;
import ru.nightcityroleplay.tests.repo.ImplantRepo;
import ru.nightcityroleplay.tests.repo.WeaponRepo;

import static java.util.UUID.randomUUID;
import static ru.nightcityroleplay.tests.entity.Tables.CHARACTERS;

public class CharacterWeaponTest {

    DSLContext dbContext = AppContext.get(DSLContext.class);
    BackendRemoteComponent backendRemote = AppContext.get(BackendRemoteComponent.class);
    WeaponRepo weaponRepo = AppContext.get(WeaponRepo.class);


    @BeforeEach
    @Test
    void setUserAdmin(){
        UserDto defaultAdmin = AppContext.get("defaultAdmin");
        backendRemote.setCurrentUser(defaultAdmin.id(), defaultAdmin.username(), defaultAdmin.username());
    }

    @Test
    @SneakyThrows
    @Description("""
        Дано: Пустая бд.
        Действие: 1. Добавить оружие методом POST /implants с валидными данными.
                  2. Добавить персонажа методом POST /character с валидными данными.
                  3. Дать оружие персонажу методом PUT
        Ожидается: Оружие успешно добавлено в бд. ID Оружия в ответе соответствует созданному в бд.
                   Персонаж успешно добавлен в бд. ID Персонажа в ответе соответствует созданному в бд.
                   Оружие успешно добавлено в персонажа. 
        """)
    void putCharacterWeapon(){
        // Создать Оружие
        String weaponName = randomUUID().toString();
        Boolean isMelee = true;
        int penetration = 201;
        int reputationRequirement = 1001;
        backendRemote.createWeapon(
            CreateWeaponRequest.builder()
                .isMelee(isMelee)
                .name(weaponName)
                .weaponType(null)
                .penetration(penetration)
                .reputationRequirement(reputationRequirement)
                .build()
        );
        UserDto defaultAdmin = AppContext.get("defaultAdmin");
        backendRemote.setCurrentUser(defaultAdmin.id(), defaultAdmin.username(), defaultAdmin.username());

        // Проверить новое оружие
        Result<WeaponsRecord> weaponRecord = weaponRepo.getWeaponsByName(weaponName);
        WeaponDto weaponDto = backendRemote.getWeapon(weaponRecord.get(0).getId());

        // Создать персонажа
        String charName = randomUUID().toString();
        backendRemote.createCharacter(
            CreateCharacterRequest.builder()
                .name(charName)
                .height(180)
                .weight(60)
                .age(20)
                .organization("test")
                .characterClass("test")
                .reputation(0)
                .build()
        );

        // Проверить нового персонажа
        Result<CharactersRecord> charResult = dbContext.select().from(CHARACTERS)
            .where(CHARACTERS.NAME.eq(charName))
            .fetchInto(CHARACTERS);
    }

}
