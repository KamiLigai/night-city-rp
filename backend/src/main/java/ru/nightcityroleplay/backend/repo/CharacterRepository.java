package ru.nightcityroleplay.backend.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.nightcityroleplay.backend.entity.CharactersEntity;

import java.util.UUID;

public interface CharacterRepository  extends JpaRepository<CharactersEntity, UUID> {

}
