package ru.nightcityroleplay.tests.repo;

import org.jooq.DSLContext;
import org.jooq.Result;
import ru.nightcityroleplay.tests.entity.tables.records.WeaponsRecord;

import java.util.UUID;

import static ru.nightcityroleplay.tests.entity.Tables.WEAPONS;

public class WeaponRepo {
    private final DSLContext dbContext;

    public WeaponRepo(DSLContext dbContext) {
        this.dbContext = dbContext;
    }

    public Result<WeaponsRecord> getWeaponsByName(String name) {
        return dbContext.select().from(WEAPONS)
            .where(WEAPONS.NAME.eq(name))
            .fetchInto(WEAPONS);
    }

    public Result<WeaponsRecord> getWeaponsById(UUID weaponId) {
        return dbContext.select().from(WEAPONS)
            .where(WEAPONS.ID.eq(weaponId))
            .fetchInto(WEAPONS);
    }
}
