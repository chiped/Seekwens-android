package com.chinmay.seekwens.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Card {
    public String code;
    public String image;
    public String id;

    public void setId(String id) {
        this.id = id;
    }
}
