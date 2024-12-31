package ru.nightcityroleplay.backend.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.nightcityroleplay.backend.entity.Weapon;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface WeaponRepository extends JpaRepository<Weapon, UUID> {

    @Query("select w.id from Weapon w")
    List<UUID> findAllWeaponIds();

    List<Weapon> findAllByIdIn(Collection<UUID> ids);
}
