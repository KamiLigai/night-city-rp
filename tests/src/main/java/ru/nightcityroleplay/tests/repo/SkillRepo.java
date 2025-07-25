package ru.nightcityroleplay.tests.repo;

import org.jooq.DSLContext;
import org.jooq.Result;
import ru.nightcityroleplay.tests.entity.tables.records.SkillsRecord;

import static ru.nightcityroleplay.tests.entity.Tables.SKILLS;

public class SkillRepo {
    private final DSLContext dbContext;

    public SkillRepo(DSLContext dbContext) {
        this.dbContext = dbContext;
    }

    public Result<SkillsRecord> getSkillsBySkillFamily(String skillFamily) {
        return dbContext.select().from(SKILLS)
            .where(SKILLS.SKILL_FAMILY.eq(skillFamily))
            .fetchInto(SKILLS);
    }
}
