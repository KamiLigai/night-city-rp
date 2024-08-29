package ru.nightcityroleplay.backend.service;


import org.springframework.context.annotation.Role;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.nightcityroleplay.backend.dto.CreateWeaponRequest;
import ru.nightcityroleplay.backend.dto.CreateWeaponResponse;
import ru.nightcityroleplay.backend.dto.UpdateWeaponRequest;
import ru.nightcityroleplay.backend.dto.WeaponDto;
import ru.nightcityroleplay.backend.entity.User;
import ru.nightcityroleplay.backend.entity.WeaponEntity;
import ru.nightcityroleplay.backend.exception.NightCityRpException;
import ru.nightcityroleplay.backend.repo.WeaponRepository;

import java.util.UUID;

@Service
public class WeaponService {

    private final WeaponRepository weaponRepo;

    public WeaponService(WeaponRepository weaponRepo) {

        this.weaponRepo = weaponRepo;
    }


    private WeaponDto toDto(WeaponEntity weapon) {
        WeaponDto weaponDto = new WeaponDto();
        weaponDto.setId(weapon.getId());
        weaponDto.setName(weapon.getName());
        weaponDto.setIs_melee(weapon.getIs_melee());
        weaponDto.setWeapon_type(weapon.getWeapon_type());
        weaponDto.setPenetration(weapon.getPenetration());
        weaponDto.setReputation_requirement(weapon.getReputation_requirement());
        return weaponDto;
    }

    @Transactional
    public CreateWeaponResponse createWeapon(CreateWeaponRequest request, Authentication auth) {
        WeaponEntity weapon = new WeaponEntity();
        Object principal = auth.getPrincipal();
        User user = (User) principal;
        //todo заменить Юзер на Админа
        weapon.setName(request.getName());
        weapon.setIs_melee(request.getIs_melee());
        weapon.setWeapon_type(request.getWeapon_type());
        weapon.setPenetration(request.getPenetration());
        weapon.setReputation_requirement(request.getReputation_requirement());

        weapon = weaponRepo.save(weapon);
        return new CreateWeaponResponse(weapon.getId());
    }

    //todo Page


    public void updateWeapon(UpdateWeaponRequest request, UUID weaponId, Authentication auth) {
        WeaponEntity newWeapon = new WeaponEntity();
        if (weaponRepo.findById(weaponId).isEmpty()) {
            throw new NightCityRpException("Оружие не найдено");
        }
        WeaponEntity oldWeapon = weaponRepo.findById(weaponId).get();
        Object principal = auth.getPrincipal();
        User user = (User) principal;
        UUID userid = user.getId();

        newWeapon.setName(request.getName());
        newWeapon.setIs_melee(request.getIs_melee());
        newWeapon.setWeapon_type(request.getWeapon_type());
        newWeapon.setPenetration(request.getPenetration());
        newWeapon.setReputation_requirement(request.getReputation_requirement());
        weaponRepo.save(newWeapon);

    }
}
