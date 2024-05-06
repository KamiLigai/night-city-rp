package ru.nightcityroleplay.backend.repo;

import org.springframework.data.repository.CrudRepository;
import ru.nightcityroleplay.backend.entity.User;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends CrudRepository<User, UUID> {

    Optional<User> findByUsername(String username);
}
