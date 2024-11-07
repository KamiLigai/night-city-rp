package ru.nightcityroleplay.backend.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import ru.nightcityroleplay.backend.dto.CreateImplantRequest;
import ru.nightcityroleplay.backend.dto.CreateImplantResponse;
import ru.nightcityroleplay.backend.dto.ImplantDto;
import ru.nightcityroleplay.backend.dto.UpdateImplantRequest;
import ru.nightcityroleplay.backend.service.ImplantService;

import java.util.UUID;

@RestController
@RequestMapping("implants")
public class ImplantController {

    private final ImplantService implantService;

    public ImplantController(ImplantService implantService) {
        this.implantService = implantService;
    }

    @PostMapping
    public CreateImplantResponse createImplant(@RequestBody CreateImplantRequest request, Authentication auth) {
        return implantService.createImplant(request, auth);
    }

    @GetMapping
    public Page<ImplantDto> getImplantPage(Pageable pageble) {
        return implantService.getImplantPage(pageble);
    }

    @GetMapping("{implantid}")
    public ImplantDto getImplant(@PathVariable UUID implantid) {
        return implantService.getImplant(implantid);
    }

    @PutMapping("{implantid}")
    public void updateImplant(@RequestBody UpdateImplantRequest request, @PathVariable UUID implantid, String name) {
        implantService.updateImplant(request, implantid, name);
    }

    @DeleteMapping("{implantid}")
    public void deleteImplant(@PathVariable UUID implantid) {
        implantService.deleteImplant(implantid);
    }
}
