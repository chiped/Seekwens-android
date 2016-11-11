package com.chinmay.seekwens.util;

import javax.inject.Singleton;

@Singleton
public class Rules {

    public final String[] board = {
            "XX","6D","7D","8D","9D","0D","QD","KD","AD","XX",
            "5D","3H","2H","2S","3S","4S","5S","6S","7S","AC",
            "4D","4H","KD","AD","AC","KC","QC","0C","8S","KC",
            "3D","5H","QD","QH","0H","9H","8H","9C","9S","QC",
            "2D","6H","0D","KH","3H","2H","7H","8C","0S","0C",
            "AS","7H","9D","AH","4H","5H","6H","7C","QS","9C",
            "KS","8H","8D","2C","3C","4C","5C","6C","KS","8C",
            "QS","9H","7D","6D","5D","4D","3D","2D","AS","7C",
            "0S","0H","QH","KH","AH","2C","3C","4C","5C","6C",
            "XX","9S","8S","7S","6S","5S","4S","3S","2S","XX"
    };

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
