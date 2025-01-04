package ru.nightcityroleplay.tests;

import io.qameta.allure.Description;
import lombok.SneakyThrows;
import okhttp3.Response;
import org.jooq.Result;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ru.nightcityroleplay.tests.component.AppContext;
import ru.nightcityroleplay.tests.component.BackendRemoteComponent;
import ru.nightcityroleplay.tests.dto.CreateWeaponRequest;
import ru.nightcityroleplay.tests.dto.UpdateWeaponRequest;
import ru.nightcityroleplay.tests.dto.UserDto;
import ru.nightcityroleplay.tests.dto.WeaponDto;
import ru.nightcityroleplay.tests.entity.tables.records.WeaponsRecord;
import ru.nightcityroleplay.tests.repo.WeaponRepo;

import java.util.UUID;
import java.util.stream.Stream;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;

public class WeaponTest {


    BackendRemoteComponent backendRemote = AppContext.get(BackendRemoteComponent.class);
    WeaponRepo weaponRepo = AppContext.get(WeaponRepo.class);

    @Test
    @SneakyThrows
    @Description("""
        Дано: Пустая бд.
        Действие: Добавить оружие методом POST /weapons с валидными данными.
        Ожидается: Оружие успешно добавлено в бд. ID оружия в ответе соответствует созданному в бд.
        """)
    void createWeapon() {
        // Создать оружие
        String weaponName = randomUUID().toString();
        Boolean isMelee = true;
        String weaponType = "Sword";
        int penetration = 20;
        int reputationRequirement = 100;
        backendRemote.createWeapon(
            CreateWeaponRequest.builder()
                .isMelee(isMelee)
                .name(weaponName)
                .weaponType(weaponType)
                .penetration(penetration)
                .reputationRequirement(reputationRequirement)
                .build()
        );
        UserDto defaultAdmin = AppContext.get("defaultAdmin");
        backendRemote.setCurrentUser(defaultAdmin.id(), defaultAdmin.username(), defaultAdmin.username());

        // Проверить новое оружие
        Result<WeaponsRecord> weaponResult = weaponRepo.getWeaponsByName(weaponName);

        assertThat(weaponResult).hasSize(1);
        assertThat(weaponResult.get(0))
            .satisfies(
                weapon -> assertThat(weapon.getIsMelee()).isEqualTo(isMelee),
                weapon -> assertThat(weapon.getName()).isEqualTo(weaponName),
                weapon -> assertThat(weapon.getWeaponType()).isEqualTo(weaponType),
                weapon -> assertThat(weapon.getPenetration()).isEqualTo(penetration),
                weapon -> assertThat(weapon.getReputationRequirement()).isEqualTo(reputationRequirement)
            );
    }


    @Test
    @SneakyThrows
    @Description("""
        Дано: Пустая бд.
        Действие: Попытаться добавить оружие методом POST /weapons с пустым именем.
        Ожидается: Запрос завершился с ошибкой и сообщение об ошибке о некорректных данных.
        """)
    void createWeaponWithBadOrSameName() {
        UserDto defaultAdmin = AppContext.get("defaultAdmin");
        backendRemote.setCurrentUser(defaultAdmin.id(), defaultAdmin.username(), defaultAdmin.username());

        // Создать оружие с пустым именем
        Response response = backendRemote.makeCreateWeaponRequest(
            CreateWeaponRequest.builder()
                .isMelee(true)
                .name("") // Пустое имя
                .weaponType("Axe")
                .penetration(0)
                .reputationRequirement(0)
                .build()
        );

        // Проверить статус кода ответа
        assertThat(response.code()).isEqualTo(400);

        // Проверить тело ответа на наличие ожидаемого сообщения
        String responseBody = response.body().string();
        assertThat(responseBody).contains("Имя оружия не может быть пустым.");
    }

