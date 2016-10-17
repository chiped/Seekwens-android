package com.chinmay.seekwens.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Team {
    public String id;
    public List<Player> playerList = new ArrayList<>();

    public Team() {
        this.id = UUID.randomUUID().toString();
    }

    public String getId() {
        return id;
    }
}
