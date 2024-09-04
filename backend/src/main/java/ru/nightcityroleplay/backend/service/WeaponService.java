package ru.nightcityroleplay.backend.service;


import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.nightcityroleplay.backend.dto.CreateWeaponRequest;
import ru.nightcityroleplay.backend.dto.CreateWeaponResponse;
import ru.nightcityroleplay.backend.dto.UpdateWeaponRequest;
import ru.nightcityroleplay.backend.dto.WeaponDto;
import ru.nightcityroleplay.backend.entity.WeaponEntity;
import ru.nightcityroleplay.backend.exception.NightCityRpException;
import ru.nightcityroleplay.backend.repo.WeaponRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class WeaponService {

    private final WeaponRepository weaponRepo;

    public WeaponService(WeaponRepository weaponRepo) {
        this.weaponRepo = weaponRepo;
    }

    private WeaponDto toDto(WeaponEntity weapon) {
        WeaponDto weaponDto = new WeaponDto();
        weaponDto.setId(weapon.getId());
        weaponDto.setName(weapon.getName());
        weaponDto.setIsMelee(weapon.getIsMelee());
        weaponDto.setWeaponType(weapon.getWeaponType());
        weaponDto.setPenetration(weapon.getPenetration());
        weaponDto.setReputationRequirement(weapon.getReputationRequirement());
        return weaponDto;
    }

    @Transactional
    public CreateWeaponResponse createWeapon(CreateWeaponRequest request, Authentication auth) {
        WeaponEntity weapon = new WeaponEntity();
        //todo заменить Юзер на Админа
        weapon.setName(request.getName());
        weapon.setIsMelee(request.getIsMelee());
        weapon.setWeaponType(request.getWeaponType());
        weapon.setPenetration(request.getPenetration());
        weapon.setReputationRequirement(request.getReputationRequirement());

        weapon = weaponRepo.save(weapon);
        return new CreateWeaponResponse(weapon.getId());
    }

    @Transactional
    public Page<WeaponDto> getWeaponPage(Pageable pageable) {
        Page<WeaponEntity> weaponPage = weaponRepo.findAll(pageable);
        List<WeaponEntity> weapons = weaponPage.toList();
        List<WeaponDto> weaponDtos = new ArrayList<>();
        for (WeaponEntity weapon : weapons) {
            weaponDtos.add(toDto(weapon));
        }
        return new PageImpl<>(weaponDtos, pageable, weaponPage.getTotalElements());
    }

    @Transactional
    public WeaponDto getWeapon(UUID weaponId) {
        Optional<WeaponEntity> weaponById = weaponRepo.findById(weaponId);
        return weaponById.map(this::toDto).orElse(null);
    }

    public void updateWeapon(UpdateWeaponRequest request, UUID weaponId) {
        WeaponEntity newWeapon = new WeaponEntity();
        if (weaponRepo.findById(weaponId).isEmpty()) {
            throw new NightCityRpException("Оружие не найдено");
        }
        newWeapon.setName(request.getName());
        newWeapon.setIsMelee(request.getIsMelee());
        newWeapon.setWeaponType(request.getWeaponType());
        newWeapon.setPenetration(request.getPenetration());
        newWeapon.setReputationRequirement(request.getReputationRequirement());
        weaponRepo.save(newWeapon);
    }

    public void deleteWeapon(UUID weaponId) {
        WeaponEntity weapon = weaponRepo.findById(weaponId).orElse(null);
        if (weapon == null) {
            log.info("Оружие {} не найдено", weaponId);
            return;
        }
        if (weapon.getCharsId().isEmpty()) {
            weaponRepo.delete(weapon);
            log.info("Оружие {} удалено", weaponId);
        } else {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Запрещено!");
        }
    }
}
