package ru.nightcityroleplay.tests;

import jdk.jfr.Description;
import lombok.SneakyThrows;
import org.jooq.DSLContext;
import org.jooq.Result;
import org.junit.jupiter.api.Test;
import ru.nightcityroleplay.tests.component.AppContext;
import ru.nightcityroleplay.tests.component.BackendRemoteComponent;
import ru.nightcityroleplay.tests.dto.*;
import ru.nightcityroleplay.tests.entity.tables.records.ImplantsRecord;
import ru.nightcityroleplay.tests.repo.ImplantRepo;

import java.util.UUID;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static ru.nightcityroleplay.tests.entity.Tables.IMPLANTS;

public class ImplantTest {

    DSLContext dbContext = AppContext.get(DSLContext.class);
    BackendRemoteComponent backendRemote = AppContext.get(BackendRemoteComponent.class);
    ImplantRepo implantRepo = AppContext.get(ImplantRepo.class);

    @Test
    @SneakyThrows
    @Description("""
        Дано: Пустая бд.
        Действие: Добавить оружие методом POST /implants с валидными данными.
        Ожидается: имплант успешно добавлено в бд. ID оружия в ответе соответствует созданному в бд.
        """)
    void createImplant() {
        // Создать имплант
        String implantName = randomUUID().toString();
        String implantType = "Оптика";
        String description = "йцуйцуйцу";
        int reputationRequirement = 100;
        int implantPointsCost = 2;
        int specialImplantPointsCost = 0;

        UserDto defaultAdmin = AppContext.get("defaultAdmin");
        backendRemote.setCurrentUser(defaultAdmin.id(), defaultAdmin.username(), defaultAdmin.username());


        backendRemote.createImplant(
            CreateImplantRequest.builder()
                .name(implantName)
                .implantType(implantType)
                .description(description)
                .reputationRequirement(reputationRequirement)
                .implantPointsCost(implantPointsCost)
                .specialImplantPointsCost(specialImplantPointsCost)
                .build()
        );

        // Проверить новый имплант
        Result<ImplantsRecord> implantResult = dbContext.select().from(IMPLANTS)
            .where(IMPLANTS.NAME.eq(implantName))
            .fetchInto(IMPLANTS);

        assertThat(implantResult).hasSize(1);
        assertThat(implantResult.get(0))
            .satisfies(
                implant -> assertThat(implant.getName()).isEqualTo(implantName),
                implant -> assertThat(implant.getImplantType()).isEqualTo(implantType),
                implant -> assertThat(implant.getDescription()).isEqualTo(description),
                implant -> assertThat(implant.getReputationRequirement()).isEqualTo(reputationRequirement),
                implant -> assertThat(implant.getImplantPointsCost()).isEqualTo(implantPointsCost),
                implant -> assertThat(implant.getSpecialImplantPointsCost()).isEqualTo(specialImplantPointsCost)
            );
    }

    @Test
    @SneakyThrows
    @Description("""
        Дано: Пустая бд.
        Действие: Попытаться добавить имплант методом POST /implants с пустым именем.
        Ожидается: Запрос завершился с ошибкой и сообщение об ошибке о некорректных данных.
        """)
    void createImplantWithBadName() {
        // Создать имплант
        String implantName = "";
        String implantType = "Оптика";
        String description = "йцуйцуйцу";
        int reputationRequirement = 100;
        int implantPointsCost = 2;
        int specialImplantPointsCost = 0;

        UserDto defaultAdmin = AppContext.get("defaultAdmin");
        backendRemote.setCurrentUser(defaultAdmin.id(), defaultAdmin.username(), defaultAdmin.username());


        HttpResponse response = backendRemote.makeCreateImplantRequest(
            CreateImplantRequest.builder()
                .name(implantName)
                .implantType(implantType)
                .description(description)
                .reputationRequirement(reputationRequirement)
                .implantPointsCost(implantPointsCost)
                .specialImplantPointsCost(specialImplantPointsCost)
                .build()
        );

        // Проверить статус кода ответа
        assertThat(response.code()).isEqualTo(400);

        // Проверить тело ответа на наличие ожидаемого сообщения
        String responseBody = response.body();
        assertThat(responseBody).contains("Имя импланта не может быть пустым.");
    }

    @Test
    @SneakyThrows
    @Description("""
        Дано: Пустая бд.
        Действие: Попытаться добавить имплант методом POST /implants с отрицательной ценной.
        Ожидается: Запрос завершился с ошибкой и сообщение об ошибке о некорректных данных.
        """)
    void createImplantWithNegativeImplantPointsCost() {
        // Создать имплант
        String implantName = "Кироши v.2";
        String implantType = "Оптика";
        String description = "йцуйцуйцу";
        int reputationRequirement = 100;
        int implantPointsCost = -2;
        int specialImplantPointsCost = 0;

        UserDto defaultAdmin = AppContext.get("defaultAdmin");
        backendRemote.setCurrentUser(defaultAdmin.id(), defaultAdmin.username(), defaultAdmin.username());


        HttpResponse response = backendRemote.makeCreateImplantRequest(
            CreateImplantRequest.builder()
                .name(implantName)
                .implantType(implantType)
                .description(description)
                .reputationRequirement(reputationRequirement)
                .implantPointsCost(implantPointsCost)
                .specialImplantPointsCost(specialImplantPointsCost)
                .build()
        );

        // Проверить статус кода ответа
        assertThat(response.code()).isEqualTo(400);

        // Проверить тело ответа на наличие ожидаемого сообщения об ошибке
        String responseBody = response.body();
        assertThat(responseBody).contains("Стоимость очков импланта не может быть отрицательной.");
    }

