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
import ru.nightcityroleplay.backend.repo.CharacterRepository;
import ru.nightcityroleplay.backend.repo.SkillRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class SkillService {

    private final SkillRepository skillRepo;
    private final CharacterRepository characterRepo;

    public SkillService(SkillRepository skillRepo, CharacterRepository characterRepo) {
        this.skillRepo = skillRepo;
        this.characterRepo = characterRepo;
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
        log.info("Способность {} была создана", skill.getName());
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
            return null;
        } else {
            return toDto(skillById.get());
        }
    }

    @Transactional
    public void updateSkill(UpdateSkillRequest skillDto, UUID skillId) {
        log.info("Способность {} обновляется", skillDto.getName());
        Skill newSkill = new Skill();
        if (skillRepo.findById(skillId).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Способность не найдена");
        }
        newSkill.setId(skillId);
        newSkill.setName(skillDto.getName());
        newSkill.setDescription(skillDto.getDescription());
        newSkill.setLevel(skillDto.getLevel());
        newSkill.setType(skillDto.getType());
        newSkill.setCost(skillDto.getCost());
        skillRepo.save(newSkill);
        log.info("Способность {} обновлена", skillDto.getName());
    }

    @Transactional
    public void deleteSkill(UUID skillId) {
        Skill skill = skillRepo.findById(skillId).orElse(null);
        if (skill == null) {
            log.info("Скилл {} не найден", skillId);
            return;
        }
        if (skill.getCharsId().isEmpty()) {
            skillRepo.delete(skill);
            log.info("Скилл {} удалён", skillId);
        } else {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Запрещено!"); //422
        }
    }
}
