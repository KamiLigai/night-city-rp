package ru.nightcityroleplay.backend.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.nightcityroleplay.backend.entity.Implant;

import java.util.List;
import java.util.UUID;

public interface ImplantRepository extends JpaRepository<Implant, UUID> {
    List<Implant> findAllByIdIn(List<UUID> ids);
}

