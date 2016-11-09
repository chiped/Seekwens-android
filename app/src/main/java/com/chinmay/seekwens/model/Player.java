package com.chinmay.seekwens.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Player {
    public String id;
    public String name;
    public int team;
    public int order;
    public Map<String, Card> hand;

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
