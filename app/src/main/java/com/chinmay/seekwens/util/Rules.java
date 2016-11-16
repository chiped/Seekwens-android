package com.chinmay.seekwens.util;

import com.chinmay.seekwens.cards.Cards;
import com.chinmay.seekwens.model.Card;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Singleton;

@Singleton
public class Rules {

    private static final int BOARD_WH = 10;
    private static int BOARD_SIZE = 100;

    private static final Set<String> oneEyedJacks = new HashSet<>(Arrays.asList(new String[] {Cards.HJ, Cards.SJ}));
    private static final Set<String> twoEyedJacks = new HashSet<>(Arrays.asList(new String[] {Cards.DJ, Cards.CJ}));
    private static final Set<Integer> wildCards = new HashSet<>(Arrays.asList(new Integer[]{0, 9, 90, 99}));

    private static final Map<Character, String> numberMap = new HashMap<Character, String>() {{
        put('A', "Ace");
        put('2', "Two");
        put('3', "Three");
        put('4', "Four");
        put('5', "Five");
        put('6', "Six");
        put('7', "Seven");
        put('8', "Eight");
        put('9', "Nine");
        put('0', "Ten");
        put('K', "King");
        put('Q', "Queen");
    }};

    private Map<Character, String> suitMap = new HashMap<Character, String>(){{
        put('S', "Spades");
        put('C', "Clubs");
        put('H', "Hearts");
        put('D', "Diamonds");
    }};

    public static final String[] board = {
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
            case BOARD_WH:
            case 12:
                return 3;
            default:
                return -1;
        }
    }

    public int sequencesNeeded(int teams) {
        return teams == 2 ? 2 : 1;
    }

    public List<Integer> emptyBoard() {
        List<Integer> board = new ArrayList<>(BOARD_SIZE);
        for (int i = 0; i < BOARD_SIZE; i++) {
            board.add(-1);
        }
        return board;
    }

    public boolean canPlayCardHere(Card selectedCard, String cardCode, int teamCode, int playerTeam) {
        if (selectedCard == null) {
            return false;
        }

        if (oneEyedJacks.contains(selectedCard.code)) {
            return teamCode != -1 && teamCode != playerTeam;
        }

        if (twoEyedJacks.contains(selectedCard.code)) {
            return teamCode == -1;
        }

        return selectedCard.code.equals(cardCode) && teamCode == -1;
    }

    public boolean shouldRemoveCoin(String cardCode) {
        return oneEyedJacks.contains(cardCode);
    }

    public String[][] transform2D(List<Long> chips, long playerTeam) {
        final String[][] chips2D = new String[BOARD_WH][BOARD_WH];
        for (int i = 0; i < chips.size(); i++) {
            final int x = i % BOARD_WH;
            final int y = i / BOARD_WH;
            final String chip = chips.get(i) == -1 ? "X" : String.valueOf(chips.get(i));
            chips2D[y][x] = wildCards.contains(i) ? String.valueOf(playerTeam) : chip;
        }
        return chips2D;
    }

    public String horizontalString(String[][] chips) {
        final StringBuilder horizontal = new StringBuilder();
        for (int i = 0; i < BOARD_WH; i++) {
            for (int j = 0; j < BOARD_WH; j++) {
                horizontal.append(chips[i][j]);
            }
            horizontal.append("X");
        }
        return horizontal.toString();
    }

    public String verticalString(String[][] chips) {
        final StringBuilder vertical = new StringBuilder();
        for (int i = 0; i < BOARD_WH; i++) {
            for (int j = 0; j < BOARD_WH; j++) {
                vertical.append(chips[j][i]);
            }
            vertical.append("X");
        }
        return vertical.toString();
    }

    public String backSlashString(String[][] chips) {
        final StringBuilder backSlash = new StringBuilder();
        for (int i = 0; i < 2* BOARD_WH -1; i++) {
            for (int j = Math.max(0, i- BOARD_WH +1); j <= Math.min(i, BOARD_WH -1); j++) {
                backSlash.append(chips[j][i-j]);
            }
            backSlash.append("X");
        }
        return backSlash.toString();
    }

    public String forwardSlashString(String[][] chips) {
        final StringBuilder forwardSlash = new StringBuilder();
        for (int i = 0; i < 2* BOARD_WH -1; i++) {
            for (int j = Math.max(0, i- BOARD_WH +1); j <= Math.min(i, BOARD_WH -1); j++) {
                forwardSlash.append(chips[BOARD_WH -1+j-i][j]);
            }
            forwardSlash.append("X");
        }
        return forwardSlash.toString();
    }

    public String cardName(String cardCode, int tile) {
        if (oneEyedJacks.contains(cardCode)) {
            return String.format("one eyed jack at %s", cardName(board[tile]));
        } else if (twoEyedJacks.contains(cardCode)) {
            return String.format("two eyed jack at %s", cardName(board[tile]));
        } else {
            return cardName(cardCode);
        }
    }

    private String cardName(String cardCode) {
        final String number = numberMap.get(cardCode.charAt(0));
        final String suit = suitMap.get(cardCode.charAt(1));
        return String.format("%s of %s", number, suit);
    }
}
