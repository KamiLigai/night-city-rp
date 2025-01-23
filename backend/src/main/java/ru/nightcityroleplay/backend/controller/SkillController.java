package ru.nightcityroleplay.backend.controller;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import ru.nightcityroleplay.backend.dto.*;
import ru.nightcityroleplay.backend.service.SkillService;

import java.util.List;
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
    public Page<SkillDto> getSkillPage(Pageable pageable) {
        return skillService.getSkillPage(pageable);
    }

    @GetMapping("{skillId}")
    public SkillDto getSkill(@PathVariable UUID skillId) {
        return skillService.getSkill(skillId);
    }

    @GetMapping("ids")
    public List<UUID> getSkillIds() {
        return skillService.getSkillIds();
    }

    @PostMapping("get-bulk")
    public List<SkillDto> getSkillsBulk(@RequestBody IdsRequest request) {
        return skillService.getSkillsBulk(request);
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
