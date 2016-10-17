package com.chinmay.seekwens.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Game {

    public String id;
    public boolean started;
    public List<Team> teamList = new ArrayList<>();

    public Game() {
        this.id = UUID.randomUUID().toString().substring(0, 5);
    }

    public String getId() {
        return id;
    }
}
