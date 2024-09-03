package ru.nightcityroleplay.backend.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.nightcityroleplay.backend.entity.Skill;

import java.util.UUID;

public interface SkillRepository extends JpaRepository <Skill, UUID> {

}
