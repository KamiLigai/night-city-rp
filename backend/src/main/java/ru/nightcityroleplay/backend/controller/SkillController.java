package ru.nightcityroleplay.backend.controller;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import ru.nightcityroleplay.backend.dto.CreateSkillRequest;
import ru.nightcityroleplay.backend.dto.CreateSkillResponse;
import ru.nightcityroleplay.backend.dto.SkillDto;
import ru.nightcityroleplay.backend.dto.UpdateSkillRequest;
import ru.nightcityroleplay.backend.service.SkillService;

import java.util.UUID;

@RestController
@RequestMapping("skills")
public class SkillController {

    private final SkillService skillService;

    public SkillController(SkillService skillService) {
        this.skillService = skillService;
    }

    @PostMapping
    public CreateSkillResponse createSkill(@RequestBody CreateSkillRequest request) {
        return skillService.createSkill(request);
    }

    @GetMapping
    public Page<SkillDto> getSkills(Pageable pageable) {
        return skillService.getSkillPage(pageable);
    }

    @GetMapping("{skillId}")
    public SkillDto getSkill(@PathVariable UUID skillId) {
        return skillService.getSkill(skillId);
    }

    @PutMapping("{skillId}")
    public void updateSkill(@RequestBody UpdateSkillRequest request, @PathVariable UUID skillId) {
        skillService.updateSkill(request, skillId);
    }

    @DeleteMapping("{skillId}")
    public void deleteSkill(@PathVariable UUID skillId) {
        skillService.deleteSkill(skillId);
    }
}
