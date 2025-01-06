package ru.nightcityroleplay.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.nightcityroleplay.backend.dto.CreateUserRequest;
import ru.nightcityroleplay.backend.dto.UserDto;
import ru.nightcityroleplay.backend.entity.User;
import ru.nightcityroleplay.backend.repo.UserRepository;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepo;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private CreateUserRequest createUserRequest;

    @BeforeEach
    public void setUp() {
        createUserRequest = new CreateUserRequest("testUser", "testPassword");
    }

    @Test
    public void testCreateUser() {
        //given
        String encodedPassword = "encodedPassword";
        when(passwordEncoder.encode(createUserRequest.password())).thenReturn(encodedPassword);

        //when
        UserDto result = userService.createUser(createUserRequest);

        //then
        assertNotNull(result);
        assertEquals(createUserRequest.username(), result.username());
        verify(passwordEncoder).encode(createUserRequest.password());
        verify(userRepo).save(any(User.class));
    }

    @Test
    public void testGetCurrentUser_userCreated_success() {
        //given
        UUID userId = UUID.randomUUID();
        User user = new User(userId, "testUser", "password", List.of());

        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(user);

        //when
        UserDto result = userService.getCurrentUser(auth);

        //then
        assertEquals("testUser", result.username());
        assertEquals(userId, result.id());
    }
}




