package ru.nightcityroleplay.tests;

import io.qameta.allure.Description;
import lombok.SneakyThrows;
import org.jooq.Result;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.nightcityroleplay.tests.component.AppContext;
import ru.nightcityroleplay.tests.component.BackendRemoteComponent;
import ru.nightcityroleplay.tests.dto.CreateSkillRequest;
import ru.nightcityroleplay.tests.dto.HttpResponse;
import ru.nightcityroleplay.tests.dto.SkillDto;
import ru.nightcityroleplay.tests.dto.UserDto;
import ru.nightcityroleplay.tests.entity.tables.records.SkillsRecord;
import ru.nightcityroleplay.tests.repo.SkillRepo;

import java.util.UUID;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;

public class SkillTest {
    BackendRemoteComponent backendRemote = AppContext.get(BackendRemoteComponent.class);
    SkillRepo skillRepo = AppContext.get(SkillRepo.class);

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
        Действие: Добавить навык методом POST /skills с валидными данными.
        Ожидается: Навык успешно добавлен в бд. ID навыка в ответе соответствует созданному в бд.
        """)
    void createSkill() {
        // Данные для нового навыка
        String skillName = randomUUID().toString();
        String skillFamily = randomUUID().toString();
        String skillDescription = "Skill description example";
        String skillClass = "solo";
        Boolean typeIsBattle = true;

        // Выполнить создание навыка
        backendRemote.createSkill(
            CreateSkillRequest.builder()
                .skillFamily(skillFamily)
                .name(skillName)
                .description(skillDescription)
                .skillClass(skillClass)
                .typeIsBattle(typeIsBattle)
                .build()
        );
        // Проверить созданные навыки
        Result<SkillsRecord> skillResult = skillRepo.getSkillsBySkillFamily(skillFamily);

        // Убедиться, что созданы 10 навыков
        assertThat(skillResult).hasSize(10);
        for (SkillsRecord skill : skillResult) {
            assertThat(skill.getName()).isEqualTo(skillName);
            assertThat(skill.getSkillFamily()).isEqualTo(skillFamily);
            assertThat(skill.getDescription()).isEqualTo(skillDescription);
            assertThat(skill.getSkillClass()).isEqualTo(skillClass);
            assertThat(skill.getTypeIsBattle()).isEqualTo(typeIsBattle);
        }
    }

    @Test
    @SneakyThrows
    @Description("""
        Дано: Пустая бд.
        Действие: Попытаться добавить навык методом POST /skills с пустым именем.
        Ожидается: Запрос завершился с ошибкой и сообщение об ошибке о некорректных данных.
        """)
    void createSkillWithBadOrSameName() {

        // Данные для нового навыка
        String skillName = "";
        String skillFamily = randomUUID().toString();
        String skillDescription = "Skill description example";
        String skillClass = "solo";
        Boolean typeIsBattle = true;

        // Выполнить создание навыка
        HttpResponse response = backendRemote.makeCreateSkillRequest(
            CreateSkillRequest.builder()
                .skillFamily(skillFamily)
                .name(skillName)
                .description(skillDescription)
                .skillClass(skillClass)
                .typeIsBattle(typeIsBattle)
                .build()
        );

        // Проверить статус кода ответа
        assertThat(response.code()).isEqualTo(400);

        // Проверить тело ответа на наличие ожидаемого сообщения
        String responseBody = response.body();
        assertThat(responseBody).contains("Название навыка не может быть пустым.");
    }

    @Test
    @Description("""
        Дано: Навыки с skillFamily.
        Действие: Удалить Навыки методом DELETE /skills/{skillFamily}.
        Ожидается: Навыки удалены из бд.
        """)
    void deleteSkill() {
        // Создание данных для нового навыка
        String skillName = randomUUID().toString();
        String skillFamily = randomUUID().toString();
        String skillDescription = "Skill description example";
        String skillClass = "solo";
        Boolean typeIsBattle = true;

        backendRemote.createSkill(
            CreateSkillRequest.builder()
                .skillFamily(skillFamily)
                .name(skillName)
                .description(skillDescription)
                .skillClass(skillClass)
                .typeIsBattle(typeIsBattle)
                .build()
        );

        // Проверяем, что навык создан
        Result<SkillsRecord> skillResult = skillRepo.getSkillsBySkillFamily(skillFamily);

        assertThat(skillResult).hasSize(10);
        assertThat(skillResult.get(0))
            .satisfies(
                skill -> assertThat(skill.getSkillFamily()).isEqualTo(skillFamily),
                skill -> assertThat(skill.getName()).isEqualTo(skillName),
                skill -> assertThat(skill.getDescription()).isEqualTo(skillDescription),
                skill -> assertThat(skill.getSkillClass()).isEqualTo(skillClass),
                skill -> assertThat(skill.getTypeIsBattle()).isEqualTo(typeIsBattle)
            );

        // Удаление навыка
        backendRemote.deleteSkill(skillResult.get(0).getSkillFamily());

        // Проверка, что навык удалён
        Result<SkillsRecord> deletedSkillResult = skillRepo.getSkillsBySkillFamily(skillFamily);
        assertThat(deletedSkillResult).isEmpty();
    }

    @Test
    @Description("""
        Дано: Навык с определённым skillFamily.
        Действие: Получить навык методом GET /skills/{skillFamily}.
        Ожидается: Получены данные навыка.
        """)
    void getSkill() {
        // Подготовка данных для нового навыка
        String skillName = UUID.randomUUID().toString();
        String skillFamily = UUID.randomUUID().toString();
        String skillDescription = "Skill description example";
        String skillClass = "solo";
        Boolean typeIsBattle = true;

        backendRemote.createSkill(
            CreateSkillRequest.builder()
                .skillFamily(skillFamily)
                .name(skillName)
                .description(skillDescription)
                .skillClass(skillClass)
                .typeIsBattle(typeIsBattle)
                .build()
        );

        // Получаем навык с использованием метода getSkill
        SkillDto skillDto = backendRemote.getSkill(skillFamily);

        // Проверяем, что полученный навык не null и соответствует ожидаемым значениям
        assertThat(skillDto).isNotNull();
        assertThat(skillDto.getName()).isEqualTo(skillName);
        assertThat(skillDto.getSkillFamily()).isEqualTo(skillFamily);
        assertThat(skillDto.getDescription()).isEqualTo(skillDescription);
        assertThat(skillDto.getSkillClass()).isEqualTo(skillClass);
        assertThat(skillDto.getTypeIsBattle()).isEqualTo(typeIsBattle);
    }

    //todo Исправить Bulk
//  void getSkillBulk_skillExists_success() {
    //      // Создать навык
    //      String skillName = "testName" + randomUUID();
    //      String skillDescription = "testDescription" + randomUUID();
    //      Integer skillLevel = 1;
    //      String skillType = "testType" + randomUUID();
    //      Integer skillCost = 100;

    //      backendRemote.createSkill(
    //          CreateSkillRequest.builder()
    //              .name(skillName)
    //              .description(skillDescription)
    //              .level(skillLevel)
    //              .type(skillType)
    //              .cost(skillCost)
    //              .build()
    //      );

    //      String skillName2 = "testName" + randomUUID();
    //      String skillDescription2 = "testDescription" + randomUUID();
    //      Integer skillLevel2 = 1;
    //      String skillType2 = "testType" + randomUUID();
    //      Integer skillCost2 = 100;

    //      backendRemote.createSkill(
    //          CreateSkillRequest.builder()
    //              .name(skillName2)
    //              .description(skillDescription2)
    //              .level(skillLevel2)
    //              .type(skillType2)
    //              .cost(skillCost2)
    //              .build()
    //      );

    //      // Проверить создание навыка
    //      Result<SkillsRecord> skillsRecords = dbContext.select().from(SKILLS)
    //          .where(SKILLS.NAME.eq(skillName).or(SKILLS.NAME.eq(skillName2)))
    //          .fetchInto(SKILLS);

    //      assertThat(skillsRecords.size()).isEqualTo(2);

    //      // Получить навыки

    //      List<UUID> listIds = skillsRecords.stream()
    //          .map(SkillsRecord::getId)
    //          .toList();

    //      IdsRequest idsRequest = new IdsRequest();
    //      idsRequest.setIds(listIds);

    //      List<SkillDto> skillsBulk = backendRemote.getSkillsBulk(idsRequest);

    //      assertThat(skillsBulk.size()).isEqualTo(2);
    //      assertThat(skillsBulk.get(0).getId()).isEqualTo(listIds.get(0));
    //      assertThat(skillsBulk.get(1).getId()).isEqualTo(listIds.get(1));
    //  }
    //  @Test
    //  void getSkillIds_skillExists_success() {
    //      // Создать навык
    //      String skillName = "testName" + randomUUID();
    //      String skillDescription = "testDescription" + randomUUID();
    //      Integer skillLevel = 1;
    //      String skillType = "testType" + randomUUID();
    //      Integer skillCost = 100;

    //      backendRemote.createSkill(
    //          CreateSkillRequest.builder()
    //              .name(skillName)
    //              .description(skillDescription)
    //              .level(skillLevel)
    //              .type(skillType)
    //              .cost(skillCost)
    //              .build()
    //      );

    //      // Получить все навыки
    //      Result<SkillsRecord> skillsRecords = dbContext.select().from(SKILLS)
    //          .fetchInto(SKILLS);

    //      List<UUID> ids = backendRemote.getSkillIds();

    //      assertThat(skillsRecords.size()).isEqualTo(ids.size());

}

