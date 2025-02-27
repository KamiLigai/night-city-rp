package ru.nightcityroleplay.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.server.ResponseStatusException;
import ru.nightcityroleplay.backend.dto.CreateSkillRequest;
import ru.nightcityroleplay.backend.dto.CreateSkillResponse;
import ru.nightcityroleplay.backend.dto.SkillDto;
import ru.nightcityroleplay.backend.dto.UpdateSkillRequest;
import ru.nightcityroleplay.backend.entity.CharacterEntity;
import ru.nightcityroleplay.backend.entity.Skill;
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

        List<CreateSkillResponse> responses = service.createSkill(request);

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

        List<CreateSkillResponse> responses = service.createSkill(request);

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
        String oldSkillFamily = randomUUID().toString();
        UpdateSkillRequest updateRequest = new UpdateSkillRequest();
        updateRequest.setName("New Name");
        updateRequest.setSkillFamily("New Skill Family");
        updateRequest.setDescription("New Description");
        updateRequest.setTypeIsBattle(true);

        when(skillRepo.findBySkillFamily(oldSkillFamily)).thenReturn(Collections.emptyList());

        // when / then
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
            service.updateSkill(updateRequest, oldSkillFamily));

        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(exception.getReason()).isEqualTo("Навыки с skillFamily " + oldSkillFamily + " не найдены");

        verify(skillRepo, never()).save(any());
    }

    @Test
    void testUpdateSkillsBySkillFamily_Success() {
        // given
        String oldName = "Old Name";
        String newName = "New Name";
        String oldSkillFamily = randomUUID().toString();
        String newSkillFamily = randomUUID().toString();

        UpdateSkillRequest updateRequest = new UpdateSkillRequest();
        updateRequest.setSkillFamily(newSkillFamily); // Устанавливаем новое skillFamily
        updateRequest.setName(newName);
        updateRequest.setDescription("New Description");
        updateRequest.setTypeIsBattle(true);

        Skill skill = new Skill();
        skill.setId(randomUUID()); // Устанавливаем ID для навыка
        skill.setSkillFamily(oldSkillFamily);
        skill.setName(oldName);
        skill.setDescription("Old Description");
        skill.setTypeIsBattle(false);

        // Мокаем репозиторий, чтобы он вернул список с существующим навыком
        when(skillRepo.findBySkillFamily(oldSkillFamily)).thenReturn(Collections.singletonList(skill));

        // when
        service.updateSkill(updateRequest, oldSkillFamily);

        // then
        verify(skillRepo, times(1)).save(skill); // Проверяем, что скилл был сохранён 1 раз
        assertThat(skill.getSkillFamily()).isEqualTo(newSkillFamily); // Проверяем обновление skillFamily
        assertThat(skill.getName()).isEqualTo(newName);
        assertThat(skill.getDescription()).isEqualTo("New Description");
        assertThat(skill.getTypeIsBattle()).isTrue();
    }


    @Test
    public void deleteSkill_skillExists_success() {
        // given
        String skillFamily = randomUUID().toString();

        Skill skill = new Skill();
        skill.setSkillFamily(skillFamily);
        skill.setCharacters(List.of());

        when(skillRepo.findBySkillFamily(skillFamily)).thenReturn(List.of(skill));

        // when
        service.deleteSkillsBySkillFamily(List.of(skillFamily));

        // then
        verify(skillRepo).delete(skill);
    }

    @Test
    public void deleteSkill_skillNotExists_throw404() {
        // given
        String skillFamily = UUID.randomUUID().toString();

        // when
        when(skillRepo.findBySkillFamily(skillFamily)).thenReturn(Collections.emptyList());

        // then
        assertThatThrownBy(() -> service.deleteSkillsBySkillFamily(List.of(skillFamily)))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("Навык " + skillFamily + " не найден")
            .extracting(ResponseStatusException.class::cast)
            .extracting(ResponseStatusException::getStatusCode)
            .isEqualTo(HttpStatus.NOT_FOUND);
    }
    @Test
    public void deleteSkill_unauthorized_throw422() {
        // given
        String skillFamily = UUID.randomUUID().toString();

        Skill skill = new Skill();
        skill.setSkillFamily(skillFamily);
        skill.setCharacters(List.of(new CharacterEntity()));

        when(skillRepo.findBySkillFamily(skillFamily)).thenReturn(List.of(skill));

        // then
        assertThatThrownBy(() -> service.deleteSkillsBySkillFamily(List.of(skillFamily)))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("Этот навык есть как минимум у одного персонажа!")
            .extracting(ResponseStatusException.class::cast)
            .extracting(ResponseStatusException::getStatusCode)
            .isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    public void deleteSkillsBySkillFamily_notExist_throw404() {
        // given
        List<String> skillFamilies = List.of("SkillA");
        when(skillRepo.findBySkillFamily("SkillA")).thenReturn(Collections.emptyList());

        // then
        assertThatThrownBy(() -> service.deleteSkillsBySkillFamily(skillFamilies))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("Навык SkillA не найден")
            .extracting(ResponseStatusException.class::cast)
            .extracting(ResponseStatusException::getStatusCode)
            .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void deleteSkillsByNames_existWithCharacters_throw422() {
        // given
        List<String> skillFamily = List.of("SkillB");

        Skill skill = new Skill();
        skill.setCharacters(Collections.singletonList(new CharacterEntity()));

        when(skillRepo.findBySkillFamily("SkillB")).thenReturn(List.of(skill));

        // then
        assertThatThrownBy(() -> service.deleteSkillsBySkillFamily(skillFamily))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("Этот навык есть как минимум у одного персонажа!")
            .extracting(ResponseStatusException.class::cast)
            .extracting(ResponseStatusException::getStatusCode)
            .isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    public void deleteSkillsByNames_existWithoutCharacters_success() {
        // given
        List<String> skillFamily = List.of("SkillC");

        Skill skill = new Skill();
        skill.setCharacters(Collections.emptyList());

        when(skillRepo.findBySkillFamily("SkillC")).thenReturn(List.of(skill));

        // when
        service.deleteSkillsBySkillFamily(skillFamily);

        // then
        verify(skillRepo, times(1)).delete(skill);
    }

    @Test
    public void deleteSkillsByNames_multipleExistWithoutCharacters_success() {
        // given
        List<String> skillFamily = List.of("SkillD", "SkillE");

        Skill skillD = new Skill();
        skillD.setCharacters(Collections.emptyList());

        Skill skillE = new Skill();
        skillE.setCharacters(Collections.emptyList());

        when(skillRepo.findBySkillFamily("SkillD")).thenReturn(List.of(skillD));
        when(skillRepo.findBySkillFamily("SkillE")).thenReturn(List.of(skillE));

        // when
        service.deleteSkillsBySkillFamily(skillFamily);

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
