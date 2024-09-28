package ru.nightcityroleplay.backend.service;


import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
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

@SuppressWarnings("checkstyle:CommentsIndentation")
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
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public CreateWeaponResponse createWeapon(CreateWeaponRequest request, Authentication auth) {
        log.info("Администратор {} пытается создать оружие с именем: {}", auth.getName(), request.getName());
        //выдача характеристик оружию
        WeaponEntity weapon = new WeaponEntity();
        weapon.setName(request.getName());
        weapon.setIsMelee(request.getIsMelee());
        weapon.setWeaponType(request.getWeaponType());
        weapon.setPenetration(request.getPenetration());
        weapon.setReputationRequirement(request.getReputationRequirement());

        //Сохранение
        weapon = weaponRepo.save(weapon);
        log.info("Оружие с ID {} было успешно создано.", weapon.getId());
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

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public void updateWeapon(UpdateWeaponRequest request, UUID weaponId) {
        log.info("Начато обновление оружия с ID: {}", weaponId);

        // Проверка, существует ли оружие с указанным ID
        WeaponEntity existingWeapon = weaponRepo.findById(weaponId).orElseThrow(()
            -> new NightCityRpException("Оружие не найдено"));

        // Обновление существующего оружия с указанными характеристиками
        existingWeapon.setName(request.getName());
        existingWeapon.setIsMelee(request.getIsMelee());
        existingWeapon.setWeaponType(request.getWeaponType());
        existingWeapon.setPenetration(request.getPenetration());
        existingWeapon.setReputationRequirement(request.getReputationRequirement());

        // Сохранение обновленного оружия
        weaponRepo.save(existingWeapon);
        log.info("Оружие с ID: {} было успешно обновлено", weaponId);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public void deleteWeapon(UUID weaponId) {
        log.info("Запрос на удаление оружия с ID: {}", weaponId);
        WeaponEntity weapon = weaponRepo.findById(weaponId).orElse(null);
        if (weapon == null) {
            log.info("Оружие {} не найдено", weaponId);
            return;
        }
        if (weapon.getCharsId().isEmpty()) {
            weaponRepo.delete(weapon);
            log.info("Оружие с ID {} было успешно удалено", weaponId);
        } else {
            log.info("Не удалось удалить оружие с ID {}: связано с характеристиками", weaponId);
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Запрещено удаление оружия, так как оно связано с характеристиками!");
        }
    }
}
