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
    private CharacterStatsService statsService;

    @BeforeEach
    void setUp() {

        MockitoAnnotations.openMocks(this);

        statsService = new CharacterStatsService();
    }

    @ParameterizedTest
    @MethodSource("calculateImplantPointsData")
    void calculateImplantPoints(int reputation, int reqImplantPoints) {
        // given
        var character = new CharacterEntity();
        character.setReputation(reputation);
        character.setAge(26);

        // when
        statsService.updateCharacterStats(character);
        int implantPoints = statsService.calculateImplantPoints(reputation);

        // then
        assertThat(implantPoints).isEqualTo(reqImplantPoints);

    }

    public static Stream<Arguments> calculateImplantPointsData() {
        // reputation, reqImplantPoints
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
    void calculateBattlePoints_youngAge_success() {
        // given
        int age = 20;

        // when
        int result = statsService.calculateBattlePoints(age);

        // then
        assertThat(result).isEqualTo(13);
    }

    @Test
    void calculateBattlePoints_middleAge_success() {
        // given
        int age = 30;

        // when
        int result = statsService.calculateBattlePoints(age);

        // then
        assertThat(result).isEqualTo(15);
    }

    @Test
    void calculateBattlePoints_oldAge_success() {
        // given
        int age = 50;

        // when
        int result = statsService.calculateBattlePoints(age);

        // then
        assertThat(result).isEqualTo(17);
    }


    @Test
    void calculateCivilPoints_isAlwaysThirteen_success() {
        // when
        int result = statsService.calculateCivilPoints();

        // then
        assertThat(result).isEqualTo(13);
    }
}