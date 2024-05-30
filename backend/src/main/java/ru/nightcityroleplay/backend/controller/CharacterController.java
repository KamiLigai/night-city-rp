package ru.nightcityroleplay.backend.controller;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import ru.nightcityroleplay.backend.dto.CharacterDto;
import ru.nightcityroleplay.backend.dto.CreateCharacterRequest;
import ru.nightcityroleplay.backend.dto.UpdateCharacterRequest;
import ru.nightcityroleplay.backend.dto.UserDto;
import ru.nightcityroleplay.backend.service.CharacterService;
import ru.nightcityroleplay.backend.service.UserService;


import java.util.UUID;

@RestController
@RequestMapping("characters")
public class CharacterController {

    private final CharacterService characterService;
    private final UserService userService;


    public CharacterController(CharacterService characterService, UserService userService) {
        this.characterService = characterService;
        this.userService = userService;
    }

    @PostMapping
    public UUID createCharacter(@RequestBody CreateCharacterRequest request) {
        return characterService.createCharacter(request);
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
    public void deleteCharacter(@PathVariable UUID characterId) {
        characterService.deleteCharacter(characterId);
    }


}


