package ru.nightcityroleplay.backend.service;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockitoAnnotations;
import ru.nightcityroleplay.backend.entity.CharacterEntity;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class CharacterStatsSeviceTest {
    private CharacterStatsService characterStatsService;

    @BeforeEach
    void setUp() {

        MockitoAnnotations.openMocks(this);

        characterStatsService = new CharacterStatsService();
    }

    @ParameterizedTest
    @MethodSource("calculateImplantPointsData")
    void calculateImplantPoints(int reputation, int implantPoints) {
        // Given
        var character = new CharacterEntity();
        character.setReputation(reputation);
        character.setAge(26);

        // When
        characterStatsService.updateCharacterStats(character);

        // Then
        assertThat(character.getImplantPoints()).isEqualTo(implantPoints);

    }

    public static Stream<Arguments> calculateImplantPointsData() {
        // reputation, implantPoints
        return Stream.of(
            Arguments.of(-1, 7),
            Arguments.of(0, 7),
            Arguments.of(18, 7),
            Arguments.of(19, 7),
            Arguments.of(29, 8),
            Arguments.of(30, 9),
            Arguments.of(39, 9),
            Arguments.of(40, 10),
            Arguments.of(100, 10)
        );
    }

    @Test
    void calculateBattlePoints_youngAge() {
        // Given
        int age = 20;

        // When
        int result = characterStatsService.calculateBattlePoints(age);

        // Then
        assertThat(result).isEqualTo(13);
    }

    @Test
    void calculateBattlePoints_middleAge() {
        // Given
        int age = 30;

        // When
        int result = characterStatsService.calculateBattlePoints(age);

        // Then
        assertThat(result).isEqualTo(15);
    }

    @Test
    void calculateBattlePoints_oldAge() {
        // Given
        int age = 50;

        // When
        int result = characterStatsService.calculateBattlePoints(age);

        // Then
        assertThat(result).isEqualTo(17);
    }


    @Test
    void calculateCivilPoints_isAlwaysThirteen() {
        // When
        int result = characterStatsService.calculateCivilPoints();

        // Then
        assertThat(result).isEqualTo(13);
    }
}