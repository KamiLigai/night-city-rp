package ru.nightcityroleplay.tests;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import ru.nightcityroleplay.tests.dto.UserDto;
import ru.nightcityroleplay.tests.entity.tables.records.SkillsRecord;
import ru.nightcityroleplay.tests.repo.SkillRepo;

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
}
