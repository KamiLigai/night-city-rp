package ru.nightcityroleplay.tests.repo;

import org.jooq.DSLContext;
import org.jooq.Result;
import ru.nightcityroleplay.tests.entity.tables.records.ImplantsRecord;

import java.util.UUID;

import static ru.nightcityroleplay.tests.entity.Tables.IMPLANTS;
public class ImplantRepo {
    private final DSLContext dbContext;

    public ImplantRepo(DSLContext dbContext) {
        this.dbContext = dbContext;
    }

    public Result<ImplantsRecord> getImplantsByName(String name) {
        return dbContext.select().from(IMPLANTS)
            .where(IMPLANTS.NAME.eq(name))
            .fetchInto(IMPLANTS);
    }

    public Result<ImplantsRecord> getImplantById(UUID implantId) {
        return dbContext.select().from(IMPLANTS)
            .where(IMPLANTS.ID.eq(implantId))
            .fetchInto(IMPLANTS);
    }
}
