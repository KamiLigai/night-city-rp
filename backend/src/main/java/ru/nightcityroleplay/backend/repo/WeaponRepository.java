package ru.nightcityroleplay.backend.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.nightcityroleplay.backend.entity.CharacterEntity;
import ru.nightcityroleplay.backend.entity.Weapon;

import java.util.List;
import java.util.UUID;

public interface WeaponRepository  extends JpaRepository<Weapon, UUID> {
    List<Weapon> findByCharactersContaining(CharacterEntity character);
}
