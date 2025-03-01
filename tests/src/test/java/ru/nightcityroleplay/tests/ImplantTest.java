package ru.nightcityroleplay.tests;

import io.qameta.allure.Description;
import lombok.SneakyThrows;
import org.jooq.DSLContext;
import org.jooq.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ru.nightcityroleplay.tests.component.AppContext;
import ru.nightcityroleplay.tests.component.BackendRemoteComponent;
import ru.nightcityroleplay.tests.dto.*;
import ru.nightcityroleplay.tests.entity.tables.records.ImplantsRecord;
import ru.nightcityroleplay.tests.repo.ImplantRepo;

import java.util.UUID;
import java.util.stream.Stream;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static ru.nightcityroleplay.tests.entity.Tables.IMPLANTS;

public class ImplantTest {

    DSLContext dbContext = AppContext.get(DSLContext.class);
    BackendRemoteComponent backendRemote = AppContext.get(BackendRemoteComponent.class);
    ImplantRepo implantRepo = AppContext.get(ImplantRepo.class);


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
        Действие: Добавить имплант методом POST /implants с валидными данными.
        Ожидается: имплант успешно добавлено в бд. ID импланта в ответе соответствует созданному в бд.
        """)
    void createImplant_validData_success() {
        // Создать имплант
        String implantName = randomUUID().toString();
        String implantType = "Оптика";
        String description = "йцуйцуйцу";
        int reputationRequirement = 100;
        int implantPointsCost = 2;
        int specialImplantPointsCost = 0;

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
        Действие: Добавить имплант методом POST /implants с валидными данными.
        Ожидается: имплант успешно добавлено в бд. ID импланта в ответе соответствует созданному в бд.
        """)
    void createImplant_byDefaultUser_throw403() {

        UserDto user = AppContext.get("defaultUser");
        AppContext.get(BackendRemoteComponent.class)
            .setCurrentUser(user.id(), user.username(), user.username());

        // Создать имплант
        String implantName = randomUUID().toString();
        String implantType = "Оптика";
        String description = "йцуйцуйцу";
        int reputationRequirement = 100;
        int implantPointsCost = 2;
        int specialImplantPointsCost = 0;

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

        assertThat(response.code()).isEqualTo(403);
    }

    @ParameterizedTest
    @MethodSource("implantDataProvider")
    @SneakyThrows
    @Description("""
        Дано: Пустая БД.
        Действие: Попытаться добавить имплант методом POST /implants с некорректными данными.
        Ожидается: Запрос завершился с ошибкой и сообщение об ошибке о некорректных данных.
        """)
    void createImplant_WithInvalidData_throw400(String implantName, String implantType, int implantPointsCost, int specialImplantPointsCost, int reputationRequirement, String expectedErrorMessage) {
        // Попытаться создать имплант с некорректными данными
        HttpResponse response = backendRemote.makeCreateImplantRequest(
            CreateImplantRequest.builder()
                .name(implantName)
                .implantType(implantType)
                .description("Описание")
                .reputationRequirement(reputationRequirement)
                .implantPointsCost(implantPointsCost)
                .specialImplantPointsCost(specialImplantPointsCost)
                .build()
        );

        // Проверить статус кода ответа
        assertThat(response.code()).isEqualTo(400);

        // Проверить тело ответа на наличие ожидаемого сообщения об ошибке
        String responseBody = response.body();
        assertThat(responseBody).contains(expectedErrorMessage);
    }

    private static Stream<Arguments> implantDataProvider() {
        return Stream.of(
            Arguments.of("", "Оптика", 2, 0, 100, "Имя импланта не может быть пустым."),
            Arguments.of("Кироши v.2", "Оптика", -2, 0, 100, "Стоимость очков импланта не может быть отрицательной.")
        );
    }

    @Test
    @Description("""
        Дано: Имплант с id.
        Действие: Удалить имплант методом DELETE /implant/{id}.
        Ожидается: Имплант удалён из бд.
        """)
    void deleteImplant_redButtonTrue_success() {
        // Создать имплант
        String implantName = randomUUID().toString();
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
        backendRemote.deleteImplant(implantResult.get(0).getId(), true);

        // Проверить, что имплант удалён
        Result<ImplantsRecord> result = implantRepo.getImplantsByName(implantName);
        assertThat(result).isEmpty();
    }

    @Test
    @Description("""
        Дано: Имплант с id.
        Действие: Удалить имплант методом DELETE /implant/{id}.
        Ожидается: Имплант удалён из бд.
        """)
    void deleteImplant_redButtonFalse_success() {
        // Создать имплант
        String implantName = randomUUID().toString();
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
        backendRemote.deleteImplant(implantResult.get(0).getId(), false);

        // Проверить, что имплант удалён
        Result<ImplantsRecord> result = implantRepo.getImplantsByName(implantName);
        assertThat(result).isEmpty();
    }

    @Test
    @Description("""
        Дано: Имплант с id.
        Действие: Удалить имплант методом DELETE /implant/{id} руками Юзера.
        Ожидается: 403.
        """)
    void deleteImplantByDefaultUser_byDefaultUser_throw403() {
        // Создать имплант
        String implantName = randomUUID().toString();
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

        UserDto user = AppContext.get("defaultUser");
        backendRemote.setCurrentUser(user.id(), user.username(), user.username());

        // Удалить имплант
        HttpResponse response = backendRemote.makeDeleteImplantRequest(implantResult.get(0).getId(), true);

        // Проверить, что имплант не удалён и вышло 403
        assertThat(response.code()).isEqualTo(403);
    }

    @Test
    @SneakyThrows
    @Description("""
        Дано: Имплант 1.
        Действие: Удалить Имплант 2 методом DELETE /implants/{id}.
        Ожидается: 404 Not Found.
               Никакой имплант не удалён.
    """)
    void deleteNonExistingImplant_whenImplantDoesNotExist_throw404() {
        // Удалить несуществующий имплант
        UUID implantId = randomUUID();
        HttpResponse response = backendRemote.makeDeleteImplantRequest(implantId, true);

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
    void getImplant_whenDataIsValid_success() {
        // Создать имплант
        String implantName = randomUUID().toString();
        String implantType = "Optics";
        String description = "Description text";
        int reputationRequirement = 100;
        int implantPointsCost = 3;
        int specialImplantPointsCost = 0;

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
        assertThat(implantDto.name()).isEqualTo(implantRecord.get(0).getName());
        assertThat(implantDto.implantType()).isEqualTo(implantRecord.get(0).getImplantType());
        assertThat(implantDto.description()).isEqualTo(implantRecord.get(0).getDescription());
        assertThat(implantDto.reputationRequirement()).isEqualTo(implantRecord.get(0).getReputationRequirement());
        assertThat(implantDto.implantPointsCost()).isEqualTo(implantRecord.get(0).getImplantPointsCost());
        assertThat(implantDto.specialImplantPointsCost()).isEqualTo(implantRecord.get(0).getSpecialImplantPointsCost());
    }

    @Test
    @Description("""
        Дано: Имплант.
        Действие: Получить число методом GET /implants/{id}/assigned-chars.
        Ожидается: Получено число 0.
        """)
    void getImplantStatus_whenDataIsValid_success() {
        // Создать имплант
        String implantName = randomUUID().toString();
        String implantType = "Optics";
        String description = "TestStatus";
        int reputationRequirement = 100;
        int implantPointsCost = 3;
        int specialImplantPointsCost = 0;

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

        // Проверить статус импланта
        Integer implantStatus = backendRemote.getImplantStatus(implantRecord.get(0).getId());

        assertThat(implantRecord).hasSize(1);
        assertThat(implantStatus).isEqualTo(0);
    }

    @Test
    @Description("""
        Дано: Имплант с id.
        Действие: Изменить имплант по id методом PUT /implants/{id}.
        Ожидается: Имплант в бд обновлён.
        """)
    void updateImplant_whenDataIsValid_success() {
        // Создать имплант
        String implantName = randomUUID().toString();
        String implantType = "Optics1";
        String description = "Initial Description";
        int reputationRequirement = 1001;
        int implantPointsCost = 3;
        int specialImplantPointsCost = 0;
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
        assertThat(updatedImplantDto.name()).isEqualTo(updatedImplantName);
        assertThat(updatedImplantDto.description()).isEqualTo(updatedDescription);
        assertThat(updatedImplantDto.reputationRequirement()).isEqualTo(100);
        assertThat(updatedImplantDto.implantPointsCost()).isEqualTo(3);
        assertThat(updatedImplantDto.specialImplantPointsCost()).isEqualTo(0);
        assertThat(updatedImplantDto.implantType()).isEqualTo("NeuralLink");
    }

    @Test
    @Description("""
        Дано: Имплант отсутствует
        Действие: Изменить имплант по id методом PUT /implants/{id}
        Ожидается: Ошибка 404, имплант не найден
        """)
    void updateImplant_whenNonExistingImplant_throw404() {
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
    void updateImplant_withBadRequest_throw400() {
        // Создать имплант
        String implantName = randomUUID().toString();
        String implantType = "NeuralLink";
        String description = "Test";
        int reputationRequirement = 100;
        int implantPointsCost = 5;
        int specialImplantPointsCost = 2;

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
        assertThat(response.code()).isEqualTo(400);
    }
}