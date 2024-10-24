package ru.nightcityroleplay.backend.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.nightcityroleplay.backend.dto.CreateImplantRequest;
import ru.nightcityroleplay.backend.dto.CreateImplantResponse;
import ru.nightcityroleplay.backend.service.ImplantService;

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
}
