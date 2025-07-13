package ru.nightcityroleplay.backend.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.nightcityroleplay.backend.entity.Skill;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SkillRepository extends JpaRepository<Skill, UUID> {

    Optional<Skill> findBySkillFamilyAndLevel(String skillFamily, int level);

    List<Skill> findBySkillFamily(String skillFamily);

    List<Skill> findBySkillFamilyId(UUID skillFamilyId);

    @Query("select s.id from Skill s")
    List<UUID> findAllSkillIds();

    List<Skill> findAllByIdIn(List<UUID> ids);

}
