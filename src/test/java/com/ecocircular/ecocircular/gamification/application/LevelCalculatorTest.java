package com.ecocircular.ecocircular.gamification.application;

import com.ecocircular.ecocircular.gamification.domain.UserLevel;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LevelCalculatorTest {

    private final LevelCalculator calculator = new LevelCalculator();

    @Test
    void calculate_zeroPoints_returnsBronce() {
        assertThat(calculator.calculate(0)).isEqualTo(UserLevel.BRONCE);
    }

    @Test
    void calculate_belowPlataThreshold_returnsBronce() {
        assertThat(calculator.calculate(99)).isEqualTo(UserLevel.BRONCE);
    }

    @Test
    void calculate_atPlataThreshold_returnsPlata() {
        assertThat(calculator.calculate(100)).isEqualTo(UserLevel.PLATA);
    }

    @Test
    void calculate_betweenPlataAndOro_returnsPlata() {
        assertThat(calculator.calculate(499)).isEqualTo(UserLevel.PLATA);
    }

    @Test
    void calculate_atOroThreshold_returnsOro() {
        assertThat(calculator.calculate(500)).isEqualTo(UserLevel.ORO);
    }

    @Test
    void calculate_atPlatinoThreshold_returnsPlatino() {
        assertThat(calculator.calculate(1500)).isEqualTo(UserLevel.PLATINO);
    }

    @Test
    void calculate_beyondPlatino_returnsPlatino() {
        assertThat(calculator.calculate(99999)).isEqualTo(UserLevel.PLATINO);
    }

    @Test
    void pointsToNextLevel_bronce_returnsRemainingToPlata() {
        assertThat(calculator.pointsToNextLevel(50)).isEqualTo(50);
    }

    @Test
    void pointsToNextLevel_plata_returnsRemainingToOro() {
        assertThat(calculator.pointsToNextLevel(200)).isEqualTo(300);
    }

    @Test
    void pointsToNextLevel_oro_returnsRemainingToPlatino() {
        assertThat(calculator.pointsToNextLevel(600)).isEqualTo(900);
    }

    @Test
    void pointsToNextLevel_platino_returnsZero() {
        assertThat(calculator.pointsToNextLevel(2000)).isZero();
    }
}
