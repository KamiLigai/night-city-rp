package ru.nightcityroleplay.backend.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.nightcityroleplay.backend.entity.Weapon;

import java.util.UUID;

public interface WeaponRepository extends JpaRepository<Weapon, UUID> {
}
