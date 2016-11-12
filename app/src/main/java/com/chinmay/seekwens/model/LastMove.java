package com.chinmay.seekwens.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LastMove {
    public int player;
    public int team;
    public int tile;
    public String card;
}
