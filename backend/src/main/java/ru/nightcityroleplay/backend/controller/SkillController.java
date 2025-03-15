package ru.nightcityroleplay.backend.controller;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import ru.nightcityroleplay.backend.dto.CreateSkillRequest;
import ru.nightcityroleplay.backend.dto.CreateSkillResponse;
import ru.nightcityroleplay.backend.dto.SkillDto;
import ru.nightcityroleplay.backend.dto.UpdateSkillRequest;
import ru.nightcityroleplay.backend.service.SkillService;

import java.util.List;


@RestController
@RequestMapping("skills")
public class SkillController {

    private final SkillService skillService;

    public SkillController(SkillService skillService) {
        this.skillService = skillService;
    }

    // область приколов
    @PostMapping()
    public List<CreateSkillResponse> createSkill(@RequestBody CreateSkillRequest request) {
        return skillService.createSkill(request);
    }

    @PutMapping("{oldName}")
    public String updateSkillsByName(@RequestBody UpdateSkillRequest updateRequest, @PathVariable String oldName) {
        skillService.updateSkill(updateRequest, oldName);
        return "Навыки успешно обновлены";
    }

    @DeleteMapping("/{skillFamily}")
    public void deleteSkillsByName(@PathVariable String skillFamily) {
        skillService.deleteSkillsBySkillFamily(List.of(skillFamily));
    }

    // Шутки кончились
    @GetMapping
    public Page<SkillDto> getSkills(Pageable pageable) {
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
}
