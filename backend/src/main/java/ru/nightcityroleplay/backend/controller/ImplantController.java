package ru.nightcityroleplay.backend.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import ru.nightcityroleplay.backend.dto.CreateImplantRequest;
import ru.nightcityroleplay.backend.dto.CreateImplantResponse;
import ru.nightcityroleplay.backend.dto.ImplantDto;
import ru.nightcityroleplay.backend.dto.UpdateImplantRequest;
import ru.nightcityroleplay.backend.service.ImplantService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("implants")
public class ImplantController {

    private final ImplantService implantService;

    public ImplantController(ImplantService implantService) {
        this.implantService = implantService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public CreateImplantResponse createImplant(@RequestBody CreateImplantRequest request, Authentication auth) {
        return implantService.createImplant(request, auth);
    }

    @GetMapping
    public Page<ImplantDto> getImplantPage(Pageable pageble) {
        return implantService.getImplantPage(pageble);
    }

    @GetMapping("{implantId}")
    public ImplantDto getImplant(@PathVariable UUID implantId) {
        return implantService.getImplant(implantId);
    }

    @PutMapping("{implantId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public void updateImplant(@RequestBody UpdateImplantRequest request, @PathVariable UUID implantId, String name) {
        implantService.updateImplant(request, implantId, name);
    }

    @GetMapping("{implantId}/assigned-count")
    public Integer getImplantStatus(@PathVariable UUID implantId) {
        return implantService.getImplantStatus(implantId);
    }

    @DeleteMapping("{implantId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public void deleteImplant(@PathVariable UUID implantId, @RequestParam boolean redButton) {
        implantService.deleteImplant(implantId, redButton);
    }

    // Получение списка всех ID имплантов
    @GetMapping("/ids")
    public List<UUID> getImplantIds() {
        return implantService.getAllImplantIds();
    }

    // Получение деталей имплантов по запросу с несколькими ID
    @PostMapping("/get-bulk")
    public List<ImplantDto> getBulkImplants(@RequestBody List<UUID> implantIds) {
        return implantService.getBulkImplants(implantIds);
    }
}
