package com.chinmay.seekwens.model;

import java.util.ArrayList;
import java.util.List;

public class Player {
    public String id;
    public String name;
    public int team;
    public List<Card> hand = new ArrayList<>();

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public static Player with(String id, String name) {
        final Player player = new Player();
        player.name = name;
        player.id = id;
        return player;
    }
}
