package ru.nightcityroleplay.backend.controller;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import ru.nightcityroleplay.backend.dto.IdsRequest;
import ru.nightcityroleplay.backend.dto.skills.CreateSkillRequest;
import ru.nightcityroleplay.backend.dto.skills.CreateSkillResponse;
import ru.nightcityroleplay.backend.dto.skills.SkillDto;
import ru.nightcityroleplay.backend.dto.skills.UpdateSkillRequest;
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

    @PostMapping()
    public List<CreateSkillResponse> createSkill(@RequestBody CreateSkillRequest request) {
        return skillService.createSkillFamily(request);
    }

    // todo: Skill Family Id
    @PutMapping("{oldName}")
    public String updateSkillsBySkillFamily(
        @RequestBody UpdateSkillRequest updateRequest,
        @PathVariable String oldName
    ) {
        skillService.updateSkill(updateRequest, oldName);
        return "Навыки успешно обновлены";
    }

    @DeleteMapping("/{skillFamily}")
    public void deleteSkillsByName(@PathVariable String skillFamily) {
        skillService.deleteSkillsBySkillFamily(List.of(skillFamily));
    }

    @GetMapping
    public Page<SkillDto> getSkillPage(Pageable pageable) {
        return skillService.getSkillPage(pageable);
    }

    @GetMapping("/unique")
    public Page<SkillDto> getUniqueSkills(Pageable pageable) {
        return skillService.getUniqueSkillPage(pageable);
    }

    @GetMapping("{skillFamily}")
    public SkillDto getSkill(@PathVariable String skillFamily) {
        return skillService.getSkill(skillFamily);
    }

    @GetMapping("ids")
    public List<UUID> getSkillIds() {
        return skillService.getSkillIds();
    }

    @PostMapping("get-bulk")
    public List<SkillDto> getSkillsBulk(@RequestBody IdsRequest request) {
        return skillService.getSkillsBulk(request);
    }
}
