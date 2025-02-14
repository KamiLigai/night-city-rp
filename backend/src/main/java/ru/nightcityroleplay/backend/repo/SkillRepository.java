package ru.nightcityroleplay.backend.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.nightcityroleplay.backend.entity.Skill;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SkillRepository extends JpaRepository<Skill, UUID> {

    List<Skill> findAllByIdIn(List<UUID> ids);

    List<Skill> findByName(String name);

    Optional<Skill> findBySkillFamilyAndLevel(String skillFamily, int level);
}
