package com.chinmay.seekwens.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Map;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Game {

    public String id;
    public String ownerId;
    public boolean started;
    public Map<String, Player> players;

    public Game() {
        this.id = UUID.randomUUID().toString().substring(0, 5);
    }

    public String getId() {
        return id;
    }

    public static Game with(String ownerId) {
        final Game game = new Game();
        game.ownerId = ownerId;
        return game;
    }
}
