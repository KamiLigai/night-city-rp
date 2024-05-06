package ru.nightcityroleplay.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.nightcityroleplay.backend.dto.CreateUserRequest;
import ru.nightcityroleplay.backend.dto.UserDto;
import ru.nightcityroleplay.backend.entity.User;
import ru.nightcityroleplay.backend.repo.UserRepository;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;

    // todo: add validation
    @Transactional
    public UserDto createUser(CreateUserRequest request) {
        var user = User.builder()
            .username(request.username())
            .password(passwordEncoder.encode(request.password()))
            .build();
        userRepo.save(user);
        return toDto(user);
    }

    private UserDto toDto(User user) {
        return UserDto.builder()
            .id(user.getId())
            .username(user.getUsername())
            .build();
    }

    public UserDto getCurrentUser(Authentication auth) {
        return toDto((User) auth.getPrincipal());
    }
}
