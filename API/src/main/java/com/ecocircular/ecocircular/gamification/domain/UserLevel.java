package com.ecocircular.ecocircular.gamification.domain;

public enum UserLevel {

    BRONCE(0, "Bronce - Comenzando tu camino reciclador"),
    PLATA(100, "Plata - Reciclador comprometido"),
    ORO(500, "Oro - Campeón del reciclaje"),
    PLATINO(1500, "Platino - Leyenda del reciclaje");

    private final int minPoints;
    private final String displayName;

    UserLevel(int minPoints, String displayName) {
        this.minPoints = minPoints;
        this.displayName = displayName;
    }

    public int getMinPoints() {
        return minPoints;
    }

    public String getDisplayName() {
        return displayName;
    }
}
