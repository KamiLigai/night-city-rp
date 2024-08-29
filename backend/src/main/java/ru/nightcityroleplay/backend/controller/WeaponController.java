package ru.nightcityroleplay.backend.controller;


import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.nightcityroleplay.backend.dto.CreateWeaponRequest;
import ru.nightcityroleplay.backend.dto.CreateWeaponResponse;
import ru.nightcityroleplay.backend.service.WeaponService;

@RestController
@RequestMapping("weapons")
public class WeaponController {

    private final WeaponService weaponService;

    public WeaponController(WeaponService weaponService) {
        this.weaponService = weaponService;
    }
    @PostMapping
    public CreateWeaponResponse createWeapon(@RequestBody CreateWeaponRequest request, Authentication auth){
        return weaponService.createWeapon(request, auth);
    }



}
