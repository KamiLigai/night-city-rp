package ru.nightcityroleplay.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.server.ResponseStatusException;
import ru.nightcityroleplay.backend.dto.CreateSkillRequest;
import ru.nightcityroleplay.backend.dto.SkillDto;
import ru.nightcityroleplay.backend.dto.UpdateSkillRequest;
import ru.nightcityroleplay.backend.entity.CharacterEntity;
import ru.nightcityroleplay.backend.entity.Skill;
import ru.nightcityroleplay.backend.entity.User;
import ru.nightcityroleplay.backend.repo.SkillRepository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
        request.setTypeIsBattle(true);
        request.setBattleCost(1);

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
    void testCreateSkillx10Battle_Success() {
        // given
        CreateSkillRequest request = new CreateSkillRequest();
        request.setSkillFamily("Pistol");
        request.setName("Стрельба из пистолетов");
        request.setDescription("Стрельба из пистолетов и револьверов. Улучшает точность и скорость перезарядки");
        request.setSkillClass("Стрелок");
        request.setTypeIsBattle(true);

        // when
        when(skillRepo.save(any(Skill.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ArgumentCaptor<Skill> skillCaptor = ArgumentCaptor.forClass(Skill.class);
        verify(skillRepo, times(10)).save(skillCaptor.capture());

        List<Skill> savedSkills = skillCaptor.getAllValues();
        for (int i = 0; i < 10; i++) {
            Skill skill = savedSkills.get(i);
            assertThat(skill.getLevel()).isEqualTo(i + 1);
            if (skill.getLevel() >= 6) {
                assertThat(skill.getReputationRequirement()).isGreaterThan(0);
            }
        }
    }

    @Test
    void testCreateSkillx10Civil_Success() {
        // given
        CreateSkillRequest request = new CreateSkillRequest();
        request.setSkillFamily("Sing");
        request.setName("Пение");
        request.setDescription("Пение");
        request.setSkillClass("Рокер");
        request.setTypeIsBattle(false);

        // when
        when(skillRepo.save(any(Skill.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ArgumentCaptor<Skill> skillCaptor = ArgumentCaptor.forClass(Skill.class);
        verify(skillRepo, times(10)).save(skillCaptor.capture());

        List<Skill> savedSkills = skillCaptor.getAllValues();
        for (int i = 0; i < 10; i++) {
            Skill skill = savedSkills.get(i);
            assertThat(skill.getLevel()).isEqualTo(i + 1);
            if (skill.getLevel() >= 6) {
                assertThat(skill.getReputationRequirement()).isGreaterThan(0);
            }
        }
    }

    @Test
    void testBuildSkill_InvalidLevel() {
        // given
        CreateSkillRequest request = new CreateSkillRequest();
        request.setSkillFamily("Magic");
        request.setName("Ice Blast");
        request.setDescription("Chilling ice attack.");
        request.setSkillClass("Mage");
        request.setTypeIsBattle(false);

        // then
        assertThatThrownBy(() -> service.buildSkill(request, 0))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("Уровень навыка должен быть от 1 до 10");

        assertThatThrownBy(() -> service.buildSkill(request, 11))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("Уровень навыка должен быть от 1 до 10");
    }

    @Test
    public void updateSkill_nonExistingSkill_throwsNotFoundException() {
        // given
        UUID skillId = UUID.randomUUID();
        when(skillRepo.findById(skillId)).thenReturn(Optional.empty());

        UpdateSkillRequest updateRequest = new UpdateSkillRequest();
        updateRequest.setName("New Name");
        updateRequest.setDescription("New Description");
        updateRequest.setTypeIsBattle(true);

        // when / then
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
            service.updateSkill(updateRequest, skillId));

        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(exception.getReason()).isEqualTo("Навык " + skillId + " не найден");

        verify(skillRepo, never()).save(any());
    }

    @Test
    void testUpdateSkillx10ByName_Success() {
        //given
        String oldName = "Old Name";
        String newName = "New Name";

        UpdateSkillRequest updateRequest = new UpdateSkillRequest();
        updateRequest.setName(newName);
        updateRequest.setDescription("New Description");
        updateRequest.setTypeIsBattle(true);

        Skill skill = new Skill();
        skill.setId(randomUUID());
        skill.setName(oldName);
        skill.setDescription("Old Description");
        skill.setTypeIsBattle(false);
        when(skillRepo.findByName(oldName)).thenReturn(Collections.singletonList(skill));
        service.updateSkillx10ByName(updateRequest, oldName);

        // when
        verify(skillRepo, times(1)).save(skill);

        // then
        assertThat(skill.getName()).isEqualTo(newName);
        assertThat(skill.getDescription()).isEqualTo("New Description");
        assertThat(skill.getTypeIsBattle()).isTrue();
    }

    @Test
    void testUpdateSkillx10ByName_NotFound() {
        // given
        String oldName = "Nonexistent Name";
        String newName = "New Name";

        UpdateSkillRequest updateRequest = new UpdateSkillRequest();
        updateRequest.setName(newName);

        // when
        when(skillRepo.findByName(oldName)).thenReturn(Collections.emptyList());

        // then
        assertThatThrownBy(() -> service.updateSkillx10ByName(updateRequest, oldName))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("Навыки с названием")
            .hasMessageContaining("не найдены")
            .extracting(ResponseStatusException.class::cast)
            .extracting(ResponseStatusException::getStatusCode)
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

    @Test
    public void deleteSkillsByNames_notExist_throw404() {
        // given
        List<String> skillNames = List.of("SkillA");
        when(skillRepo.findByName("SkillA")).thenReturn(Collections.emptyList());

        // then
        assertThatThrownBy(() -> service.deleteSkillsByNames(skillNames))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("Навык SkillA не найден")
            .extracting(ResponseStatusException.class::cast)
            .extracting(ResponseStatusException::getStatusCode)
            .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void deleteSkillsByNames_existWithCharacters_throw422() {
        // given
        List<String> skillNames = List.of("SkillB");

        Skill skill = new Skill();
        skill.setCharacters(Collections.singletonList(new CharacterEntity()));

        when(skillRepo.findByName("SkillB")).thenReturn(List.of(skill));

        // then
        assertThatThrownBy(() -> service.deleteSkillsByNames(skillNames))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("Этот навык есть как минимум у одного персонажа!")
            .extracting(ResponseStatusException.class::cast)
            .extracting(ResponseStatusException::getStatusCode)
            .isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    public void deleteSkillsByNames_existWithoutCharacters_success() {
        // given
        List<String> skillNames = List.of("SkillC");

        Skill skill = new Skill();
        skill.setCharacters(Collections.emptyList());

        when(skillRepo.findByName("SkillC")).thenReturn(List.of(skill));

        // when
        service.deleteSkillsByNames(skillNames);

        // then
        verify(skillRepo, times(1)).delete(skill);
    }

    @Test
    public void deleteSkillsByNames_multipleExistWithoutCharacters_success() {
        // given
        List<String> skillNames = List.of("SkillD", "SkillE");

        Skill skillD = new Skill();
        skillD.setCharacters(Collections.emptyList());

        Skill skillE = new Skill();
        skillE.setCharacters(Collections.emptyList());

        when(skillRepo.findByName("SkillD")).thenReturn(List.of(skillD));
        when(skillRepo.findByName("SkillE")).thenReturn(List.of(skillE));

        // when
        service.deleteSkillsByNames(skillNames);

        // then
        verify(skillRepo, times(1)).delete(skillD);
        verify(skillRepo, times(1)).delete(skillE);
    }

    @Test
    public void getUniqueSkillPage_emptyPage_returnsEmptyPage() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Skill> skillPage = Page.empty(pageable);
        when(skillRepo.findAll(pageable)).thenReturn(skillPage);

        // when
        Page<SkillDto> result = service.getUniqueSkillPage(pageable);

        // then
        assertThat(result.getTotalElements()).isEqualTo(0);
    }
}
