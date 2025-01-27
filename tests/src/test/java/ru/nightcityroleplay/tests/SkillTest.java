package ru.nightcityroleplay.tests;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jooq.DSLContext;
import org.jooq.Result;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.nightcityroleplay.tests.component.AppContext;
import ru.nightcityroleplay.tests.component.BackendRemoteComponent;
import ru.nightcityroleplay.tests.dto.CreateSkillRequest;
import ru.nightcityroleplay.tests.dto.IdsRequest;
import ru.nightcityroleplay.tests.dto.SkillDto;
import ru.nightcityroleplay.tests.dto.UserDto;
import ru.nightcityroleplay.tests.entity.tables.records.SkillsRecord;

import java.util.List;
import java.util.UUID;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static ru.nightcityroleplay.tests.entity.Tables.SKILLS;

public class SkillTest {

    DSLContext dbContext = AppContext.get(DSLContext.class);
    BackendRemoteComponent backendRemote = AppContext.get(BackendRemoteComponent.class);
    ObjectMapper objectMapper = AppContext.get(ObjectMapper.class);

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
    void getSkillBulk_skillExists_success() {
        // Создать навык
        String skillName = "testName" + randomUUID();
        String skillDescription = "testDescription" + randomUUID();
        Integer skillLevel = 1;
        String skillType = "testType" + randomUUID();
        Integer skillCost = 100;

        backendRemote.createSkill(
            CreateSkillRequest.builder()
                .name(skillName)
                .description(skillDescription)
                .level(skillLevel)
                .type(skillType)
                .cost(skillCost)
                .build()
        );

        String skillName2 = "testName" + randomUUID();
        String skillDescription2 = "testDescription" + randomUUID();
        Integer skillLevel2 = 1;
        String skillType2 = "testType" + randomUUID();
        Integer skillCost2 = 100;

        backendRemote.createSkill(
            CreateSkillRequest.builder()
                .name(skillName2)
                .description(skillDescription2)
                .level(skillLevel2)
                .type(skillType2)
                .cost(skillCost2)
                .build()
        );

        // Проверить создание навыка
        Result<SkillsRecord> skillsRecords = dbContext.select().from(SKILLS)
            .where(SKILLS.NAME.eq(skillName).or(SKILLS.NAME.eq(skillName2)))
            .fetchInto(SKILLS);

        assertThat(skillsRecords.size()).isEqualTo(2);

        // Получить навыки

        List<UUID> listIds = skillsRecords.stream()
            .map(SkillsRecord::getId)
            .toList();

        IdsRequest idsRequest = new IdsRequest();
        idsRequest.setIds(listIds);

        List<SkillDto> skillsBulk = backendRemote.getSkillsBulk(idsRequest);

        assertThat(skillsBulk.size()).isEqualTo(2);
        assertThat(skillsBulk.get(0).getId()).isEqualTo(listIds.get(0));
        assertThat(skillsBulk.get(1).getId()).isEqualTo(listIds.get(1));
    }
}
