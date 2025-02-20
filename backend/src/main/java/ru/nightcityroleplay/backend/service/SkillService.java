package ru.nightcityroleplay.backend.service;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.nightcityroleplay.backend.dto.CreateSkillRequest;
import ru.nightcityroleplay.backend.dto.CreateSkillResponse;
import ru.nightcityroleplay.backend.dto.SkillDto;
import ru.nightcityroleplay.backend.dto.UpdateSkillRequest;
import ru.nightcityroleplay.backend.entity.Skill;
import ru.nightcityroleplay.backend.repo.SkillRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;
import static ru.nightcityroleplay.backend.util.BooleanUtils.not;

@Service
@Slf4j
public class SkillService {

    private final SkillRepository skillRepo;

    public SkillService(SkillRepository skillRepo) {
        this.skillRepo = skillRepo;
    }

    private SkillDto toDto(Skill skill) {
        SkillDto skillDto = new SkillDto();
        skillDto.setId(skill.getId());
        skillDto.setSkillFamily(skill.getSkillFamily());
        skillDto.setName(skill.getName());
        skillDto.setDescription(skill.getDescription());
        skillDto.setSkillClass(skill.getSkillClass());
        skillDto.setTypeIsBattle(skill.getTypeIsBattle());
        skillDto.setLevel(skill.getLevel());
        skillDto.setBattleCost(skill.getBattleCost());
        skillDto.setCivilCost(skill.getCivilCost());
        skillDto.setReputationRequirement(skill.getReputationRequirement());
        return skillDto;
    }

    /// Область приколов

    @Transactional
    public List<CreateSkillResponse> createSkill(CreateSkillRequest baseRequest) {
        List<CreateSkillResponse> responses = new ArrayList<>();

        for (int level = 1; level <= 10; level++) {
            Skill skill = buildSkill(baseRequest, level);
            skill = skillRepo.save(skill);
            log.info("Навык {} уровня {} был создан", skill.getId(), level);
            responses.add(new CreateSkillResponse(skill.getId()));
        }
        return responses;
    }

    Skill buildSkill(CreateSkillRequest request, int level) {
        Skill skill = new Skill();
        skill.setId(UUID.randomUUID());
        skill.setSkillFamily(request.getSkillFamily());
        skill.setName(request.getName());
        skill.setDescription(request.getDescription());
        skill.setSkillClass(request.getSkillClass());
        skill.setLevel(level);
        skill.setTypeIsBattle(request.getTypeIsBattle());

        if (level < 1 || level > 10) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Уровень навыка должен быть от 1 до 10");
        }

        if (request.getTypeIsBattle()) {
            setBattleCostAndReputationRequirement(skill, level);
            skill.setCivilCost(0);
        } else {
            setCivilCostAndReputationRequirement(skill, level);
            skill.setBattleCost(0);
        }

