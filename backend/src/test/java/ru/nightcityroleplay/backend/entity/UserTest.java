package ru.nightcityroleplay.backend.entity;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.security.core.GrantedAuthority;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.Assertions.assertThat;

class UserTest {

    @ParameterizedTest
    @MethodSource("getAuthoritiesData")
    void getAuthorities(List<String> roles, List<String> expectedAuthorities) {
        // given
        var user = new User();
        Set<Role> rolesEntities = roles.stream()
            .map(this::createRole)
            .collect(toSet());
        user.setRoles(rolesEntities);

        // when
        var actualAuthorities = user.getAuthorities();

        // then
        assertThat(actualAuthorities)
            .map(GrantedAuthority::getAuthority)
            .containsExactlyInAnyOrderElementsOf(expectedAuthorities);
    }

    public static Stream<Arguments> getAuthoritiesData() {
        // roles, expectedAuthorities
        return Stream.of(
            Arguments.of(List.of("USER", "ADMIN"), List.of("ROLE_USER", "ROLE_ADMIN")),
            Arguments.of(List.of(), List.of())
        );
    }

    private Role createRole(String name) {
        return new Role(UUID.randomUUID(), name);
    }
}