    @ParameterizedTest
    @MethodSource("weaponDataProvider")
    @SneakyThrows
    @Description("""
    Дано: Пустая БД.
    Действие: Попытаться добавить оружие методом POST /weapons с некорректными данными.
    Ожидается: Запрос завершился с ошибкой и сообщение об ошибке о некорректных данных.
    """)
    void createWeaponWithInvalidData(String weaponName, String weaponType, int penetration, int reputationRequirement, String expectedErrorMessage) {
        // Попытаться создать оружие с некорректными данными
        Response response = backendRemote.makeCreateWeaponRequest(
            CreateWeaponRequest.builder()
                .isMelee(true)
                .name(weaponName)
                .weaponType(weaponType)
                .penetration(penetration)
                .reputationRequirement(reputationRequirement)
                .build()
        );

        UserDto defaultAdmin = AppContext.get("defaultAdmin");
        backendRemote.setCurrentUser(defaultAdmin.id(), defaultAdmin.username(), defaultAdmin.username());

        // Проверить статус кода ответа
        assertThat(response.code()).isEqualTo(400);

        // Проверить тело ответа на наличие ожидаемого сообщения об ошибке
        String responseBody = response.body().string();
        assertThat(responseBody).contains(expectedErrorMessage);
    }
    private static Stream<Arguments> weaponDataProvider() {
        return Stream.of(
            Arguments.of("Тамогавк", "Axe", -2, 0, "Пробив не может быть отрицательным."),
            Arguments.of("Танто", "Dagger", 0, -5, "Требование к репутации не может быть отрицательным.")
        );
    }

    @Test
    @Description("""
        Дано: Оружие с id.
        Действие: Удалить оружие методом DELETE /weapon/{id}.
        Ожидается: Оружие удалёно из бд.
        """)
    void deleteWeapon() {
        // Создать оружие
        String weaponName = randomUUID().toString();
        Boolean isMelee = true;
        String weaponType = "Sword1";
        int penetration = 201;
        int reputationRequirement = 1001;
        backendRemote.createWeapon(
            CreateWeaponRequest.builder()
                .isMelee(isMelee)
                .name(weaponName)
                .weaponType(weaponType)
                .penetration(penetration)
                .reputationRequirement(reputationRequirement)
                .build()
        );
        UserDto defaultAdmin = AppContext.get("defaultAdmin");
        backendRemote.setCurrentUser(defaultAdmin.id(), defaultAdmin.username(), defaultAdmin.username());

        // Проверить новое оружие
        Result<WeaponsRecord> weaponResult = weaponRepo.getWeaponsByName(weaponName);

        assertThat(weaponResult).hasSize(1);
        assertThat(weaponResult.get(0))
            .satisfies(
                weapon -> assertThat(weapon.getIsMelee()).isEqualTo(isMelee),
                weapon -> assertThat(weapon.getName()).isEqualTo(weaponName),
                weapon -> assertThat(weapon.getWeaponType()).isEqualTo(weaponType),
                weapon -> assertThat(weapon.getPenetration()).isEqualTo(penetration),
                weapon -> assertThat(weapon.getReputationRequirement()).isEqualTo(reputationRequirement)
            );

        backendRemote.deleteWeapon(weaponResult.get(0).getId());

        Result<WeaponsRecord> result = weaponRepo.getWeaponsByName(weaponName);
        assertThat(result).size().isEqualTo(0);
    }

    @Test
    @Description("""
        Дано: Оружие.
        Действие: Получить персонажа методом GET /weapons/{id}.
        Ожидается: Получены данные Оружия.
        """)
    void getWeapon() {
        // Создать оружие
        String weaponName = randomUUID().toString();
        Boolean isMelee = true;
        String weaponType = "Sword1";
        int penetration = 201;
        int reputationRequirement = 1001;
        backendRemote.createWeapon(
            CreateWeaponRequest.builder()
                .isMelee(isMelee)
                .name(weaponName)
                .weaponType(weaponType)
                .penetration(penetration)
                .reputationRequirement(reputationRequirement)
                .build()
        );
        UserDto defaultAdmin = AppContext.get("defaultAdmin");
        backendRemote.setCurrentUser(defaultAdmin.id(), defaultAdmin.username(), defaultAdmin.username());

        // Проверить новое оружие
        Result<WeaponsRecord> weaponRecord = weaponRepo.getWeaponsByName(weaponName);
        WeaponDto weaponDto = backendRemote.getWeapon(weaponRecord.get(0).getId());

        assertThat(weaponRecord).hasSize(1);
        assertThat(weaponDto.getIsMelee()).isEqualTo(weaponRecord.get(0).getIsMelee());
        assertThat(weaponDto.getName()).isEqualTo(weaponRecord.get(0).getName());
        assertThat(weaponDto.getWeaponType()).isEqualTo(weaponRecord.get(0).getWeaponType());
        assertThat(weaponDto.getPenetration()).isEqualTo(weaponRecord.get(0).getPenetration());
        assertThat(weaponDto.getReputationRequirement()).isEqualTo(weaponRecord.get(0).getReputationRequirement());
    }