    @Test
    @Description("""
        Дано: Имплант с id.
        Действие: Удалить имплант методом DELETE /implant/{id}.
        Ожидается: Имплант удалён из бд.
        """)
    void deleteImplant() {
        // Создать имплант
        String implantName = randomUUID().toString();
        // Авторизовать пользователя
        UserDto defaultAdmin = AppContext.get("defaultAdmin");
        backendRemote.setCurrentUser(defaultAdmin.id(), defaultAdmin.username(), defaultAdmin.username());
        backendRemote.createImplant(
            CreateImplantRequest.builder()
                .name(implantName)
                .implantType("Optics_Kiroshi")
                .description("Description text")
                .reputationRequirement(100)
                .implantPointsCost(3)
                .specialImplantPointsCost(0)
                .build()
        );

        // Проверить, что имплант создан
        Result<ImplantsRecord> implantResult = implantRepo.getImplantsByName(implantName);
        assertThat(implantResult).hasSize(1);

        // Удалить имплант
        backendRemote.deleteImplant(implantResult.get(0).getId());

        // Проверить, что имплант удалён
        Result<ImplantsRecord> result = implantRepo.getImplantsByName(implantName);
        assertThat(result).isEmpty();
    }

    @Test
    @SneakyThrows
    @Description("""
    Дано: Имплант 1.
    Действие: Удалить Имплант 2 методом DELETE /implants/{id}.
    Ожидается: 404 Not Found.
               Никакой имплант не удалён.
    """)
    void deleteNonExistingImplant() {
        UserDto defaultAdmin = AppContext.get("defaultAdmin");
        backendRemote.setCurrentUser(defaultAdmin.id(), defaultAdmin.username(), defaultAdmin.username());

        // Удалить несуществующий имплант
        UUID implantId = randomUUID();
        HttpResponse response = backendRemote.makeDeleteImplantRequest(implantId);

        // Проверить, что вернулся статус 404 и сообщение об ошибке
        assertThat(response.code()).isEqualTo(404);
        assertThat(response.body().strip()).contains("Имплант не найден");

        // Проверить, что имплант не существует в репозитории
        Result<ImplantsRecord> result = implantRepo.getImplantById(implantId);
        assertThat(result).size().isEqualTo(0);
    }

    @Test
    @Description("""
        Дано: Имплант.
        Действие: Получить имплант методом GET /implants/{id}.
        Ожидается: Получены данные импланта.
        """)
    void getImplant() {
        // Создать имплант
        String implantName = randomUUID().toString();
        String implantType = "Optics";
        String description = "Description text";
        int reputationRequirement = 100;
        int implantPointsCost = 3;
        int specialImplantPointsCost = 0;

        // Авторизовать пользователя
        UserDto defaultAdmin = AppContext.get("defaultAdmin");
        backendRemote.setCurrentUser(defaultAdmin.id(), defaultAdmin.username(), defaultAdmin.username());

        backendRemote.createImplant(
            CreateImplantRequest.builder()
                .name(implantName)
                .implantType(implantType)
                .description(description)
                .reputationRequirement(reputationRequirement)
                .implantPointsCost(implantPointsCost)
                .specialImplantPointsCost(specialImplantPointsCost)
                .build()
        );

        // Проверить новый имплант
        Result<ImplantsRecord> implantRecord = implantRepo.getImplantsByName(implantName);
        ImplantDto implantDto = backendRemote.getImplant(implantRecord.get(0).getId());

        assertThat(implantRecord).hasSize(1);
        assertThat(implantDto.getName()).isEqualTo(implantRecord.get(0).getName());
        assertThat(implantDto.getImplantType()).isEqualTo(implantRecord.get(0).getImplantType());
        assertThat(implantDto.getDescription()).isEqualTo(implantRecord.get(0).getDescription());
        assertThat(implantDto.getReputationRequirement()).isEqualTo(implantRecord.get(0).getReputationRequirement());
        assertThat(implantDto.getImplantPointsCost()).isEqualTo(implantRecord.get(0).getImplantPointsCost());
        assertThat(implantDto.getSpecialImplantPointsCost()).isEqualTo(implantRecord.get(0).getSpecialImplantPointsCost());
    }

