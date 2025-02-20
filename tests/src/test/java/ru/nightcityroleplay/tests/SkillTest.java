package ru.nightcityroleplay.tests;

import io.qameta.allure.Description;
import lombok.SneakyThrows;
import org.jooq.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.nightcityroleplay.tests.component.AppContext;
import ru.nightcityroleplay.tests.component.BackendRemoteComponent;
import ru.nightcityroleplay.tests.dto.CreateSkillRequest;
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
        UserDto defaultAdmin = AppContext.get("defaultAdmin");
        backendRemote.setCurrentUser(defaultAdmin.id(), defaultAdmin.username(), defaultAdmin.username());
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
        String skillFamily = "Short Blade";
        String skillDescription = "Skill description example";
        String skillClass = "solo";
        boolean typeIsBattle = true;

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
        // Проверить созданный навык
        Result<SkillsRecord> skillResult = skillRepo.getSkillsByName(skillName);

        // Убедиться, что успешно создан один навык
        assertThat(skillResult).hasSize(10);
        assertThat(skillResult.get(0))
            .satisfies(
                skill -> assertThat(skill.getName()).isEqualTo(skillName),
                skill -> assertThat(skill.getSkillFamily()).isEqualTo(skillFamily),
                skill -> assertThat(skill.getDescription()).isEqualTo(skillDescription),
                skill -> assertThat(skill.getSkillClass()).isEqualTo(skillClass),
                skill -> assertThat(skill.getTypeIsBattle()).isEqualTo(typeIsBattle)
            );
    }
}
