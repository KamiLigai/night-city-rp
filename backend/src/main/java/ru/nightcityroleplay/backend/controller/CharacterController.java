package ru.nightcityroleplay.backend.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import ru.nightcityroleplay.backend.dto.*;
import ru.nightcityroleplay.backend.service.CharacterService;

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
    public Page<CharacterDto> getCharacters(Pageable pageable) {
        return characterService.getCharacterPage(pageable);
    }
    @GetMapping("{characterId}")
    public CharacterDto getCharacter(@PathVariable UUID characterId) {
        return characterService.getCharacter(characterId);
    }
    @PutMapping("{characterId}")
    public void updateCharacter(@RequestBody UpdateCharacterRequest request, @PathVariable UUID characterId, Authentication auth) {
        characterService.updateCharacter(request, characterId, auth);
    }
    @DeleteMapping("{characterId}")
    public void deleteCharacter(@PathVariable UUID characterId, Authentication auth) {
        characterService.deleteCharacter(characterId, auth);
    }
    @PutMapping("{characterId}/skills")
    public void updateCharacterSkill(@RequestBody UpdateCharacterSkillRequest request, @PathVariable UUID characterId, Authentication auth) {
        characterService.updateCharacterSkill(request, characterId, auth);
    }
    @PutMapping("{characterId}/weapons")
    public void putCharacterWeapon(@PathVariable UUID characterId, @RequestBody UpdateCharacterWeaponRequest request, Authentication auth) {
        characterService.putCharacterWeapon(request, characterId, auth);
    }
    @DeleteMapping("{characterId}/weapons/{weaponId}")
    public void deleteCharacterWeapon(@PathVariable UUID characterId, @PathVariable UUID weaponId, Authentication auth) {
        characterService.deleteCharacterWeapon(weaponId, characterId, auth);
    }
}


