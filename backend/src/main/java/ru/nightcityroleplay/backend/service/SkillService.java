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
        skillDto.setName(skill.getName());
        skillDto.setDescription(skill.getDescription());
        skillDto.setLevel(skill.getLevel());
        skillDto.setType(skill.getType());
        skillDto.setCost(skill.getCost());
        return skillDto;
    }

    @Transactional
    public CreateSkillResponse createSkill(CreateSkillRequest request) {
        Skill skill = new Skill();
        skill.setId(UUID.randomUUID());
        skill.setName(request.getName());
        skill.setDescription(request.getDescription());
        skill.setLevel(request.getLevel());
        skill.setType(request.getType());
        skill.setCost(request.getCost());
        skill = skillRepo.save(skill);
        log.info("Навык {} был создан", skill.getId());
        return new CreateSkillResponse(skill.getId());
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
    public SkillDto getSkill(UUID skillId) {
        Optional<Skill> skillById = skillRepo.findById(skillId);
        if (skillById.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Навык " + skillId + " не найден");
        }
        return toDto(skillById.get());
    }

    @Transactional
    public void updateSkill(UpdateSkillRequest skillDto, UUID skillId) {
        log.info("Навык {} обновляется", skillDto.getName());
        Optional<Skill> oldSkill = skillRepo.findById(skillId);
        Skill newSkill = new Skill();
        if (oldSkill.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Навык " + skillId + " не найден");
        }
        newSkill.setId(skillId);
        newSkill.setName(skillDto.getName());
        newSkill.setDescription(skillDto.getDescription());
        newSkill.setLevel(oldSkill.get().getLevel());
        newSkill.setType(skillDto.getType());
        newSkill.setCost(oldSkill.get().getCost());
        skillRepo.save(newSkill);
        log.info("Навык {} обновлен", skillDto.getName());
    }

    @Transactional
    public void deleteSkill(UUID skillId) {
        Skill skill = skillRepo.findById(skillId).orElse(null);
        if (skill == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Навык " + skillId + " не найден");
        }
        if (not(skill.getCharacters().isEmpty())) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Этот навык есть как минимум у одного персонажа!");
        }
        skillRepo.delete(skill);
        log.info("Навык {} удалён", skillId);
    }
}
