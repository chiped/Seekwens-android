package com.chinmay.seekwens.util;

import javax.inject.Singleton;

@Singleton
public class Rules {

    public int cardsPerPlayer(int totalPlayers) {
        switch (totalPlayers) {
            case 2:
                return 7;
            case 3:
            case 4:
                return 6;
            case 6:
                return 5;
            case 8:
            case 9:
                return 4;
            case 10:
            case 12:
                return 3;
            default:
                return -1;
        }
    }

    public int sequencesNeeded(int teams) {
        return teams == 2 ? 2 : 1;
    }
}
