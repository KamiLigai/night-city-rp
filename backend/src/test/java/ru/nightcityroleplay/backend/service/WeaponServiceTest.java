package ru.nightcityroleplay.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ResponseStatusException;
import ru.nightcityroleplay.backend.dto.CreateWeaponRequest;
import ru.nightcityroleplay.backend.dto.UpdateWeaponRequest;
import ru.nightcityroleplay.backend.dto.WeaponDto;
import ru.nightcityroleplay.backend.entity.CharacterEntity;
import ru.nightcityroleplay.backend.entity.Weapon;
import ru.nightcityroleplay.backend.exception.NightCityRpException;
import ru.nightcityroleplay.backend.repo.WeaponRepository;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class WeaponServiceTest {

    WeaponService service;
    WeaponRepository repo;

    @BeforeEach
    void setUp() {
        repo = mock();
        service = new WeaponService(repo);
    }

    @Test
    void createWeapon() {
        // given
        var request = new CreateWeaponRequest();
        request.setName("test-name");
        request.setIsMelee(true);
        request.setWeaponType("test-weapon-type");
        request.setPenetration(2);
        request.setReputationRequirement(40);

        var weapon = new Weapon();
        when(repo.save(any()))
            .thenReturn(weapon);
        Authentication auth = mock();

        // when
        service.createWeapon(request, auth);

        // then
        var weaponCaptor = ArgumentCaptor.forClass(Weapon.class);
        verify(repo).save(weaponCaptor.capture());
        Weapon savedWeapon = weaponCaptor.getValue();
        assertThat(savedWeapon.getName())
            .isEqualTo("test-name");
        assertThat(savedWeapon.getIsMelee())
            .isEqualTo(true);
        assertThat(savedWeapon.getWeaponType())
            .isEqualTo("test-weapon-type");
        assertThat(savedWeapon.getPenetration())
            .isEqualTo(2);
        assertThat(savedWeapon.getReputationRequirement())
            .isEqualTo(40);
    }


    @Test
    void getWeaponPageTest() {
        // given
        Pageable pageable = mock();
        Page<Weapon> weaponPage = mock();
        when(weaponPage.toList())
            .thenReturn(List.of(
                new Weapon()
                    .setName("test-weapon")
            ));
        when(repo.findAll(any(Pageable.class)))
            .thenReturn(weaponPage);


        // when
        var result = service.getWeaponPage(pageable);

        // then
        verify(repo).findAll(pageable);
        assertThat(result.getContent())
            .isEqualTo(List.of(
                new WeaponDto()
                    .setName("test-weapon")
            ));
    }

    @Test
    void getWeapon_ShouldReturnWeaponDto_WhenWeaponExists() {
        // given
        UUID weaponId = UUID.randomUUID();
        Weapon weaponEntity = new Weapon();
        weaponEntity.setId(weaponId);
        weaponEntity.setName("test-name");
        weaponEntity.setIsMelee(true);
        weaponEntity.setWeaponType("test-weapon-type");
        weaponEntity.setPenetration(2);
        weaponEntity.setReputationRequirement(40);
        when(repo.findById(weaponId)).thenReturn(Optional.of(weaponEntity));

        // when
        WeaponDto result = service.getWeapon(weaponId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(weaponId);
        assertThat(result.getName()).isEqualTo("test-name");
        assertThat(result.getIsMelee()).isEqualTo(true);
        assertThat(result.getWeaponType()).isEqualTo("test-weapon-type");
        assertThat(result.getPenetration()).isEqualTo(2);
        assertThat(result.getReputationRequirement()).isEqualTo(40);
        verify(repo).findById(weaponId);
    }

    @Test
    void updateWeapon_ShouldUpdateWeapon_WhenWeaponExists() {
        // given
        UUID weaponId = UUID.randomUUID();
        UpdateWeaponRequest request = new UpdateWeaponRequest();
        request.setName("updated-name");
        request.setIsMelee(false);
        request.setWeaponType("updated-weapon-type");
        request.setPenetration(5);
        request.setReputationRequirement(50);

        Weapon existingWeapon = new Weapon();
        existingWeapon.setId(weaponId); // Солформация ID существующего оружия

        // Настройка мока, чтобы findById возвращал существующее оружие
        when(repo.findById(weaponId)).thenReturn(Optional.of(existingWeapon));

        // when
        service.updateWeapon(request, weaponId);

        // then
        assertThat(existingWeapon.getName()).isEqualTo("updated-name");
        assertThat(existingWeapon.getIsMelee()).isEqualTo(false);
        assertThat(existingWeapon.getWeaponType()).isEqualTo("updated-weapon-type");
        assertThat(existingWeapon.getPenetration()).isEqualTo(5);
        assertThat(existingWeapon.getReputationRequirement()).isEqualTo(50);

        verify(repo).findById(weaponId);
        verify(repo).save(existingWeapon);
    }


    @Test
    void updateWeapon_ShouldThrowException_WhenWeaponDoesNotExist() {
        // given
        UUID nonExistentWeaponId = UUID.randomUUID(); // создаем UUID, для которого оружие не будет найдено
        UpdateWeaponRequest request = new UpdateWeaponRequest();
        request.setName("some-name");

        // Настройка мока, чтобы findById возвращал пустой Optional
        when(repo.findById(nonExistentWeaponId)).thenReturn(Optional.empty());

        // when/then
        assertThrows(NightCityRpException.class, () ->
            service.updateWeapon(request, nonExistentWeaponId)
        );
    }

    @Test
    void deleteWeapon_ShouldDeleteWeapon_WhenWeaponNotExist() {
        // given
        UUID nonexistentWeaponId = UUID.randomUUID();
        when(repo.findById(nonexistentWeaponId)).thenReturn(Optional.empty());

        // when
        service.deleteWeapon(nonexistentWeaponId);

        // then
        verify(repo).findById(nonexistentWeaponId);
        verify(repo, never()).delete(any());
    }

    @Test
    void deleteWeapon_ShouldDeleteWeapon_WhenWeaponHasNoChars() {
        // given
        UUID weaponId = UUID.randomUUID();
        Weapon weapon = new Weapon();
        weapon.setCharsId(Collections.emptyList()); // Оружие без характеристик

        when(repo.findById(weaponId)).thenReturn(Optional.of(weapon));

        // when
        service.deleteWeapon(weaponId);

        // then
        verify(repo).findById(weaponId);
        verify(repo).delete(weapon);
    }

    @Test
    void deleteWeapon_ShouldThrowException_WhenWeaponHasChars() {
        // given
        UUID weaponId = UUID.randomUUID();
        Weapon weapon = new Weapon();

        List<CharacterEntity> chars = new ArrayList<>();
        chars.add(new CharacterEntity());
        weapon.setCharsId(chars);
        when(repo.findById(weaponId)).thenReturn(Optional.of(weapon));

        // when
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
            () -> service.deleteWeapon(weaponId));

        // then
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, exception.getStatusCode());
        assertEquals("Запрещено удаление оружия, так как оно связано с характеристиками!", exception.getReason());

        verify(repo).findById(weaponId);
        verify(repo, never()).delete(any());
    }
}

