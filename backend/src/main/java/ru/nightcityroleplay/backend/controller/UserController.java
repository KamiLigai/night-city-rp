package ru.nightcityroleplay.backend.controller;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import ru.nightcityroleplay.backend.dto.CreateUserRequest;
import ru.nightcityroleplay.backend.dto.CurrentUserDto;
import ru.nightcityroleplay.backend.dto.UserDto;
import ru.nightcityroleplay.backend.service.UserService;

import java.util.UUID;

@RestController
@RequestMapping("users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final MeterRegistry meterRegistry;

    @PostMapping
    public CurrentUserDto createUser(@RequestBody CreateUserRequest request) {
        meterRegistry.counter("user_created").increment();
        return userService.createUser(request);
    }

    @GetMapping("me")
    public CurrentUserDto getCurrentUser(Authentication auth) {
        meterRegistry.counter("current_user_requested").increment();
        return userService.getCurrentUser(auth);
    }

    @GetMapping
    public Page<UserDto> getAllUsers(Pageable pageble) {
        return userService.getUserPage(pageble);
    }
    @GetMapping("{userId}")
    public CurrentUserDto getUserById(@PathVariable UUID userId) {
        return userService.getUserById(userId);
    }
}
