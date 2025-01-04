package ru.nightcityroleplay.backend.controller;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import ru.nightcityroleplay.backend.dto.CreateWeaponRequest;
import ru.nightcityroleplay.backend.dto.CreateWeaponResponse;
import ru.nightcityroleplay.backend.dto.IdsRequest;
import ru.nightcityroleplay.backend.dto.UpdateWeaponRequest;
import ru.nightcityroleplay.backend.dto.WeaponDto;
import ru.nightcityroleplay.backend.service.WeaponService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("weapons")
public class WeaponController {
    private final WeaponService weaponService;

    public WeaponController(WeaponService weaponService) {
        this.weaponService = weaponService;
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping
    public CreateWeaponResponse createWeapon(@RequestBody CreateWeaponRequest request, Authentication auth) {
        return weaponService.createWeapon(request, auth);
    }

    @GetMapping
    public Page<WeaponDto> getWeaponPage(Pageable pageable) {
        return weaponService.getWeaponPage(pageable);
    }

    @GetMapping("{weaponId}")
    public WeaponDto getWeapon(@PathVariable UUID weaponId) {
        return weaponService.getWeapon(weaponId);
    }

    @GetMapping("ids")
    public List<UUID> getWeaponIds() {
        return weaponService.getWeaponIds();
    }

    @PostMapping("get-bulk")
    public List<WeaponDto> getWeaponsBulk(@RequestBody IdsRequest request) {
        return weaponService.getWeaponsBulk(request);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("{weaponId}")
    public void updateWeapon(@RequestBody UpdateWeaponRequest request, @PathVariable UUID weaponId) {
        weaponService.updateWeapon(request, weaponId);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("{weaponId}")
    public void deleteWeapon(@PathVariable UUID weaponId) {
        weaponService.deleteWeapon(weaponId);
    }
}
