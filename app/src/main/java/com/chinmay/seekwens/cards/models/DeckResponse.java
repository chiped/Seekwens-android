package com.chinmay.seekwens.cards.models;

import com.chinmay.seekwens.model.Card;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DeckResponse {

    public boolean success;

    @JsonProperty("deck_id")
    public String deckId;

    public List<Card> cards;
}
