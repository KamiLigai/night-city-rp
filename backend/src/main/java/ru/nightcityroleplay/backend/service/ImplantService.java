package ru.nightcityroleplay.backend.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.nightcityroleplay.backend.dto.CreateImplantRequest;
import ru.nightcityroleplay.backend.dto.CreateImplantResponse;
import ru.nightcityroleplay.backend.dto.ImplantDto;
import ru.nightcityroleplay.backend.dto.UpdateImplantRequest;
import ru.nightcityroleplay.backend.entity.Implant;
import ru.nightcityroleplay.backend.exception.NightCityRpException;
import ru.nightcityroleplay.backend.repo.ImplantRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.util.stream.Collectors.toList;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;


@Service
@Slf4j
public class ImplantService {

    private final ImplantRepository implantRepo;


    public ImplantService(ImplantRepository implantRepo) {
        this.implantRepo = implantRepo;
    }

    private ImplantDto toDto(Implant implant) {
        ImplantDto implantDto = new ImplantDto();
        implantDto.setId(implant.getId());
        implantDto.setName(implant.getName());
        implantDto.setImplantType(implant.getImplantType());
        implantDto.setDescription(implant.getDescription());
        implantDto.setReputationRequirement(implant.getReputationRequirement());
        implantDto.setImplantPointsCost(implant.getImplantPointsCost());
        implantDto.setSpecialImplantPointsCost(implant.getSpecialImplantPointsCost());
        return implantDto;
    }

    @Transactional
    public CreateImplantResponse createImplant(CreateImplantRequest request, Authentication auth) {
        log.info("Админ {} пытается создать имплант с именем {}", auth.getName(), request.getName());
        Implant implant = new Implant();
        implant.setId(UUID.randomUUID());
        implant.setName(request.getName());
        implant.setImplantType(request.getImplantType());
        implant.setDescription(request.getDescription());
        implant.setReputationRequirement(request.getReputationRequirement());
        implant.setImplantPointsCost(request.getImplantPointsCost());
        implant.setSpecialImplantPointsCost(request.getSpecialImplantPointsCost());
        implant = implantRepo.save(implant);
        return new CreateImplantResponse(implant.getId());
    }

    @Transactional
    public Page<ImplantDto> getImplantPage(Pageable pageable) {
        Page<Implant> implantPage = implantRepo.findAll(pageable);
        List<Implant> implants = implantPage.toList();
        List<ImplantDto> implantDtos = new ArrayList<>();
        for (Implant implant : implants) {
            implantDtos.add(toDto(implant));
        }
        return new PageImpl<>(implantDtos, pageable, implantPage.getTotalElements());
    }

    @Transactional
    public ImplantDto getImplant(UUID implantId) {
        Optional<Implant> implantById = implantRepo.findById(implantId);
        return implantById.map(this::toDto).orElse(null);
    }

    @Transactional
    public void updateImplant(UpdateImplantRequest request, UUID implantId, String name) {
        log.info("Начато обновление импланта с ID: {}. Название {}", implantId, name);

        Implant existingImplant = implantRepo.findById(implantId).orElseThrow(()
            -> new NightCityRpException("Имплант не найден"));
        existingImplant.setName(request.getName());
        existingImplant.setImplantType(request.getImplantType());
        existingImplant.setDescription(request.getDescription());
        existingImplant.setReputationRequirement(request.getReputationRequirement());
        existingImplant.setImplantPointsCost(request.getImplantPointsCost());
        existingImplant.setSpecialImplantPointsCost(request.getSpecialImplantPointsCost());
        implantRepo.save(existingImplant);
        log.info("Имлпнат с ID: {} было успешно обновлено", implantId);
    }


    @Transactional
    public void deleteImplant(UUID implantId) {
        log.info("Запрос на удаление оружия с ID: {}", implantId);
        Implant implant = implantRepo.findById(implantId).orElse(null);

        if (implant == null) {
            log.info("Имплант {} не найден", implantId);
            return;
        }
        if (implant.getCharsId().isEmpty()) {
            implantRepo.delete(implant);
            log.info("Имплант с ID {} был успешно удалено", implantId);
        } else {
            log.info("Не удалось удалить оружие с ID {}: связано с характеристиками", implantId);
            throw new ResponseStatusException(
                    UNPROCESSABLE_ENTITY, "Запрещено удаление импланта, так как оно связано с характеристиками"
            );
        }
    }

    // Получение списка всех ID имплантов
    public List<UUID> getAllImplantIds() {
        return implantRepo.findAll().stream()
            .map(Implant::getId)
            .collect(toList());
    }

    // Получение деталей имплантов по списку ID
    public List<ImplantDto> getBulkImplants(List<UUID> implantIds) {
        return implantRepo.findAllById(implantIds)
            .stream()
            .map(this::toDto)
            .collect(toList());
    }

}
