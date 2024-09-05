package ru.nightcityroleplay.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.nightcityroleplay.backend.dto.CreateUserRequest;
import ru.nightcityroleplay.backend.dto.UserDto;
import ru.nightcityroleplay.backend.entity.Role;
import ru.nightcityroleplay.backend.entity.User;
import ru.nightcityroleplay.backend.repo.UserRepository;

import java.util.ArrayList;
import java.util.List;

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
        List<String> roles = new ArrayList<>();
        for (int i = 0; i < user.getRoles().size(); i++) {
            Role role = user.getRoles().get(i);
            roles.add(role.getName());
        }

        return UserDto.builder()
            .id(user.getId())
            .username(user.getUsername())
            .roles(roles)
            .build();
    }

    public UserDto getCurrentUser(Authentication auth) {
        Object principal = auth.getPrincipal();
        User user = (User) principal;
        return toDto(user);
    }
}
