package ru.nightcityroleplay.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.nightcityroleplay.backend.dto.CreateWeaponRequest;
import ru.nightcityroleplay.backend.dto.WeaponDto;
import ru.nightcityroleplay.backend.entity.WeaponEntity;
import ru.nightcityroleplay.backend.repo.WeaponRepository;

import java.util.List;
import java.util.Random;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
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

        var weapon = new WeaponEntity();
        when(repo.save(any()))
            .thenReturn(weapon);
        Authentication auth = mock();

        // when
        service.createWeapon(request, auth);

        // then
        var weaponCaptor = ArgumentCaptor.forClass(WeaponEntity.class);
        verify(repo).save(weaponCaptor.capture());
        WeaponEntity savedWeapon = weaponCaptor.getValue();
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
        Page<WeaponEntity> weaponPage = mock();
        when(weaponPage.toList())
            .thenReturn(List.of(
                new WeaponEntity()
                    .setName("test-weapon")
            ));
        when(repo.findAll(any(Pageable.class)))
            .thenReturn(weaponPage);
        UUID id = UUID.randomUUID();

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
}