    @Test
    @Description("""
    Дано: Имплант с id.
    Действие: Изменить имплант по id методом PUT /implants/{id}.
    Ожидается: Имплант в бд обновлён.
    """)
    void updateImplant() {
        // Создать имплант
        String implantName = randomUUID().toString();
        String implantType = "Optics1";
        String description = "Initial Description";
        int reputationRequirement = 1001;
        int implantPointsCost = 3;
        int specialImplantPointsCost = 0;
        // Авторизовать пользователя
        UserDto defaultAdmin = AppContext.get("defaultAdmin");
        backendRemote.setCurrentUser(defaultAdmin.id(), defaultAdmin.username(), defaultAdmin.username());
        backendRemote.createImplant(
            CreateImplantRequest.builder()
                .name(implantName)
                .implantType(implantType)
                .description(description)
                .reputationRequirement(reputationRequirement)
                .implantPointsCost(implantPointsCost)
                .specialImplantPointsCost(specialImplantPointsCost)
                .build()
        );

        // Проверить созданный имплант
        Result<ImplantsRecord> implantRecord = implantRepo.getImplantsByName(implantName);

        // Изменить имплант
        String updatedImplantName = "UPDATED" + randomUUID();
        String updatedDescription = "Updated Description";
        backendRemote.updateImplant(implantRecord.get(0).getId(), UpdateImplantRequest.builder()
            .name(updatedImplantName)
            .implantType("NeuralLink")
            .description(updatedDescription)
            .reputationRequirement(100)
            .implantPointsCost(3)
            .specialImplantPointsCost(0)
            .build()
        );

        // Получить обновлённый имплант
        ImplantDto updatedImplantDto = backendRemote.getImplant(implantRecord.get(0).getId());

        // Проверить изменения
        assertThat(updatedImplantDto.getName()).isEqualTo(updatedImplantName);
        assertThat(updatedImplantDto.getDescription()).isEqualTo(updatedDescription);
        assertThat(updatedImplantDto.getReputationRequirement()).isEqualTo(100);
        assertThat(updatedImplantDto.getImplantPointsCost()).isEqualTo(3);
        assertThat(updatedImplantDto.getSpecialImplantPointsCost()).isEqualTo(0);
        assertThat(updatedImplantDto.getImplantType()).isEqualTo("NeuralLink");
    }

    @Test
    @Description("""
    Дано: Имплант отсутствует
    Действие: Изменить имплант по id методом PUT /implants/{id}
    Ожидается: Ошибка 404, имплант не найден
    """)
    void updateNonExistingImplant() {
        // Установить текущего пользователя
        UserDto defaultAdmin = AppContext.get("defaultAdmin");
        backendRemote.setCurrentUser(defaultAdmin.id(), defaultAdmin.username(), defaultAdmin.username());

        // Пытаться изменить несуществующий имплант
        HttpResponse response = backendRemote.makeUpdateImplantRequest(
            randomUUID(),
            UpdateImplantRequest.builder()
                .name("Some Name")
                .implantType("NeuralLink")
                .description("Some Description")
                .reputationRequirement(100)
                .implantPointsCost(10)
                .specialImplantPointsCost(5)
                .build()
        );

        // Проверить, что код ответа 404
        assertThat(response.code()).isEqualTo(404);
    }

    @Test
    @Description("""
    Дано: Имплант с id.
    Действие: Изменить Имплант по id методом PUT /implants/{id} с некорректными данными.
    Ожидается: 400 Bad_Request.
               Никакой Имплант не был изменен.
    """)
    void updateImplantWithBadRequest() {
        // Создать имплант
        String implantName = randomUUID().toString();
        String implantType = "NeuralLink";
        String description = "Test";
        int reputationRequirement = 100;
        int implantPointsCost = 5;
        int specialImplantPointsCost = 2;
        UserDto defaultAdmin = AppContext.get("defaultAdmin");
        backendRemote.setCurrentUser(defaultAdmin.id(), defaultAdmin.username(), defaultAdmin.username());

        backendRemote.createImplant(
            CreateImplantRequest.builder()
                .name(implantName)
                .implantType(implantType)
                .description(description)
                .reputationRequirement(reputationRequirement)
                .implantPointsCost(implantPointsCost)
                .specialImplantPointsCost(specialImplantPointsCost)
                .build()
        );

        // Проверить созданный имплант
        Result<ImplantsRecord> implantRecord = implantRepo.getImplantsByName(implantName);

        // Попробовать обновить имплант с некорректными данными
        String updatedImplantName = "UPDATED" + randomUUID();
        HttpResponse response = backendRemote.makeUpdateImplantRequest(
            implantRecord.get(0).getId(),
            UpdateImplantRequest.builder()
                .name(updatedImplantName)
                .implantType("")
                .description("")
                .reputationRequirement(-1)
                .implantPointsCost(-5)
                .specialImplantPointsCost(-10)
                .build()
        );

        // Проверить, что ответ имеет статус 400
        assertThat(response.code()).isEqualTo(400);
    }
}