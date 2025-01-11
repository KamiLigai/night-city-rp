package ru.nightcityroleplay.tests.repo;

import org.jooq.DSLContext;
import org.jooq.Result;
import ru.nightcityroleplay.tests.entity.tables.records.SkillsRecord;

import java.util.UUID;

import static ru.nightcityroleplay.tests.entity.Tables.SKILLS;

public class SkillRepo {
    private final DSLContext dbContext;

    public SkillRepo(DSLContext dbContext) {
        this.dbContext = dbContext;
    }

    public Result<SkillsRecord> getSkillsByName(String name) {
        return dbContext.select().from(SKILLS)
            .where(SKILLS.NAME.eq(name))
            .fetchInto(SKILLS);
    }

    public Result<SkillsRecord> getSkillsById(UUID skillid) {
        return dbContext.select().from(SKILLS)
            .where(SKILLS.ID.eq(skillid))
            .fetchInto(SKILLS);
    }
}