        return skill;
    }

    private void setBattleCostAndReputationRequirement(Skill skill, int level) {
        if (level < 6) {
            skill.setBattleCost(level);
            skill.setReputationRequirement(0);
        } else {
            if (level == 6) {
                skill.setBattleCost(7);
                skill.setReputationRequirement(70);
            } else if (level == 7) {
                skill.setBattleCost(9);
                skill.setReputationRequirement(100);
            } else if (level == 8) {
                skill.setBattleCost(12);
                skill.setReputationRequirement(130);
            } else if (level == 9) {
                skill.setBattleCost(16);
                skill.setReputationRequirement(160);
            } else if (level == 10) {
                skill.setBattleCost(21);
                skill.setReputationRequirement(200);
            }
        }
    }

    private void setCivilCostAndReputationRequirement(Skill skill, int level) {
        if (level < 6) {
            skill.setCivilCost(level);
            skill.setReputationRequirement(0);
        } else {
            if (level == 6) {
                skill.setCivilCost(7);
                skill.setReputationRequirement(70);
            } else if (level == 7) {
                skill.setCivilCost(9);
                skill.setReputationRequirement(100);
            } else if (level == 8) {
                skill.setCivilCost(12);
                skill.setReputationRequirement(130);
            } else if (level == 9) {
                skill.setCivilCost(16);
                skill.setReputationRequirement(160);
            } else if (level == 10) {
                skill.setCivilCost(21);
                skill.setReputationRequirement(200);
            }
        }
    }

    @Transactional
    public void updateSkillTimes10ByName(UpdateSkillRequest updateRequest, String oldName) {
        log.info("Навыки с названием {} обновляются на новое название {}", oldName, updateRequest.getName());

        List<Skill> skills = skillRepo.findByName(oldName);

        if (skills.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Навыки с названием " + oldName + " не найдены");
        }

        for (Skill oldSkill : skills) {
            oldSkill.setName(updateRequest.getName());
            oldSkill.setDescription(updateRequest.getDescription());
            oldSkill.setTypeIsBattle(updateRequest.getTypeIsBattle());
            skillRepo.save(oldSkill);
            log.info("Навык с id {} обновлён", oldSkill.getId());
        }
    }

    @Transactional
    public void deleteSkillsByNames(List<String> skillNames) {
        for (String skillName : skillNames) {
            List<Skill> skills = skillRepo.findByName(skillName);
            if (skills == null || skills.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Навык " + skillName + " не найден");
            }
            for (Skill skill : skills) {
                if (!skill.getCharacters().isEmpty()) {
                    throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                        "Этот навык есть как минимум у одного персонажа!");
                }
                skillRepo.delete(skill);
                log.info("Навык {} удалён", skillName);
            }
        }
    }

    @Transactional
    public Page<SkillDto> getSkillPage(Pageable pageable) {
        Page<Skill> skillPage = skillRepo.findAll(pageable);
        List<Skill> skills = skillPage.toList();
        List<SkillDto> skillDtos = new ArrayList<>();
        for (Skill skill : skills) {
            skillDtos.add(toDto(skill));
        }
        return new PageImpl<>(skillDtos, pageable, skillPage.getTotalElements());
    }

    @Transactional
    public Page<SkillDto> getUniqueSkillPage(Pageable pageable) {
        Page<Skill> skillPage = skillRepo.findAll(pageable);
        List<Skill> skills = skillPage.toList();
        List<Skill> uniqueSkills = new ArrayList<>();
        List<SkillDto> skillDtos = new ArrayList<>();

        for (Skill skill : skills) {
            boolean isUnique = true;
            for (Skill uniqueSkill : uniqueSkills) {
                if (isSameSkill(uniqueSkill, skill) && uniqueSkill.getLevel() <= skill.getLevel()) {
                    isUnique = false;
                    break;
                }
            }
            if (isUnique) {
                uniqueSkills.add(skill);
            }
        }

        for (Skill uniqueSkill : uniqueSkills) {
            skillDtos.add(toDto(uniqueSkill));
        }

        return new PageImpl<>(skillDtos, pageable, skillPage.getTotalElements());
    }

    private boolean isSameSkill(Skill skill1, Skill skill2) {
        return skill1.getSkillFamily().equals(skill2.getSkillFamily()) && skill1.getName().equals(skill2.getName());
    }

    @Transactional
    public SkillDto getSkill(UUID skillId) {
        Optional<Skill> skillById = skillRepo.findById(skillId);
        if (skillById.isEmpty()) {
            throw new ResponseStatusException(NOT_FOUND, "Навык " + skillId + " не найден");
        }
        return toDto(skillById.get());
    }

    @Transactional
    public void updateSkill(UpdateSkillRequest skillDto, UUID skillId) {
        log.info("Навык {} обновляется", skillDto.getName());
        Optional<Skill> oldSkill = skillRepo.findById(skillId);
        Skill newSkill = new Skill();
        if (oldSkill.isEmpty()) {
            throw new ResponseStatusException(NOT_FOUND, "Навык " + skillId + " не найден");
        }
        newSkill.setId(skillId);
        newSkill.setName(skillDto.getName());
        newSkill.setDescription(skillDto.getDescription());
        newSkill.setTypeIsBattle(skillDto.getTypeIsBattle());
        newSkill.setLevel(oldSkill.get().getLevel());
        newSkill.setBattleCost(oldSkill.get().getBattleCost());
        newSkill.setCivilCost(oldSkill.get().getCivilCost());
        newSkill.setReputationRequirement(oldSkill.get().getReputationRequirement());
        skillRepo.save(newSkill);
        log.info("Навык {} обновлен", skillDto.getName());
    }

    @Transactional
    public void deleteSkill(UUID skillId) {
        Skill skill = skillRepo.findById(skillId).orElse(null);
        if (skill == null) {
            throw new ResponseStatusException(NOT_FOUND, "Навык " + skillId + " не найден");
        }
        if (not(skill.getCharacters().isEmpty())) {
            throw new ResponseStatusException(UNPROCESSABLE_ENTITY, "Этот навык есть как минимум у одного персонажа!");
        }
        skillRepo.delete(skill);
        log.info("Навык {} удалён", skillId);
    }
}
