package ru.nightcityroleplay.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.nightcityroleplay.backend.dto.CreateUserRequest;
import ru.nightcityroleplay.backend.dto.UserDto;
import ru.nightcityroleplay.backend.entity.Role;
import ru.nightcityroleplay.backend.entity.User;
import ru.nightcityroleplay.backend.repo.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;

    // todo: add validation
    @Transactional
    public UserDto createUser(CreateUserRequest request) {
        List<Role> roles = new ArrayList<>();
        var user = User.builder()
            .username(request.username())
            .password(passwordEncoder.encode(request.password()))
            .roles(roles)
            .build();
        userRepo.save(user);
        return toDto(user);
    }
    @Transactional
    public UserDto toDto(User user) {
        return UserDto.builder()
            .id(user.getId())
            .username(user.getUsername())
            .build();
    }
    @Transactional
    public UserDto getCurrentUser(Authentication auth) {
        Object principal = auth.getPrincipal();
        User user = (User) principal;
        return toDto(user);
    }

    @Transactional
    public Page<UserDto> getUserPage(Pageable pageable) {
        Page<User> userPage = userRepo.findAll(pageable);
        List<User> users = userPage.toList();
        List<UserDto> userDtos = users.stream()
            .map(this::toDto)
            .collect(Collectors.toList());
        return new PageImpl<>(userDtos, pageable, userPage.getTotalElements());
    }
    @Transactional
    public UserDto getUserById(UUID userId) {
        User user = userRepo.findById(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with ID: " + userId));
        return toDto(user);
    }
}

