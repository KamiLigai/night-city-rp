package ru.nightcityroleplay.backend.controller;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
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

    @PostMapping("/CreateFamily")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public List<CreateSkillResponse> createSkillFamily(@RequestBody CreateSkillRequest request) {
        return skillService.createSkillFamily(request);
    }

    @PutMapping("{oldSkillFamilyId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String updateSkillsBySkillFamilyId(
        @RequestBody UpdateSkillRequest updateRequest,
        @PathVariable UUID oldSkillFamilyId
    ) {
        skillService.updateSkill(updateRequest, oldSkillFamilyId);
        return "Навыки успешно обновлены";
    }

    @DeleteMapping("/{skillFamilyId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public void deleteSkillsBySkillFamilyId(@PathVariable UUID skillFamilyId) {
        skillService.deleteSkillsBySkillFamilyId(List.of(skillFamilyId));
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

