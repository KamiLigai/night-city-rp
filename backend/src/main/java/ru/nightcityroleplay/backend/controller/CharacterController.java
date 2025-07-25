package ru.nightcityroleplay.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import ru.nightcityroleplay.backend.dto.GiveReputationRequest;
import ru.nightcityroleplay.backend.dto.character.*;
import ru.nightcityroleplay.backend.dto.implants.ImplantDto;
import ru.nightcityroleplay.backend.service.CharacterService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("characters")
public class CharacterController {
    private final CharacterService characterService;

    public CharacterController(CharacterService characterService) {
        this.characterService = characterService;
    }

    @PostMapping
    public CreateCharacterResponse createCharacter(@RequestBody CreateCharacterRequest request, Authentication auth) {
        return characterService.createCharacter(request, auth);
    }

    @GetMapping()
    public Page<CharacterDto> getCharacterPage(Pageable pageable) {
        return characterService.getCharacterPage(pageable);
    }

    @GetMapping("{characterId}")
    public CharacterDto getCharacter(@PathVariable UUID characterId) {
        return characterService.getCharacter(characterId);
    }

    @PutMapping("{characterId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public void updateCharacter(
        @RequestBody UpdateCharacterRequest request,
        @PathVariable UUID characterId
    ) {
        characterService.updateCharacter(request, characterId);
    }

    @DeleteMapping("{characterId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public void deleteCharacter(@PathVariable UUID characterId, Authentication auth) {
        characterService.deleteCharacter(characterId, auth);
    }

    @PostMapping("{characterId}/reputation/give")
    @Operation(summary = "Начислить репутацию персонажу", description = "Добавляет репутацию персонажу по его ID")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public void giveReputation(
        @RequestBody GiveReputationRequest request,
        @PathVariable UUID characterId,
        Authentication auth
    ) {
        characterService.giveReputation(request, characterId, auth);
    }

    @PutMapping("{characterId}/skills/force")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public void updateCharacterSkill(
        @RequestBody UpdateCharacterSkillsRequest request,
        @PathVariable UUID characterId
    ) {
        characterService.updateCharacterSkill(request, characterId);
    }

    @PutMapping("{characterId}/skills/initial")
    public void selectInitialCharacterSkills(
        @RequestBody UpdateCharacterSkillsRequest request,
        @PathVariable UUID characterId,
        Authentication auth
    ) {
        characterService.selectInitialCharacterSkills(request, characterId, auth);
    }

    @PutMapping("{characterId}/skills/upgrade")
    public void upgradeCharacterSkill(
        @RequestBody UpgradeCharacterSkillRequest request,
        @PathVariable UUID characterId,
        Authentication auth
    ) {
        characterService.upgradeCharacterSkill(request, characterId, auth);
    }

    @GetMapping("{characterId}/implants")
    public List<ImplantDto> getCharacterImplants(@PathVariable UUID characterId) {
        return characterService.getCharacterImplants(characterId);
    }

    @PutMapping("{characterId}/implants")
    public void updateCharacterImplants(
        @RequestBody UpdateCharacterImplantsRequest request,
        @PathVariable UUID characterId,
        Authentication auth
    ) {
        characterService.updateCharacterImplants(request, characterId, auth);
    }

    @DeleteMapping("{characterId}/implants/{implantId}")
    public void deleteCharacterImplant(
        @RequestBody UpdateCharacterImplantsRequest request,
        @PathVariable UUID implantId,
        Authentication auth
    ) {
        characterService.updateCharacterImplants(request, implantId, auth);
    }

    @PutMapping("{characterId}/weapons")
    public void putCharacterWeapon(
        @PathVariable UUID characterId,
        @RequestBody UpdateCharacterWeaponRequest request,
        Authentication auth
    ) {
        characterService.putCharacterWeapon(request, characterId, auth);
    }
}

