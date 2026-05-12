package com.ecocircular.ecocircular.gamification.application;

import com.ecocircular.ecocircular.gamification.domain.UserLevel;
import org.springframework.stereotype.Component;

@Component
public class LevelCalculator {

    public UserLevel calculate(double totalPoints) {
        if (totalPoints >= UserLevel.PLATINO.getMinPoints()) return UserLevel.PLATINO;
        if (totalPoints >= UserLevel.ORO.getMinPoints()) return UserLevel.ORO;
        if (totalPoints >= UserLevel.PLATA.getMinPoints()) return UserLevel.PLATA;
        return UserLevel.BRONCE;
    }

    public int pointsToNextLevel(double totalPoints) {
        return switch (calculate(totalPoints)) {
            case BRONCE -> UserLevel.PLATA.getMinPoints() - (int) totalPoints;
            case PLATA -> UserLevel.ORO.getMinPoints() - (int) totalPoints;
            case ORO -> UserLevel.PLATINO.getMinPoints() - (int) totalPoints;
            case PLATINO -> 0;
        };
    }
}
