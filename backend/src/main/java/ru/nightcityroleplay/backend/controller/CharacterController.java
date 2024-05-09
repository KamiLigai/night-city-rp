package ru.nightcityroleplay.backend.controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.nightcityroleplay.backend.dto.CharacterDTO;
import ru.nightcityroleplay.backend.dto.CreateCharacterRequest;
import ru.nightcityroleplay.backend.dto.UpdateCharacterRequest;
import ru.nightcityroleplay.backend.service.CharacterService;


import java.util.ArrayList;
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
    public UUID createCharacter(@RequestBody CreateCharacterRequest request) {
        return characterService.createCharacter(request);
    }

    @GetMapping()
    public Page<CharacterDTO> getCharacters(Pageable pageable) {
        return characterService.getCharacterPage(pageable);
    }

    @GetMapping("{characterId}")
    public CharacterDTO getCharacter(@PathVariable UUID characterId) {
        return characterService.getCharacter(characterId);
    }


    @PutMapping("{characterId}")
    public void updateCharacter(@RequestBody UpdateCharacterRequest request, @PathVariable UUID characterId) {
        characterService.updateCharacter(request, characterId);
    }


    @DeleteMapping("{characterId}")
    public void deleteCharacter(@PathVariable UUID characterId) {
        characterService.deleteCharacter(characterId);
    }


}