    @Test
    @SneakyThrows
    @Description("""
        Дано: Оружие 1.
        Действие: Удалить Оружие 2 методом DELETE /weapons/{id}.
        Ожидается: 404 Not Found.
                   Никакое оружие не удалёно.
        """)
    void deleteNonExistingWeapon() {
        UserDto defaultAdmin = AppContext.get("defaultAdmin");
        backendRemote.setCurrentUser(defaultAdmin.id(), defaultAdmin.username(), defaultAdmin.username());

        // Удалить Оружие
        UUID weaponId = randomUUID();
        Response response = backendRemote.makeDeleteWeaponRequest(weaponId);

        // Проверить удаление Оружия
        Result<WeaponsRecord> result = weaponRepo.getWeaponsById(weaponId);

        assertThat(response.code()).isEqualTo(404);
        assertThat(result).size().isEqualTo(0);
    }

    @Test
    @Description("""
        Дано: Оружие с id.
        Действие: Изменить персонажа по id методом PUT /weapons/{id}.
        Ожидается: Оружие в бд обновлен.
        """)
    void updateWeapon() {
        // Создать оружие
        String weaponName = randomUUID().toString();
        Boolean isMelee = true;
        String weaponType = "Sword1";
        int penetration = 201;
        int reputationRequirement = 1001;
        backendRemote.createWeapon(
            CreateWeaponRequest.builder()
                .isMelee(isMelee)
                .name(weaponName)
                .weaponType(weaponType)
                .penetration(penetration)
                .reputationRequirement(reputationRequirement)
                .build()
        );
        UserDto defaultAdmin = AppContext.get("defaultAdmin");
        backendRemote.setCurrentUser(defaultAdmin.id(), defaultAdmin.username(), defaultAdmin.username());

        // Проверить новое оружие
        Result<WeaponsRecord> weaponRecord = weaponRepo.getWeaponsByName(weaponName);
        // Изменить оружие
        String updatedWeaponName = "UPDATED" + randomUUID();
        backendRemote.updateWeapon(weaponRecord.get(0).getId(), UpdateWeaponRequest.builder()
            .name(updatedWeaponName)
            .isMelee(true)
            .reputationRequirement(100)
            .penetration(5)
            .weaponType("Длинное Режущее")
            .build());

        WeaponDto updatedWeaponDto = backendRemote.getWeapon(weaponRecord.get(0).getId());

        assertThat(updatedWeaponDto.getName()).isEqualTo(updatedWeaponName);
    }

    @Test
    @Description("""
         Дано: Оружие отсутствует
          Действие: Изменить Оружие по id методом PUT /weapons/{id}
          Ожидается: Ошибка 404, оружие не найдено
        """)
    void updateNonExistingWeapon() {
        //Изменить персонажа
        UserDto defaultAdmin = AppContext.get("defaultAdmin");
        backendRemote.setCurrentUser(defaultAdmin.id(), defaultAdmin.username(), defaultAdmin.username());

        Response response = backendRemote.makeUpdateWeaponRequest(
            randomUUID(),
            UpdateWeaponRequest.builder()
                .name("Some Name")
                .isMelee(true)
                .reputationRequirement(100)
                .penetration(5)
                .weaponType("Длинное Режущее")
                .build());

        assertThat(response.code()).isEqualTo(404);
    }

    @Test
    @Description("""
        Дано: Оружие с id.
        Действие: Изменить Оружие по id методом PUT /weapons/{id} с некорректными данными.
        Ожидается: 400 Bad_Request.
                   Никакое Оружие не было изменено.
        """)
    void updateWeaponWithBadRequest() {
        // Создать оружие
        String weaponName = randomUUID().toString();
        Boolean isMelee = true;
        String weaponType = "Sword1";
        int penetration = 201;
        int reputationRequirement = 1001;
        backendRemote.createWeapon(
            CreateWeaponRequest.builder()
                .isMelee(isMelee)
                .name(weaponName)
                .weaponType(weaponType)
                .penetration(penetration)
                .reputationRequirement(reputationRequirement)
                .build()
        );
        UserDto defaultAdmin = AppContext.get("defaultAdmin");
        backendRemote.setCurrentUser(defaultAdmin.id(), defaultAdmin.username(), defaultAdmin.username());

        // Проверить новое оружие
        Result<WeaponsRecord> weaponRecord = weaponRepo.getWeaponsByName(weaponName);

        String updatedWeaponName = "UPDATED" + randomUUID();
        Response response = backendRemote.makeUpdateWeaponRequest(
            weaponRecord.get(0).getId(), UpdateWeaponRequest.builder()
                .name(updatedWeaponName)
                .isMelee(true)
                .reputationRequirement(-1)
                .penetration(-1)
                .weaponType("0")
                .build());

        assertThat(response.code()).isEqualTo(400);
    }



}