package ru.nightcityroleplay.backend.service;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ResponseStatusException;
import ru.nightcityroleplay.backend.dto.weapons.CreateWeaponRequest;
import ru.nightcityroleplay.backend.dto.IdsRequest;
import ru.nightcityroleplay.backend.dto.weapons.UpdateWeaponRequest;
import ru.nightcityroleplay.backend.dto.weapons.WeaponDto;
import ru.nightcityroleplay.backend.entity.CharacterEntity;
import ru.nightcityroleplay.backend.entity.Weapon;
import ru.nightcityroleplay.backend.repo.WeaponRepository;

import java.util.*;
import java.util.stream.Stream;

import static java.util.UUID.randomUUID;
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
    void createWeapon_weaponCreated_success() {
        // given
        var request = new CreateWeaponRequest();
        request.setName("test-name");
        request.setIsMelee(false);
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
            .isEqualTo(false);
        assertThat(savedWeapon.getWeaponType())
            .isEqualTo("test-weapon-type");
        assertThat(savedWeapon.getPenetration())
            .isEqualTo(2);
        assertThat(savedWeapon.getReputationRequirement())
            .isEqualTo(40);
    }

    @ParameterizedTest(name = "{index} - Проверка с данными: {0}, ожидаемое сообщение: {1}")
    @MethodSource("createWeaponWithBadRequestData")
    @SneakyThrows
    void createWeapon_badRequest_throw400(CreateWeaponRequest request, String expectedMessage) {
        // given
        Authentication auth = mock();

        // when
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            service.createWeapon(request, auth);
        });

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals(expectedMessage, exception.getReason());
    }

    public static Stream<Arguments> createWeaponWithBadRequestData() {
        return Stream.of(
            Arguments.of(CreateWeaponRequest.builder().isMelee(null).name("test-name").weaponType("test").penetration(10).reputationRequirement(10).build(),
                "'Ближнее?' не может быть null"),
            Arguments.of(CreateWeaponRequest.builder().isMelee(false).name("").weaponType("test").penetration(10).reputationRequirement(10).build(),
                "Имя оружия не может быть пустым."),
            Arguments.of(CreateWeaponRequest.builder().isMelee(false).name("test-name").weaponType(null).penetration(10).reputationRequirement(10).build(),
                "Тип этого оружия не может быть null"),
            Arguments.of(CreateWeaponRequest.builder().isMelee(true).name("test-name").weaponType("test").penetration(10).reputationRequirement(10).build(),
                "Тип оружия должен быть null"),
            Arguments.of(CreateWeaponRequest.builder().isMelee(false).name("test-name").weaponType("test").penetration(-10).reputationRequirement(10).build(),
                "Пробив не может быть отрицательным."),
            Arguments.of(CreateWeaponRequest.builder().isMelee(false).name("test-name").weaponType("test").penetration(10).reputationRequirement(-10).build(),
                "Требование к репутации не может быть отрицательным.")
        );
    }


    @Test
    void getWeaponPage_weaponExists_success() {
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
    void getWeaponDto_weaponExists_success() {
        // given
        UUID weaponId = randomUUID();
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
    void updateWeapon_weaponExists_success() {
        // given
        UUID weaponId = randomUUID();
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
    void updateWeapon_weaponNotExists_throw404() {
        // given
        UUID nonExistentWeaponId = randomUUID(); // создаем UUID, для которого оружие не будет найдено
        UpdateWeaponRequest request = new UpdateWeaponRequest();
        request.setName("some-name");

        // Настройка мока, чтобы findById возвращал пустой Optional
        when(repo.findById(nonExistentWeaponId)).thenReturn(Optional.empty());

        // when/then
        assertThrows(ResponseStatusException.class, () ->
            service.updateWeapon(request, nonExistentWeaponId)
        );
    }

    @Test
    void deleteWeapon_weaponNotExists_throw404() {
        // given
        UUID nonexistentWeaponId = randomUUID();
        when(repo.findById(nonexistentWeaponId)).thenReturn(Optional.empty());

        // when & then
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            service.deleteWeapon(nonexistentWeaponId);
        });

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Оружие не найдено", exception.getReason());

        verify(repo).findById(nonexistentWeaponId);
        verify(repo, never()).delete(any());
    }

    @Test
    void deleteWeapon_weaponExists_success() {
        // given
        UUID weaponId = randomUUID();
        Weapon weapon = new Weapon();
        weapon.setCharacters(Collections.emptyList()); // Оружие без характеристик

        when(repo.findById(weaponId)).thenReturn(Optional.of(weapon));

        // when
        service.deleteWeapon(weaponId);

        // then
        verify(repo).findById(weaponId);
        verify(repo).delete(weapon);
    }

    @Test
    void deleteWeapon_weaponExistsAndJoined_throw422() {
        // given
        UUID weaponId = randomUUID();
        Weapon weapon = new Weapon();

        List<CharacterEntity> chars = new ArrayList<>();
        chars.add(new CharacterEntity());
        weapon.setCharacters(chars);
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

    @Test
    void getWeaponsBulk_weaponExists_success() {
        // given
        when(repo.findAllByIdIn(any()))
            .thenReturn(List.of(new Weapon().setName("a"), new Weapon().setName("b")));
        // when
        List<WeaponDto> weaponsBulk = service.getWeaponsBulk(new IdsRequest(List.of(randomUUID())));

        // then
        assertThat(weaponsBulk).hasSize(2);
        assertThat(weaponsBulk.get(0).getName()).isEqualTo("a");
        assertThat(weaponsBulk.get(1).getName()).isEqualTo("b");
    }
}

