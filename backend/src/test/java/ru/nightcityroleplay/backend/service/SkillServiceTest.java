package ru.nightcityroleplay.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.server.ResponseStatusException;
import ru.nightcityroleplay.backend.dto.CreateSkillRequest;
import ru.nightcityroleplay.backend.dto.UpdateSkillRequest;
import ru.nightcityroleplay.backend.entity.CharacterEntity;
import ru.nightcityroleplay.backend.entity.Skill;
import ru.nightcityroleplay.backend.entity.User;
import ru.nightcityroleplay.backend.repo.SkillRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

class SkillServiceTest {

    SkillService service;
    SkillRepository skillRepo;

    @BeforeEach
    void setUp() {
        skillRepo = mock();
        service = new SkillService(skillRepo);
    }

    @Test
    void getSkill_skillIsAbsent_throw404() {
        // given
        UUID id = randomUUID();
        when(skillRepo.findById(id))
            .thenReturn(Optional.empty());

        // then
        assertThatThrownBy(() -> service.getSkill(id))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("Навык " + id + " не найден")
            .extracting(ResponseStatusException.class::cast)
            .extracting(ErrorResponseException::getStatusCode)
            .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void createSkill_skillExists_success() {
        // given
        var request = new CreateSkillRequest();
        request.setName("Что-то");
        request.setDescription("Делает что-то");
        request.setLevel(1);
        request.setType("CIVIL");
        request.setCost(2);

        Authentication auth = mock();
        User user = new User();

        when(auth.getPrincipal())
            .thenReturn(user);

        UUID id = randomUUID();
        var skill = new Skill();
        skill.setId(id);
        when(skillRepo.save(any()))
            .thenReturn(skill);

        // when
        service.createSkill(request);

        // then
        verify(skillRepo).save(any());
        verify(skillRepo, never()).deleteById(any());
        Object someObject = mock();
        verifyNoInteractions(someObject);
    }

    @Test
    void getSkill_skillWithData_isNotNull() {
        // given
        UUID skillId = randomUUID();
        Skill skill = new Skill();
        skill.setId(skillId);
        skill.setName("Что-то");
        skill.setDescription("Делает что-то");
        skill.setLevel(1);
        skill.setType("CIVIL");
        skill.setCost(2);

        when(skillRepo.findById(skillId))
            .thenReturn(Optional.of(skill));

        // when
        var result = service.getSkill(skillId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(skillId);
        assertThat(result.getName()).isEqualTo("Что-то");
        assertThat(result.getDescription()).isEqualTo("Делает что-то");
        assertThat(result.getLevel()).isEqualTo(1);
        assertThat(result.getType()).isEqualTo("CIVIL");
        assertThat(result.getCost()).isEqualTo(2);
    }

    @Test
    void updateSkill_skillNotExists_throw404() {
        // given
        var request = new UpdateSkillRequest();
        UUID skillId = UUID.randomUUID();

        when(skillRepo.findById(skillId)).thenReturn(Optional.empty());

        // then
        assertThatThrownBy(() -> service.updateSkill(request, skillId))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("Навык " + skillId + " не найден")
            .extracting(ResponseStatusException.class::cast)
            .extracting(ErrorResponseException::getStatusCode)
            .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void deleteSkill_skillExists_success() {
        // given
        UUID skillId = UUID.randomUUID();

        Skill skill = new Skill();
        skill.setId(skillId);
        skill.setCharacters(List.of());

        when(skillRepo.findById(skillId)).thenReturn(Optional.of(skill));

        // when
        service.deleteSkill(skillId);

        // then
        verify(skillRepo).delete(skill);
    }

    @Test
    public void deleteSkill_skillNotExists_throw404() {
        // given
        UUID skillId = UUID.randomUUID();

        // when
        when(skillRepo.findById(skillId)).thenReturn(Optional.empty());

        // then
        assertThatThrownBy(() -> service.deleteSkill(skillId))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("Навык " + skillId + " не найден")
            .extracting(ResponseStatusException.class::cast)
            .extracting(ErrorResponseException::getStatusCode)
            .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void deleteSkill_unauthorized_throw422() {
        // given
        UUID skillId = UUID.randomUUID();

        Skill skill = new Skill();
        skill.setId(skillId);
        skill.setCharacters(List.of(new CharacterEntity()));

        when(skillRepo.findById(skillId)).thenReturn(Optional.of(skill));

        // then
        assertThatThrownBy(() -> service.deleteSkill(skillId))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("Этот навык есть как минимум у одного персонажа!")
            .extracting(ResponseStatusException.class::cast)
            .extracting(ErrorResponseException::getStatusCode)
            .isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }
}