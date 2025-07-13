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
import ru.nightcityroleplay.backend.dto.skills.CreateSkillRequest;
import ru.nightcityroleplay.backend.dto.skills.CreateSkillResponse;
import ru.nightcityroleplay.backend.dto.skills.SkillDto;
import ru.nightcityroleplay.backend.dto.skills.UpdateSkillRequest;
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
        skillRepo = mock(SkillRepository.class);
        service = new SkillService(skillRepo);
    }

    @Test
    void getSkill_skillIsAbsent_throw404() {
        // given
        String skillFamily = randomUUID().toString();
        when(skillRepo.findBySkillFamilyAndLevel(skillFamily, 1))
            .thenReturn(Optional.empty());

        // then
        assertThatThrownBy(() -> service.getSkill(skillFamily))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("Навык " + skillFamily + " не найден")
            .extracting(ResponseStatusException.class::cast)
            .extracting(ResponseStatusException::getStatusCode)
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

        List<CreateSkillResponse> responses = service.createSkillFamily(request);

        ArgumentCaptor<Skill> skillCaptor = ArgumentCaptor.forClass(Skill.class);
        verify(skillRepo, times(10)).save(skillCaptor.capture());

        List<Skill> savedSkills = skillCaptor.getAllValues();
        UUID skillFamilyId = savedSkills.get(0).getSkillFamilyId();

        for (int i = 0; i < 10; i++) {
            Skill skill = savedSkills.get(i);
            assertThat(skill.getLevel()).isEqualTo(i + 1);
            assertThat(skill.getSkillFamilyId()).isEqualTo(skillFamilyId);
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

        List<CreateSkillResponse> responses = service.createSkillFamily(request);

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
    void testBuildSkill_ThrowsExceptionForInvalidLevel() {
        // given
        CreateSkillRequest request = new CreateSkillRequest();
        request.setSkillFamily("ExampleFamily");
        request.setName("InvalidSkill");
        UUID skillFamilyId = UUID.randomUUID(); // Создание фиктивного skillFamilyId

        // when/then
        assertThatThrownBy(() -> service.buildSkill(request, 0, skillFamilyId))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("Уровень навыка должен быть от 1 до 10")
            .extracting(ResponseStatusException.class::cast)
            .extracting(ResponseStatusException::getStatusCode)
            .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void updateSkill_nonExistingSkill_throwsNotFoundException() {
        // given
        UUID oldSkillFamilyId = randomUUID();
        UpdateSkillRequest updateRequest = new UpdateSkillRequest();
        updateRequest.setName("New Name");
        updateRequest.setSkillFamily("New Skill Family");
        updateRequest.setDescription("New Description");
        updateRequest.setTypeIsBattle(true);

        when(skillRepo.findBySkillFamilyId(oldSkillFamilyId)).thenReturn(Collections.emptyList());

        // when / then
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
            service.updateSkill(updateRequest, oldSkillFamilyId));

        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(exception.getReason()).isEqualTo("Навыки с skillFamily " + oldSkillFamilyId + " не найдены");

        verify(skillRepo, never()).save(any());
    }

    @Test
    public void deleteSkill_skillExists_success() {
        // given
        UUID skillFamilyId = randomUUID();

        Skill skill = new Skill();
        skill.setSkillFamilyId(skillFamilyId);
        skill.setCharacters(List.of());

        when(skillRepo.findBySkillFamilyId(skillFamilyId)).thenReturn(List.of(skill));

        // when
        service.deleteSkillsBySkillFamilyId(List.of(skillFamilyId));

        // then
        verify(skillRepo).delete(skill);
    }

    @Test
    public void deleteSkill_skillNotExists_throw404() {
        // given
        UUID skillFamilyId = UUID.randomUUID();

        // when
        when(skillRepo.findBySkillFamilyId(skillFamilyId)).thenReturn(Collections.emptyList());

        // then
        assertThatThrownBy(() -> service.deleteSkillsBySkillFamilyId(List.of(skillFamilyId)))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("Навык " + skillFamilyId + " не найден")
            .extracting(ResponseStatusException.class::cast)
            .extracting(ResponseStatusException::getStatusCode)
            .isEqualTo(HttpStatus.NOT_FOUND);
    }
    @Test
    public void deleteSkill_unauthorized_throw422() {
        // given
        UUID skillFamilyId = UUID.randomUUID();

        Skill skill = new Skill();
        skill.setSkillFamilyId(skillFamilyId);
        skill.setCharacters(List.of(new CharacterEntity()));

        when(skillRepo.findBySkillFamilyId(skillFamilyId)).thenReturn(List.of(skill));

        // then
        assertThatThrownBy(() -> service.deleteSkillsBySkillFamilyId(List.of(skillFamilyId)))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("Этот навык есть как минимум у одного персонажа!")
            .extracting(ResponseStatusException.class::cast)
            .extracting(ResponseStatusException::getStatusCode)
            .isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    public void deleteSkillsBySkillFamily_notExist_throw404() {
        // given
        UUID a = randomUUID();
        List<UUID> skillFamilyIds = List.of(a);
        when(skillRepo.findBySkillFamilyId(a)).thenReturn(Collections.emptyList());

        // then
        assertThatThrownBy(() -> service.deleteSkillsBySkillFamilyId(skillFamilyIds))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("Навык a не найден")
            .extracting(ResponseStatusException.class::cast)
            .extracting(ResponseStatusException::getStatusCode)
            .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void deleteSkillsByNames_existWithCharacters_throw422() {
        // given
        UUID b = randomUUID();
        List<UUID> skillFamilyId = List.of(b);

        Skill skill = new Skill();
        skill.setCharacters(Collections.singletonList(new CharacterEntity()));

        when(skillRepo.findBySkillFamilyId(b)).thenReturn(List.of(skill));

        // then
        assertThatThrownBy(() -> service.deleteSkillsBySkillFamilyId(skillFamilyId))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("Этот навык a есть как минимум у одного персонажа!")
            .extracting(ResponseStatusException.class::cast)
            .extracting(ResponseStatusException::getStatusCode)
            .isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    public void deleteSkillsByNames_existWithoutCharacters_success() {
        // given
        UUID c = randomUUID();
        List<UUID> skillFamilyId = List.of(c);

        Skill skill = new Skill();
        skill.setCharacters(Collections.emptyList());

        when(skillRepo.findBySkillFamilyId(c)).thenReturn(List.of(skill));

        // when
        service.deleteSkillsBySkillFamilyId(skillFamilyId);

        // then
        verify(skillRepo, times(1)).delete(skill);
    }

    @Test
    public void deleteSkillsByNames_multipleExistWithoutCharacters_success() {
        // given
        UUID d = randomUUID();
        UUID e = randomUUID();
        List<UUID> skillFamilyId = List.of(d, e);

        Skill skillD = new Skill();
        skillD.setCharacters(Collections.emptyList());

        Skill skillE = new Skill();
        skillE.setCharacters(Collections.emptyList());

        when(skillRepo.findBySkillFamilyId(e)).thenReturn(List.of(skillD));
        when(skillRepo.findBySkillFamilyId(d)).thenReturn(List.of(skillE));

        // when
        service.deleteSkillsBySkillFamilyId(skillFamilyId);

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

    // todo Изменить балк под новую логику навыков

// @Test
// public void getSkillBulk_skillExists_success() {
//     // given
//     UUID skill1Id = randomUUID();
//     Skill skill1 = new Skill();
//     skill1.setId(skill1Id);
//     skill1.setName("Что-то");
//     skill1.setDescription("Делает что-то 1");
//     skill1.setLevel(1);
//     skill1.setType("CIVIL");
//     skill1.setCost(2);

//     UUID skill2Id = randomUUID();
//     Skill skill2 = new Skill();
//     skill2.setId(skill2Id);
//     skill2.setName("Что-то");
//     skill2.setDescription("Делает что-то 2");
//     skill2.setLevel(1);
//     skill2.setType("CIVIL");
//     skill2.setCost(2);

//     IdsRequest idsRequest = new IdsRequest();
//     idsRequest.setIds(List.of(skill1Id, skill2Id));

//     // when
//     when(skillRepo.findAllByIdIn(List.of(skill1Id, skill2Id)))
//         .thenReturn(List.of(skill1, skill2));

//     List<SkillDto> result = service.getSkillsBulk(idsRequest);

//     // then
//     assertThat(result).isNotNull();
//     assertThat(result.get(0).getId()).isEqualTo(skill1Id);
//     assertThat(result.get(1).getId()).isEqualTo(skill2Id);

// }
}
