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
import ru.nightcityroleplay.backend.dto.implants.CreateImplantRequest;
import ru.nightcityroleplay.backend.dto.implants.CreateImplantResponse;
import ru.nightcityroleplay.backend.dto.implants.ImplantDto;
import ru.nightcityroleplay.backend.dto.implants.UpdateImplantRequest;
import ru.nightcityroleplay.backend.entity.Implant;
import ru.nightcityroleplay.backend.repo.ImplantRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.util.stream.Collectors.toList;
import static org.springframework.http.HttpStatus.NOT_FOUND;


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

        // Валидация входных данных
        if (request.getName() == null || request.getName().isBlank()) {
            log.info("Ошибка: Имя импланта не может быть пустым. Пользователь: {}",
                auth.getName());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Имя импланта не может быть пустым.");
        }
        if (request.getImplantType() == null || request.getImplantType().isBlank()) {
            log.info("Ошибка: Тип импланта не может быть пустым. Пользователь: {}",
                auth.getName());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Тип импланта не может быть пустым.");
        }
        if (request.getDescription() == null || request.getDescription().isBlank()) {
            log.info("Ошибка: Описание не может быть пустым. Пользователь: {}",
                auth.getName());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Описание не может быть пустым.");
        }
        if (request.getReputationRequirement() < 0) {
            log.info("Ошибка: Требование к репутации не может быть отрицательным. Пользователь: {}",
                auth.getName());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Требование к репутации не может быть отрицательным.");
        }
        if (request.getImplantPointsCost() < 0) {
            log.info("Ошибка: Стоимость очков импланта не может быть отрицательной. Пользователь: {}",
                auth.getName());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Стоимость очков импланта не может быть отрицательной.");
        }
        if (request.getSpecialImplantPointsCost() < 0) {
            log.info("Ошибка: Стоимость особых имплантных очков не может быть отрицательной. Пользователь: {}",
                auth.getName());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Стоимость особых имплантных очков не может быть отрицательной.");
        }

        // Создание импланта
        Implant implant = new Implant();
        implant.setId(UUID.randomUUID());
        implant.setName(request.getName());
        implant.setImplantType(request.getImplantType());
        implant.setDescription(request.getDescription());
        implant.setReputationRequirement(request.getReputationRequirement());
        implant.setImplantPointsCost(request.getImplantPointsCost());
        implant.setSpecialImplantPointsCost(request.getSpecialImplantPointsCost());
        implant = implantRepo.save(implant);

        log.info("Имплант с именем {} успешно создан пользователем {}",
            request.getName(), auth.getName());
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
    public Integer getImplantAssignmentsCount(UUID implantId) {
        Optional<Implant> implantById = implantRepo.findById(implantId);
        if (implantById.isEmpty()) {
            throw new ResponseStatusException(NOT_FOUND, "Имплант " + implantId + " не найден");
        }
        return implantById.get().getChars().size();
    }

    @Transactional
    public void updateImplant(UpdateImplantRequest request, UUID implantId, String name) {
        log.info("Администратор пытается создать имплант с именем: {} с id {}", name, implantId);
        if (request.getReputationRequirement() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Требование к репутации не может быть отрицательным");
        }
        if (request.getImplantPointsCost() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Стоимость очков импланта не может быть отрицательной");
        }
        if (request.getSpecialImplantPointsCost() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Стоимость особых очков импланта не может быть отрицательной");
        }
        if (request.getName() == null || request.getName().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Имя импланта не должно быть пустым");
        }
        if (request.getImplantType() == null || request.getImplantType().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Тип импланта не должен быть пустым");
        }
        if (request.getDescription() == null || request.getDescription().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Описание не должно быть пустым");
        }
        // Проверка, существует ли имплант с указанным ID
        Implant existingImplant = implantRepo.findById(implantId).orElseThrow(() ->
            new ResponseStatusException(HttpStatus.NOT_FOUND, "Имплант не найден")
        );
        // Обновление существующего импланта с указанными характеристиками
        existingImplant.setName(request.getName());
        existingImplant.setImplantType(request.getImplantType());
        existingImplant.setDescription(request.getDescription());
        existingImplant.setReputationRequirement(request.getReputationRequirement());
        existingImplant.setImplantPointsCost(request.getImplantPointsCost());
        existingImplant.setSpecialImplantPointsCost(request.getSpecialImplantPointsCost());

        implantRepo.save(existingImplant);
        log.info("Имплант с ID: {} был успешно обновлен", implantId);
    }

    @Transactional
    public void deleteImplant(UUID implantId, boolean ignoreAssignments) {
        log.info("Запрос на удаление импланта с ID: {}", implantId);

        Implant implant = implantRepo.findById(implantId).orElse(null);

        if (implant == null) {
            log.info("Имплант {} не найден", implantId);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Имплант не найден");
        }
        if (!ignoreAssignments) {
            if (!implant.getChars().isEmpty()) {
                log.info("Не удалось удалить имплант с ID {}: так как он встроен в персонажей", implantId);
                throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Запрещено удаление импланта, так как он встроен в персонажей");
            }
            implantRepo.delete(implant);
            log.info("Имплант с ID {} был успешно удалён", implantId);
            return;
        }

        implantRepo.delete(implant);
        log.info("Имплант с ID {} был успешно удалён", implantId);
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

