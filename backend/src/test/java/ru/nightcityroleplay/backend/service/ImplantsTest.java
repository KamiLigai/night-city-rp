package ru.nightcityroleplay.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ResponseStatusException;
import ru.nightcityroleplay.backend.dto.CreateImplantRequest;
import ru.nightcityroleplay.backend.dto.UpdateImplantRequest;
import ru.nightcityroleplay.backend.entity.Implant;
import ru.nightcityroleplay.backend.entity.User;
import ru.nightcityroleplay.backend.repo.ImplantRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


public class ImplantsTest {

    ImplantService service;

    ImplantRepository implantRepo;

    @BeforeEach
    void setUp() {
        implantRepo = mock();
        service = new ImplantService(implantRepo);
    }

    @Test
    void createImplant_implantIsSaved_success() {
        // given
        var request = new CreateImplantRequest();
        request.setName("Клинки Богомолла TEST");
        request.setImplantType("Конечности");
        request.setDescription("Боевой кибернетический имплант, смертоносные клинки которого исходят из предплечья, что позволяет использовать их как оружие ближнего боя.\n" +
            "\n" +
            "Уровень Пробивной мощности: 3");
        request.setReputationRequirement(70);
        request.setImplantPointsCost(3);
        request.setSpecialImplantPointsCost(0);

        Authentication auth = mock();
        User user = new User();

        when(auth.getPrincipal())
            .thenReturn(user);

        UUID id = randomUUID();
        Implant implant = new Implant();
        implant.setId(id);
        when(implantRepo.save(any()))
            .thenReturn(implant);

        // when
        service.createImplant(request, auth);

        // then
        verify(implantRepo).save(any());
        verify(implantRepo, never()).deleteById(any());
        Object someObject = mock();
        verifyNoInteractions(someObject);
    }

    @Test
    void getImplant_implantIsNotNull_success() {
        // given
        UUID implantId = randomUUID();
        Implant implant = new Implant();
        implant.setId(implantId);
        implant.setName("Клинки Богомолла TEST");
        implant.setImplantType("Конечности");
        implant.setDescription("Боевой кибернетический имплант, смертоносные клинки которого исходят из предплечья, что позволяет использовать их как оружие ближнего боя.\n" +
            "\n" +
            "Уровень Пробивной мощности: 3");
        implant.setReputationRequirement(70);
        implant.setImplantPointsCost(3);
        implant.setSpecialImplantPointsCost(0);

        when(implantRepo.findById(implantId))
            .thenReturn(Optional.of(implant));

        // when
        var result = service.getImplant(implantId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(implantId);
        assertThat(result.getName()).isEqualTo("Клинки Богомолла TEST");
        assertThat(result.getImplantType()).isEqualTo("Конечности");
        assertThat(result.getDescription()).isEqualTo("Боевой кибернетический имплант, смертоносные клинки которого исходят из предплечья, что позволяет использовать их как оружие ближнего боя.\n" +
            "\n" +
            "Уровень Пробивной мощности: 3");
        assertThat(result.getReputationRequirement()).isEqualTo(70);
        assertThat(result.getImplantPointsCost()).isEqualTo(3);
        assertThat(result.getSpecialImplantPointsCost()).isEqualTo(0);
    }

    @Test
    public void updateImplant_implantExists_success() {
        // given
        UUID implantId = UUID.randomUUID();
        Implant existingImplant = new Implant();
        existingImplant.setId(implantId);
        existingImplant.setName("Старый Имплант");

        UpdateImplantRequest request = new UpdateImplantRequest();
        request.setName("Новый Имплант");
        request.setImplantType("Конечность");
        request.setDescription("Описание");
        request.setReputationRequirement(40);
        request.setImplantPointsCost(3);
        request.setSpecialImplantPointsCost(0);

        when(implantRepo.findById(implantId)).thenReturn(Optional.of(existingImplant));

        // when
        service.updateImplant(request, implantId, "Новый Имплант");

        // then
        assertEquals("Новый Имплант", existingImplant.getName());
        assertEquals("Конечность", existingImplant.getImplantType());
        assertEquals("Описание", existingImplant.getDescription());
        assertEquals(40, existingImplant.getReputationRequirement());
        assertEquals(3, existingImplant.getImplantPointsCost());
        assertEquals(0, existingImplant.getSpecialImplantPointsCost());
        verify(implantRepo).save(existingImplant); // Убедитесь, что метод save был вызван
    }


    @Test
    void updateImplant_implantIsAbsent_throwException() {
        // given
        var request = new UpdateImplantRequest();
        request.setName("Valid Name");
        request.setImplantType("Valid Type");
        request.setDescription("Valid Description");
        request.setReputationRequirement(10);
        request.setImplantPointsCost(20);
        request.setSpecialImplantPointsCost(5);

        UUID implantId = UUID.randomUUID();
        String implantName = "Клинки Богомолла TEST";

        // when
        when(implantRepo.findById(implantId)).thenReturn(Optional.empty());

        // then
        assertThatThrownBy(() -> service.updateImplant(request, implantId, implantName))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("Имплант не найден")
            .matches(exception -> ((ResponseStatusException) exception).getStatusCode() == HttpStatus.NOT_FOUND);
    }
    @Test
    public void deleteImplant_implantExists_success() {
        // given
        UUID implantId = UUID.randomUUID();
        Implant implant = new Implant();
        implant.setId(implantId);
        implant.setChars(List.of());

        when(implantRepo.findById(implantId)).thenReturn(Optional.of(implant));

        // when
        service.deleteImplant(implantId);

        // then
        verify(implantRepo).delete(implant);
    }

    @Test
    public void deleteImplant_implantIsAbsent_throw404() {
        // given
        UUID implantId = UUID.randomUUID();
        when(implantRepo.findById(implantId)).thenReturn(Optional.empty());

        // when
        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> service.deleteImplant(implantId)
        );

        // then
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(exception.getMessage()).contains("Имплант не найден");

        verify(implantRepo, never()).delete(any(Implant.class));
    }
}

