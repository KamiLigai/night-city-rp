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
import ru.nightcityroleplay.backend.dto.IdsRequest;
import ru.nightcityroleplay.backend.dto.UpdateWeaponRequest;
import ru.nightcityroleplay.backend.dto.WeaponDto;
import ru.nightcityroleplay.backend.entity.Weapon;
import ru.nightcityroleplay.backend.repo.WeaponRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class WeaponService {

    private final WeaponRepository weaponRepo;

    public WeaponService(WeaponRepository weaponRepo) {
        this.weaponRepo = weaponRepo;
    }

    private WeaponDto toDto(Weapon weapon) {
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
        log.info("Администратор {} пытается создать оружие с именем: {}", auth.getName(), request.getName());
        //выдача характеристик оружию
        Weapon weapon = new Weapon();
        weapon.setName(request.getName());
        weapon.setIsMelee(request.getIsMelee());
        weapon.setWeaponType(request.getWeaponType());
        weapon.setPenetration(request.getPenetration());
        weapon.setReputationRequirement(request.getReputationRequirement());

        //проверка на отрицательные значения
        if (weapon.getName() == null || weapon.getName().isEmpty())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Имя оружия не может быть пустым.");
        if (weapon.getIsMelee() == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "'Ближнее?' не может быть null");
        if (weapon.getWeaponType() == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Тип Оружия не может быть null");
        if (weapon.getPenetration() < 0)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Пробив не может быть отрицательным.");
        if (weapon.getReputationRequirement() < 0)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Требование к репутации не может быть отрицательным.");

        //Сохранение
        weapon = weaponRepo.save(weapon);
        log.info("Оружие с ID {} было успешно создано.", weapon.getId());
        return new CreateWeaponResponse(weapon.getId());
    }

    @Transactional
    public Page<WeaponDto> getWeaponPage(Pageable pageable) {
        Page<Weapon> weaponPage = weaponRepo.findAll(pageable);
        List<Weapon> weapons = weaponPage.toList();
        List<WeaponDto> weaponDtos = new ArrayList<>();
        for (Weapon weapon : weapons) {
            weaponDtos.add(toDto(weapon));
        }
        return new PageImpl<>(weaponDtos, pageable, weaponPage.getTotalElements());
    }

    @Transactional
    public WeaponDto getWeapon(UUID weaponId) {
        return weaponRepo.findById(weaponId)
            .map(this::toDto)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Оружие не найдено"));
    }

    @Transactional
    public void updateWeapon(UpdateWeaponRequest request, UUID weaponId) {
        log.info("Начато обновление оружия с ID: {}", weaponId);

        // Проверка, существует ли оружие с указанным ID
        Weapon existingWeapon = weaponRepo.findById(weaponId).orElseThrow(()
            -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Оружие не найдено"));

        // Обновление существующего оружия с указанными характеристиками
        existingWeapon.setName(request.getName());
        existingWeapon.setIsMelee(request.getIsMelee());
        existingWeapon.setWeaponType(request.getWeaponType());
        existingWeapon.setPenetration(request.getPenetration());
        existingWeapon.setReputationRequirement(request.getReputationRequirement());

        //проверка на отрицательные значения
        if (existingWeapon.getName() == null || existingWeapon.getName().isEmpty())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Имя оружия не может быть пустым.");
        if (existingWeapon.getIsMelee() == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "'Ближнее?' не может быть null");
        if (existingWeapon.getWeaponType() == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Тип Оружия не может быть null");
        if (existingWeapon.getPenetration() < 0)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Пробив не может быть отрицательным.");
        if (existingWeapon.getReputationRequirement() < 0)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Требование к репутации не может быть отрицательным.");

        // Сохранение обновленного оружия
        weaponRepo.save(existingWeapon);
        log.info("Оружие с ID: {} было успешно обновлено", weaponId);
    }

    @Transactional
    public void deleteWeapon(UUID weaponId) {
        log.info("Запрос на удаление оружия с ID: {}", weaponId);
        Weapon weapon = weaponRepo.findById(weaponId).orElse(null);

        if (weapon == null) {
            log.info("Оружие {} не найдено", weaponId);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Оружие не найдено");
        }

        if (weapon.getCharacters().isEmpty()) {
            weaponRepo.delete(weapon);
            log.info("Оружие с ID {} было успешно удалено", weaponId);
        } else {
            log.info("Не удалось удалить оружие с ID {}: связано с характеристиками", weaponId);
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Запрещено удаление оружия, так как оно связано с характеристиками!");
        }
    }

    @Transactional(readOnly = true)
    public List<UUID> getWeaponIds() {
        return weaponRepo.findAllWeaponIds();
    }

    @Transactional(readOnly = true)
    public List<WeaponDto> getWeaponsBulk(IdsRequest request) {
        return weaponRepo.findAllByIdIn(request.getIds())
            .stream().map(this::toDto)
            .toList();
    }
}
