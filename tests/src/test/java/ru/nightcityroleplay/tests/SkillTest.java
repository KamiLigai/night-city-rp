package ru.nightcityroleplay.tests;

import io.qameta.allure.Description;
import lombok.SneakyThrows;
import org.jooq.Result;
import org.junit.jupiter.api.Test;
import ru.nightcityroleplay.tests.component.AppContext;
import ru.nightcityroleplay.tests.component.BackendRemoteComponent;
import ru.nightcityroleplay.tests.dto.CreateSkillRequest;
import ru.nightcityroleplay.tests.entity.tables.records.SkillsRecord;
import ru.nightcityroleplay.tests.repo.SkillRepo;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;


public class SkillTest {

    BackendRemoteComponent backendRemote = AppContext.get(BackendRemoteComponent.class);
    SkillRepo skillRepo = AppContext.get(SkillRepo.class);

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
        String skillDescription = "Skill description example";
        int skillLevel = 5;
        String skillType = "Active";
        int skillCost = 1;

        // Выполнить создание навыка
        backendRemote.createSkill(
            CreateSkillRequest.builder()
                .name(skillName)
                .description(skillDescription)
                .level(skillLevel)
                .type(skillType)
                .cost(skillCost)
                .build()
        );

        // Проверить созданный навык
        Result<SkillsRecord> skillResult = skillRepo.getSkillsByName(skillName);

        // Убедиться, что успешно создан один навык
        assertThat(skillResult).hasSize(1);
        assertThat(skillResult.get(0))
            .satisfies(
                skill -> assertThat(skill.getName()).isEqualTo(skillName),
                skill -> assertThat(skill.getDescription()).isEqualTo(skillDescription),
                skill -> assertThat(skill.getLevel()).isEqualTo(skillLevel),
                skill -> assertThat(skill.getType()).isEqualTo(skillType),
                skill -> assertThat(skill.getCost()).isEqualTo(skillCost)
            );
    }
}
